package com.datapeice.astolfosplayer.core.api

import android.content.Context
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.datapeice.astolfosplayer.app.domain.track.Track
import com.datapeice.astolfosplayer.core.data.Settings
import com.google.protobuf.ByteString
import file.File.DeleteRequest
import file.File.DownloadRequest
import file.File.FileMetadata
import file.File.UploadRequest
import file.FileServiceGrpcKt.FileServiceCoroutineStub
import kotlinx.coroutines.flow.flow

interface TrackApi {
    suspend fun uploadTrack(
        file: DocumentFile,
        track: Track? = null
    ): String

    suspend fun downloadTrack(hash: String, filename: String, destinationFolder: DocumentFile): DocumentFile

    suspend fun deleteTrack(hash: String): Boolean
}

class GrpcTrackApi(
    private val context: Context,
    private val settings: Settings,
    private val channelProvider: GrpcChannelProvider
) : TrackApi {

    private val CHUNK_SIZE = 64 * 1024
    private val fileStub by lazy { FileServiceCoroutineStub(channelProvider.fileChannel) }

    override suspend fun uploadTrack(
        file: DocumentFile,
        track: Track?
    ): String {
        val requestFlow = flow {
            val metadata = FileMetadata.newBuilder().apply {
                filename = file.name ?: "unknown"
                track?.let {
                    it.title?.let { this.title = it }
                    it.artist?.let { this.artist = it }
                    it.album?.let { this.album = it }
                    this.duration = it.duration
                }
            }.build()

            emit(UploadRequest.newBuilder().setMetadata(metadata).build())

            context.contentResolver.openInputStream(file.uri)?.use { inputStream ->
                val buffer = ByteArray(CHUNK_SIZE)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    val chunk = if (bytesRead == CHUNK_SIZE) buffer else buffer.copyOf(bytesRead)
                    emit(UploadRequest.newBuilder().setChunk(ByteString.copyFrom(chunk)).build())
                }
            } ?: throw Exception("Cannot open stream for ${file.uri}")
        }

        return fileStub.upload(requestFlow).hash
    }

    override suspend fun downloadTrack(hash: String, filename: String, destinationFolder: DocumentFile): DocumentFile {
        Log.d(TAG, "📥 Downloading: $filename (hash: $hash)")

        val request = DownloadRequest.newBuilder().setHash(hash).build()

        // Определяем MIME-тип по расширению файла
        val mimeType = when (filename.substringAfterLast('.', "").lowercase()) {
            "mp3" -> "audio/mpeg"
            "flac" -> "audio/flac"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "m4a" -> "audio/mp4"
            else -> "application/octet-stream"
        }

        // Проверяем существующий файл с таким же именем
        val existingFile = destinationFolder.findFile(filename)
        if (existingFile != null) {
            Log.w(TAG, "⚠️ File exists: $filename, deleting...")
            existingFile.delete()
        }

        // Создаем новый файл
        val file = destinationFolder.createFile(mimeType, filename)
            ?: throw Exception("Failed to create file: $filename")

        var totalBytesReceived = 0L
        var chunksCount = 0
        var streamCompleted = false

        try {
            context.contentResolver.openOutputStream(file.uri, "wt")?.buffered()?.use { outputStream ->
                fileStub.download(request).collect { response ->
                    val chunkData = response.chunk.toByteArray()
                    outputStream.write(chunkData)

                    totalBytesReceived += chunkData.size
                    chunksCount++

                    // Логируем прогресс каждые 100 чанков
                    if (chunksCount % 100 == 0) {
                        Log.d(TAG, "   Progress: ${totalBytesReceived / 1024 / 1024} MB (${chunksCount} chunks)")
                    }
                }
                // Финальный flush
                outputStream.flush()
                streamCompleted = true
            } ?: throw Exception("Failed to open output stream for $filename")

            if (!streamCompleted) {
                throw Exception("Download stream closed prematurely")
            }

            Log.d(TAG, "📊 Download stats:")
            Log.d(TAG, "   Received: $totalBytesReceived bytes ($chunksCount chunks)")

            // Ждем пока файл станет доступен для чтения с правильным размером
            var fileStabilized = false
            var retries = 0
            val maxRetries = 20
            var lastSize = 0L

            while (retries < maxRetries && !fileStabilized) {
                val currentSize = file.length()

                if (currentSize == 0L) {
                    Log.d(TAG, "⏳ Waiting for file to appear, retry $retries/$maxRetries")
                    kotlinx.coroutines.delay(50)
                    retries++
                    continue
                }

                if (currentSize == lastSize) {
                    // Размер не меняется - файл стабилизировался
                    fileStabilized = true
                } else {
                    lastSize = currentSize
                    Log.d(TAG, "⏳ File size: $currentSize bytes, waiting for stabilization...")
                    kotlinx.coroutines.delay(50)
                    retries++
                }
            }

            val actualFileSize = file.length()
            Log.d(TAG, "   File size: $actualFileSize bytes")

            // Проверяем размер файла
            if (actualFileSize == 0L) {
                Log.e(TAG, "❌ Downloaded file is empty!")
                file.delete()
                throw Exception("Downloaded file is empty")
            }

            if (actualFileSize != totalBytesReceived) {
                Log.e(TAG, "❌ Size mismatch!")
                Log.e(TAG, "   Expected: $totalBytesReceived bytes")
                Log.e(TAG, "   Got: $actualFileSize bytes")
                Log.e(TAG, "   Diff: ${totalBytesReceived - actualFileSize} bytes lost")
                file.delete()
                throw Exception("File size mismatch: expected $totalBytesReceived but got $actualFileSize")
            }

            Log.d(TAG, "✅ Downloaded successfully: $filename")
            return file

        } catch (e: Exception) {
            Log.e(TAG, "❌ Download failed: $filename", e)
            if (file.exists()) {
                file.delete()
            }
            throw e
        }
    }

    override suspend fun deleteTrack(hash: String): Boolean {
        val request = DeleteRequest.newBuilder().setHash(hash).build()
        return fileStub.delete(request).success
    }

    companion object {
        private const val TAG = "GrpcTrackApi"
    }
}