package com.datapeice.astolfosplayer.core.api

import android.content.Context
import android.util.Log
import androidx.compose.ui.res.stringResource
import androidx.documentfile.provider.DocumentFile
import com.datapeice.astolfosplayer.R
import com.datapeice.astolfosplayer.core.data.Settings
import com.datapeice.astolfosplayer.core.utils.FileHasher
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpStatement
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentRangeHeaderValue
import io.ktor.utils.io.jvm.javaio.copyTo
import io.ktor.utils.io.streams.asInput

interface SyncApi {
    suspend fun performSync(
        localFolder: DocumentFile,
        onProgress: (current: Int, total: Int, message: String) -> Unit,
        onComplete: suspend () -> Unit = {}
    )
}

class KtorSyncApi(
    private val httpClient: HttpClient,
    private val settings: Settings,
    private val trackApi: TrackApi,
    private val context: Context
) : SyncApi {

    private val serverAddress: String get() = settings.serverAddress

    override suspend fun performSync(
        localFolder: DocumentFile,
        onProgress: (current: Int, total: Int, message: String) -> Unit,
        onComplete: suspend () -> Unit
    ) {
        var currentStep = 0

        onProgress(currentStep, 1, context.getString( R.string.getting_status))
        val serverFiles = try {
            httpClient.get("$serverAddress/api/sync/status") {
                bearerAuth(settings.accessToken)
            }.body<List<SyncStatusItem>>()
        } catch (e: Exception) {
            onProgress(0, 1, context.getString(R.string.error_message, e.message ?: ""))
            // Log.e("SyncApi", "Ошибка получения статуса", e)
            return
        }
        val serverHashes = serverFiles.associate { it.fileHash to it.filename }

        onProgress(currentStep, 1, context.getString(R.string.analyzing_local_files))
        val localFiles = localFolder.listFiles().filter {
            it.name?.endsWith(".mp3", true) == true ||
                    it.name?.endsWith(".flac", true) == true
        }
        val localFileHashes = mutableMapOf<String, DocumentFile>()
        for (file in localFiles) {
            context.contentResolver.openInputStream(file.uri)?.use { inputStream ->
                val hash = FileHasher.calculateSha256(inputStream)
                if (hash != null) {
                    localFileHashes[hash] = file
                }
            }
        }

        val filesToUpload = localFileHashes.filter { it.key !in serverHashes.keys }.values.toList()
        val filesToDownload = serverFiles.filter { it.fileHash !in localFileHashes.keys }
        val totalSteps = filesToUpload.size + filesToDownload.size

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
                context.contentResolver.openInputStream(file.uri)?.use { inputStream ->
                    httpClient.submitFormWithBinaryData(
                        url = "$serverAddress/api/tracks/upload",
                        formData = formData {
                            append("file", inputStream.asInput(), Headers.build {
                                append(HttpHeaders.ContentType,
                                    if (fileName.endsWith(".flac", true)) "audio/flac" else "audio/mpeg")
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            })
                            append("title", fileName.substringBeforeLast("."))
                        }
                    ) {
                        bearerAuth(settings.accessToken)
                    }
                }
                onProgress(currentStep, totalSteps, context.getString(R.string.file_uploaded, fileName))
            } catch (e: Exception) {
                onProgress(currentStep, totalSteps, context.getString(R.string.upload_error, fileName))
                //Log.e("SyncApi", "Ошибка загрузки $fileName", e)
            }
        }

        // --- Скачиваем файлы через HttpStatement ---
        var allServerTracks: List<Track>? = null
        for ((index, syncItem) in filesToDownload.withIndex()) {
            currentStep++
            val filename = syncItem.filename
            onProgress(currentStep, totalSteps, context.getString(R.string.downloading_file_progress, index + 1, filesToDownload.size, filename))

            try {
                if (allServerTracks == null) {
                    allServerTracks = trackApi.getAllTracks()
                }
                val trackToDownload = allServerTracks.find { it.fileHash == syncItem.fileHash }
                if (trackToDownload == null) {
                    onProgress(currentStep, totalSteps, context.getString(R.string.id_not_found_for_file, filename))
                    continue
                }

                val mimeType = if (filename.endsWith(".flac", true)) "audio/flac" else "audio/mpeg"
                val newFile = localFolder.createFile(mimeType, filename)

                if (newFile != null) {
                    var totalWritten = 0L

                    // Используем HttpStatement для прямого стриминга без буферизации
                    httpClient.prepareGet("$serverAddress/api/tracks/${trackToDownload.id}/file") {
                        bearerAuth(settings.accessToken)
                    }.execute { response ->
                        context.contentResolver.openOutputStream(newFile.uri)?.use { output ->
                            totalWritten = response.bodyAsChannel().copyTo(output)
                        }
                    }

                    //Log.d("SyncApi", "Downloaded $filename: $totalWritten bytes")
                    onProgress(currentStep, totalSteps, context.getString(R.string.file_downloaded, filename))
                } else {
                    onProgress(currentStep, totalSteps, context.getString(R.string.failed_to_create_file, filename))
                }

            } catch (e: Exception) {
                onProgress(currentStep, totalSteps, context.getString(R.string.error_file, filename))
                //Log.e("SyncApi", "Ошибка скачивания $filename", e)
            }
        }

        onProgress(totalSteps, totalSteps, context.getString(R.string.updating_library))
        try {
            onComplete()
            onProgress(totalSteps, totalSteps, context.getString(R.string.synchronization_completed))
        } catch (e: Exception) {
            //Log.e("SyncApi", "Ошибка обновления библиотеки", e)
        }
    }
}
