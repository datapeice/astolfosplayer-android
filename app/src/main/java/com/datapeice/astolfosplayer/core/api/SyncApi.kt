package com.datapeice.astolfosplayer.core.api

import android.content.Context
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.datapeice.astolfosplayer.R
import com.datapeice.astolfosplayer.core.data.Settings
import com.datapeice.astolfosplayer.core.utils.FileHasher
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.jvm.javaio.copyTo
import io.ktor.utils.io.streams.asInput

interface SyncApi {
    suspend fun performSync(
        localFolder: DocumentFile,
        onProgress: (current: Int, total: Int, message: String) -> Unit,
        onComplete: suspend () -> Unit = {},
        onTrackIdReceived: suspend (fileHash: String, trackId: String) -> Unit = { _, _ -> }
    )
}

class KtorSyncApi(
    private val httpClient: HttpClient,
    private val settings: Settings,
    private val trackApi: TrackApi,
    private val context: Context
) : SyncApi {

    private val serverAddress: String get() = settings.serverAddress

    private fun isCriticalNetworkError(e: Exception): Boolean {
        return when (e) {
            is java.net.ConnectException,
            is java.net.UnknownHostException,
            is java.net.SocketTimeoutException,
            is java.nio.channels.ClosedSelectorException,
            is java.nio.channels.ClosedChannelException -> true
            else -> e.message?.contains("Unable to resolve host") == true ||
                    e.message?.contains("Failed to connect") == true ||
                    e.message?.contains("ClosedSelector") == true ||
                    e.message?.contains("ClosedChannel") == true
        }
    }

    override suspend fun performSync(
        localFolder: DocumentFile,
        onProgress: (current: Int, total: Int, message: String) -> Unit,
        onComplete: suspend () -> Unit,
        onTrackIdReceived: suspend (fileHash: String, trackId: String) -> Unit
    ) {
        var currentStep = 0
        var hasErrors = false
        var errorMessage: String? = null

        try {
            onProgress(currentStep, 1, context.getString(R.string.getting_status))

            // Проверка доступности сервера
            if (serverAddress.isBlank()) {
                errorMessage = "Server address not configured"
                onProgress(0, 1, errorMessage)
                Log.e("SyncApi", errorMessage)
                return
            }

            if (settings.accessToken.isBlank()) {
                errorMessage = "Access token not configured"
                onProgress(0, 1, errorMessage)
                Log.e("SyncApi", errorMessage)
                return
            }

            val serverFiles = try {
                Log.d("SyncApi", "Requesting status from $serverAddress/api/sync/status")
                httpClient.get("$serverAddress/api/sync/status") {
                    bearerAuth(settings.accessToken)
                    timeout {
                        requestTimeoutMillis = 30_000
                        socketTimeoutMillis = 30_000
                        connectTimeoutMillis = 30_000
                    }
                }.body<List<SyncStatusItem>>()
            } catch (e: Exception) {
                hasErrors = true
                errorMessage = when {
                    isCriticalNetworkError(e) -> "Server unavailable. Check network connection and server address."
                    e.message?.contains("401") == true -> "Authentication failed. Check access token."
                    else -> "Network error: ${e.message}"
                }
                onProgress(0, 1, context.getString(R.string.error_message, errorMessage))
                Log.e("SyncApi", "Failed to get server status", e)
                return
            }

            Log.d("SyncApi", "Server has ${serverFiles.size} files")
            val serverHashes = serverFiles.associate { it.fileHash to it }

            onProgress(currentStep, 1, context.getString(R.string.analyzing_local_files))
            val localFiles = localFolder.listFiles().filter {
                it.name?.endsWith(".mp3", true) == true ||
                        it.name?.endsWith(".flac", true) == true
            }

            Log.d("SyncApi", "Found ${localFiles.size} local audio files")

            val localFileHashes = mutableMapOf<String, DocumentFile>()
            for (file in localFiles) {
                context.contentResolver.openInputStream(file.uri)?.use { inputStream ->
                    val hash = FileHasher.calculateSha256(inputStream)
                    if (hash != null) {
                        localFileHashes[hash] = file
                        Log.d("SyncApi", "Local file ${file.name} hash: $hash")
                    }
                }
            }

            val filesToUpload = localFileHashes.filter { (hash, _) ->
                !serverHashes.containsKey(hash)
            }.values.toList()

            val filesToDownload = serverFiles.filter { serverItem ->
                !localFileHashes.containsKey(serverItem.fileHash)
            }

            val totalSteps = filesToUpload.size + filesToDownload.size

            Log.d("SyncApi", "Files to upload: ${filesToUpload.size}, to download: ${filesToDownload.size}")

            if (totalSteps == 0) {
                onProgress(0, 0, context.getString(R.string.all_files_synchronized))
                onComplete()
                return
            }

            onProgress(currentStep, totalSteps, context.getString(R.string.files_found_to_upload_download, filesToUpload.size, filesToDownload.size))

            // --- Загружаем файлы ---
            for ((index, file) in filesToUpload.withIndex()) {
                currentStep++
                val fileName = file.name ?: "unknown"
                onProgress(currentStep, totalSteps, context.getString(R.string.uploading_file_progress, index + 1, filesToUpload.size, fileName))

                try {
                    val fileSize = file.length()
                    val fileSizeMB = fileSize / 1024 / 1024
                    Log.d("SyncApi", "Uploading $fileName, size: $fileSizeMB MB")

                    // Вычисляем хеш для коллбэка
                    val fileHash = context.contentResolver.openInputStream(file.uri)?.use { inputStream ->
                        FileHasher.calculateSha256(inputStream)
                    }

                    val tempFile = java.io.File(context.cacheDir, "upload_temp_${System.currentTimeMillis()}")
                    try {
                        context.contentResolver.openInputStream(file.uri)?.use { inputStream ->
                            tempFile.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream, bufferSize = 8192)
                            }
                        }

                        tempFile.inputStream().use { inputStream ->
                            val response = httpClient.submitFormWithBinaryData(
                                url = "$serverAddress/api/tracks/upload",
                                formData = formData {
                                    append(
                                        "file",
                                        InputProvider(fileSize) { inputStream.asInput() },
                                        Headers.build {
                                            append(HttpHeaders.ContentType,
                                                if (fileName.endsWith(".flac", true)) "audio/flac" else "audio/mpeg")
                                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                                        }
                                    )
                                    append("title", fileName.substringBeforeLast("."))
                                }
                            ) {
                                bearerAuth(settings.accessToken)
                                timeout {
                                    requestTimeoutMillis = 600_000
                                    socketTimeoutMillis = 600_000
                                }
                            }

                            val trackMetadata = response.body<TrackMetadata>()
                            Log.d("SyncApi", "Upload successful: ${trackMetadata.filename}")

                            // Используем хеш от сервера для гарантии совпадения
                            trackMetadata.id?.let { id ->
                                val hash = trackMetadata.fileHash ?: fileHash
                                if (hash != null) {
                                    onTrackIdReceived(hash, id)
                                }
                            }
                        }
                    } finally {
                        if (tempFile.exists()) tempFile.delete()
                    }

                    onProgress(currentStep, totalSteps, context.getString(R.string.file_uploaded, fileName))
                } catch (e: OutOfMemoryError) {
                    hasErrors = true
                    val errorMsg = "${context.getString(R.string.upload_error, fileName)}: File too large"
                    onProgress(currentStep, totalSteps, errorMsg)
                    Log.e("SyncApi", "OOM error for $fileName", e)
                } catch (e: Exception) {
                    hasErrors = true
                    val errorMsg = "${context.getString(R.string.upload_error, fileName)}: ${e.message ?: "Unknown error"}"
                    onProgress(currentStep, totalSteps, errorMsg)
                    Log.e("SyncApi", "Upload error for $fileName", e)

                    if (isCriticalNetworkError(e)) {
                        errorMessage = "Connection lost during upload"
                        onProgress(currentStep, totalSteps, context.getString(R.string.error_message, errorMessage))
                        return
                    }
                }
            }

            // --- Скачиваем файлы ---
            // ... (начало файла SyncApi.kt)

            // --- Скачиваем файлы ---
            var allServerTracks: List<Track>? = null // Оставляем кэширование списка треков
            for ((index, syncItem) in filesToDownload.withIndex()) {
                currentStep++
                val filename = syncItem.filename
                onProgress(currentStep, totalSteps, context.getString(R.string.downloading_file_progress, index + 1, filesToDownload.size, filename))

                try {
                    if (allServerTracks == null) {
                        // Получаем все треки с сервера один раз для экономии запросов
                        allServerTracks = trackApi.getAllTracks()
                    }
                    val trackToDownload = allServerTracks.find { it.fileHash == syncItem.fileHash }
                    if (trackToDownload == null) {
                        hasErrors = true
                        onProgress(currentStep, totalSteps, context.getString(R.string.id_not_found_for_file, filename))
                        continue
                    }

                    // Проверяем, что ID трека существует
                    if (trackToDownload.id.isBlank()) {
                        hasErrors = true
                        Log.w("SyncApi", "Track ID is blank for file: $filename, hash: ${syncItem.fileHash}")
                        onProgress(currentStep, totalSteps, "Error: Track ID is missing for $filename")
                        continue
                    }

                    val mimeType = if (filename.endsWith(".flac", true)) "audio/flac" else "audio/mpeg"
                    val newFile = localFolder.createFile(mimeType, filename)

                    if (newFile != null) {
                        var totalWritten = 0L

                        httpClient.prepareGet("$serverAddress/api/tracks/${trackToDownload.id}/file") {
                            bearerAuth(settings.accessToken)
                        }.execute { response ->
                            context.contentResolver.openOutputStream(newFile.uri)?.use { output ->
                                totalWritten = response.bodyAsChannel().copyTo(output)

                                // ----> НАЧАЛО ИЗМЕНЕНИЯ <----
                                // После успешного скачивания файла вызываем коллбэк,
                                // чтобы сохранить ID трека в локальной базе данных.
                                // Мы передаем хэш файла и ID трека, полученный от сервера.
                                onTrackIdReceived(syncItem.fileHash, trackToDownload.id)
                                Log.d("SyncApi", "Track downloaded and ID assigned: ${trackToDownload.id} for file ${syncItem.filename}")
                                // ----> КОНЕЦ ИЗМЕНЕНИЯ <----
                            }
                        }

                        if (totalWritten > 0) {
                            onProgress(currentStep, totalSteps, context.getString(R.string.file_downloaded, filename))
                        } else {
                            // Если ничего не записалось, возможно, была ошибка
                            hasErrors = true
                            onProgress(currentStep, totalSteps, "Download failed for $filename: empty file")
                            newFile.delete() // Удаляем пустой файл
                        }
                    } else {
                        hasErrors = true
                        onProgress(currentStep, totalSteps, "Failed to create local file: $filename")
                    }

                } catch (e: Exception) {
                    hasErrors = true
                    val errorMsg = "${context.getString(R.string.error_file, filename)}: ${e.message ?: "Unknown error"}"
                    onProgress(currentStep, totalSteps, errorMsg)
                    Log.e("SyncApi", "Download error for $filename", e)

                    if (isCriticalNetworkError(e)) {
                        errorMessage = "Connection lost during download"
                        onProgress(currentStep, totalSteps, context.getString(R.string.error_message, errorMessage))
                        return
                    }
                }
            }

// ... (оставшаяся часть файла)


            onProgress(totalSteps, totalSteps, context.getString(R.string.updating_library))
            onComplete()

        } catch (e: Exception) {
            hasErrors = true
            errorMessage = e.message ?: "Unknown error"
            Log.e("SyncApi", "Sync error", e)
        } finally {
            val finalMessage = when {
                errorMessage != null -> "Synchronization failed: $errorMessage"
                hasErrors -> "${context.getString(R.string.synchronization_completed)} (with errors)"
                else -> context.getString(R.string.synchronization_completed)
            }
            onProgress(currentStep, currentStep.coerceAtLeast(1), finalMessage)
        }
    }
}
