package com.datapeice.astolfosplayer.core.api

import android.content.Context
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.datapeice.astolfosplayer.R
import com.datapeice.astolfosplayer.core.data.Settings
import com.datapeice.astolfosplayer.core.utils.FileHasher
import com.google.protobuf.Empty
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import sync.SyncServiceGrpcKt.SyncServiceCoroutineStub as SyncStub

interface SyncApi {
    suspend fun performSync(
        localFolder: DocumentFile,
        onProgress: (current: Int, total: Int, message: String) -> Unit,
        onComplete: () -> Unit
    )
}

class GrpcSyncApi(
    private val context: Context,
    private val settings: Settings,
    private val channelProvider: GrpcChannelProvider,
    private val trackApi: TrackApi
) : SyncApi {

    private val SUPPORTED_EXTENSIONS = setOf("mp3", "flac", "wav", "m4a", "ogg")
    private val syncStub by lazy { SyncStub(channelProvider.syncChannel) }
    private val syncMutex = Mutex()

    override suspend fun performSync(
        localFolder: DocumentFile,
        onProgress: (current: Int, total: Int, message: String) -> Unit,
        onComplete: () -> Unit
    ) = withContext(Dispatchers.IO) {

        // Проверка что синхронизация не запущена
        if (!syncMutex.tryLock()) {
            Log.w(TAG, "⚠️ Sync already in progress, skipping")
            onProgress(0, 0, context.getString(R.string.synchronization))
            return@withContext
        }

        try {
            // 1. Анализ файлов
            onProgress(0, 0, context.getString(R.string.analyzing_local_files))

            // Получаем список файлов с сервера
            Log.d(TAG, "Fetching server files...")
            val serverFilesMap = fetchServerFiles()
            val serverHashes = serverFilesMap.keys
            Log.d(TAG, "Server has ${serverHashes.size} files: $serverFilesMap")

            val localFiles = scanLocalFiles(localFolder)
            Log.d(TAG, "Found ${localFiles.size} local files")

            // Хеширование локальных файлов
            val localFileHashes = mutableMapOf<String, DocumentFile>()
            localFiles.forEachIndexed { index, file ->
                onProgress(index, localFiles.size, "Hashing: ${file.name}")
                try {
                    context.contentResolver.openInputStream(file.uri)?.use { stream ->
                        FileHasher.calculateSha256(stream)?.let { hash ->
                            localFileHashes[hash] = file
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Hash error: ${file.name}", e)
                }
            }
            Log.d(TAG, "Local hashes calculated: ${localFileHashes.keys}")

            // 2. Вычисление разницы
            val filesToUpload = localFileHashes.filterKeys { it !in serverHashes }
            val filesToDownload = serverHashes.filter { it !in localFileHashes.keys }

            Log.d(TAG, "Files to upload: ${filesToUpload.size}")
            Log.d(TAG, "Files to download: ${filesToDownload.size}")

            val totalOps = filesToUpload.size + filesToDownload.size

            if (totalOps == 0) {
                Log.d(TAG, "✅ Nothing to sync")
                onProgress(0, 0, context.getString(R.string.all_files_synchronized))
                onComplete()
                return@withContext
            }

            var currentOp = 0

            // 3. Загрузка (Upload)
            filesToUpload.forEach { (hash, file) ->
                currentOp++
                val fileName = file.name ?: "unknown"
                onProgress(currentOp, totalOps, "Uploading $fileName")

                try {
                    Log.d(TAG, "⬆️ Starting upload: $fileName (hash: $hash)")
                    val uploadedHash = trackApi.uploadTrack(file, null)
                    Log.d(TAG, "✅ Upload response received: $fileName (hash: $uploadedHash)")

                    // КРИТИЧНО: Проверяем что файл действительно появился на сервере
                    var retries = 0
                    val maxRetries = 10
                    var fileVerified = false

                    while (retries < maxRetries && !fileVerified) {
                        fileVerified = verifyFileOnServer(uploadedHash)
                        if (!fileVerified) {
                            retries++
                            Log.w(TAG, "⚠️ File not yet on server, retry $retries/$maxRetries")
                            kotlinx.coroutines.delay(200)
                        }
                    }

                    if (!fileVerified) {
                        throw Exception("Failed to verify upload on server after $maxRetries retries")
                    }

                    Log.d(TAG, "✅ Upload verified on server: $fileName")

                } catch (e: StatusException) {
                    // Если файл уже существует на сервере - проверяем его наличие
                    if (e.status.code == Status.Code.INTERNAL &&
                        e.message?.contains("UNIQUE constraint") == true) {
                        Log.w(TAG, "⚠️ File already exists on server: $fileName")
                        // Проверяем что файл действительно есть
                        if (verifyFileOnServer(hash)) {
                            Log.d(TAG, "✅ Existing file verified on server: $fileName")
                        } else {
                            Log.e(TAG, "❌ File reported as existing but not found on server!")
                            throw e
                        }
                    } else {
                        Log.e(TAG, "❌ Upload failed: $fileName", e)
                        throw e
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Upload failed: $fileName", e)
                    throw e
                }
            }

            // 4. Скачивание (Download) с проверкой хеша
            filesToDownload.forEach { expectedHash ->
                currentOp++
                val filename = serverFilesMap[expectedHash] ?: "$expectedHash.bin"

                onProgress(currentOp, totalOps, "Downloading $filename...")

                try {
                    Log.d(TAG, "⬇️ Starting download: $filename")
                    val downloadedFile = trackApi.downloadTrack(expectedHash, filename, localFolder)

                    // КРИТИЧНО: Проверяем что файл полностью записан
                    var fileReady = false
                    var retries = 0
                    val maxRetries = 10
                    var lastSize = 0L

                    while (retries < maxRetries && !fileReady) {
                        val currentSize = downloadedFile.length()
                        if (currentSize > 0 && currentSize == lastSize) {
                            // Размер стабилизировался
                            fileReady = true
                        } else {
                            lastSize = currentSize
                            retries++
                            Log.d(TAG, "⏳ Waiting for file to stabilize: $currentSize bytes, retry $retries/$maxRetries")
                            kotlinx.coroutines.delay(100)
                        }
                    }

                    if (!fileReady) {
                        throw Exception("File did not stabilize after $maxRetries checks")
                    }

                    // Проверяем хеш скачанного файла
                    Log.d(TAG, "🔍 Verifying hash for $filename...")

                    val actualHash = context.contentResolver.openInputStream(downloadedFile.uri)?.use { stream ->
                        FileHasher.calculateSha256(stream)
                    }

                    if (actualHash != expectedHash) {
                        Log.e(TAG, "❌ Hash mismatch for $filename!")
                        Log.e(TAG, "   Expected: $expectedHash")
                        Log.e(TAG, "   Got:      $actualHash")
                        Log.e(TAG, "   File size: ${downloadedFile.length()} bytes")

                        // Удаляем поврежденный файл
                        downloadedFile.delete()
                        Log.w(TAG, "🗑️ Deleted corrupted file: $filename")

                        throw Exception("Hash verification failed for $filename")
                    } else {
                        Log.d(TAG, "✅ Hash verified for $filename")
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Download failed: $filename", e)
                    if (e is StatusException && e.status.code == Status.Code.NOT_FOUND) {
                        onProgress(currentOp, totalOps, "File not found on server: $filename")
                    }
                    throw e
                }
            }

            // 5. Финиш
            Log.d(TAG, "✅ Sync completed successfully")
            onComplete()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Sync Error", e)
            throw e
        } finally {
            syncMutex.unlock()
        }
    }

    private suspend fun fetchServerFiles(): Map<String, String> {
        val response = syncStub.getSync(Empty.getDefaultInstance())
        return response.filesList.associate { it.hash to it.filename }
    }

    private suspend fun verifyFileOnServer(hash: String): Boolean {
        return try {
            val serverFiles = fetchServerFiles()
            val exists = hash in serverFiles.keys
            Log.d(TAG, "🔍 Server verification for hash $hash: ${if (exists) "EXISTS" else "NOT FOUND"}")
            exists
        } catch (e: Exception) {
            Log.e(TAG, "Failed to verify file on server", e)
            false
        }
    }

    private fun scanLocalFiles(folder: DocumentFile): List<DocumentFile> {
        val result = mutableListOf<DocumentFile>()
        val files = folder.listFiles()
        for (file in files) {
            if (file.isDirectory) {
                result.addAll(scanLocalFiles(file))
            } else if (file.isFile) {
                val ext = file.name?.substringAfterLast('.', "")?.lowercase()
                if (ext in SUPPORTED_EXTENSIONS) {
                    result.add(file)
                }
            }
        }
        return result
    }

    companion object {
        private const val TAG = "GrpcSyncApi"
    }
}