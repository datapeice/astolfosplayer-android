package com.datapeice.astolfosplayer.core.api

import android.content.Context
import android.util.Log
import androidx.compose.ui.input.key.key
import androidx.documentfile.provider.DocumentFile
import com.datapeice.astolfosplayer.core.data.Settings
import com.datapeice.astolfosplayer.core.utils.FileHasher
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import java.io.InputStream

/**
 * Интерфейс для работы с API синхронизации.
 */
interface SyncApi {
    suspend fun performSync(
        localFolder: DocumentFile,
        // Лямбда теперь принимает текущий шаг, общее число шагов и сообщение
        onProgress: (current: Int, total: Int, message: String) -> Unit
    )
}

/**
 * Реализация SyncApi с использованием Ktor.
 */
class KtorSyncApi(
    private val httpClient: HttpClient,
    private val settings: Settings,
    private val trackApi: TrackApi,
    private val context: Context // Зависимость от контекста
) : SyncApi {

    private val serverAddress: String get() = settings.serverAddress

    override suspend fun performSync(
        localFolder: DocumentFile,
        onProgress: (current: Int, total: Int, message: String) -> Unit
    ) {
        var currentStep = 0

        // --- 1. Получаем статус с сервера ---
        onProgress(currentStep, 1, "Получение статуса с сервера...")
        val serverFiles = try {
            httpClient.get("$serverAddress/api/sync/status") {
                bearerAuth(settings.accessToken)
            }.body<List<SyncStatusItem>>()
        } catch (e: Exception) {
            onProgress(0, 1, "Ошибка: Не удалось получить статус. ${e.message}")
            return
        }
        val serverHashes = serverFiles.associate { it.fileHash to it.filename }

        // --- 2. Анализируем локальные файлы ---
        onProgress(currentStep, 1, "Анализ локальных файлов...")
        val localFiles = localFolder.listFiles().filter { it.name?.endsWith(".mp3", true) == true }
        val localFileHashes = mutableMapOf<String, DocumentFile>()
        for (file in localFiles) {
            context.contentResolver.openInputStream(file.uri)?.use { inputStream ->
                val hash = FileHasher.calculateSha256(inputStream)
                if (hash != null) {
                    localFileHashes[hash] = file
                }
            }
        }

        // --- 3. Определяем, что нужно загрузить и скачать ---
        val filesToUpload = localFileHashes.filter { it.key !in serverHashes.keys }.values.toList()
        val filesToDownload = serverFiles.filter { it.fileHash !in localFileHashes.keys }
        val totalSteps = filesToUpload.size + filesToDownload.size
        onProgress(currentStep, totalSteps, "Подготовка к синхронизации...")

        // --- 4. Загружаем новые файлы на сервер ---
        if (filesToUpload.isNotEmpty()) {
            onProgress(currentStep, totalSteps, "Загрузка ${filesToUpload.size} новых файлов...")
            try {
                // Используем batch-upload, как в Python-скрипте
                httpClient.post("$serverAddress/api/sync/batch-upload") {
                    bearerAuth(settings.accessToken)
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                filesToUpload.forEach { file ->
                                    val fileBytes = context.contentResolver.openInputStream(file.uri)?.use { it.readBytes() }
                                    if (fileBytes != null) {
                                        append("files", fileBytes, Headers.build {
                                            append(HttpHeaders.ContentType, "audio/mpeg")
                                            append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                                        })
                                    }
                                }
                            }
                        )
                    )
                }
                currentStep += filesToUpload.size
                onProgress(currentStep, totalSteps, "Пакетная загрузка завершена.")

            } catch (e: Exception) {
                onProgress(currentStep, totalSteps, "Ошибка при массовой загрузке: ${e.message}")
            }
        }

        // --- 5. Скачиваем недостающие файлы с сервера ---
        var allServerTracks: List<Track>? = null
        for ((fileHash, filename) in filesToDownload) {
            currentStep++
            onProgress(currentStep, totalSteps, "Поиск файла: $filename...")
            try {
                if (allServerTracks == null) {
                    allServerTracks = trackApi.getAllTracks()
                }
                val trackToDownload = allServerTracks.find { it.fileHash == fileHash }
                if (trackToDownload == null) {
                    onProgress(currentStep, totalSteps, "Не найден ID для: $filename")
                    continue
                }

                onProgress(currentStep, totalSteps, "Скачивание: $filename...")
                val downloadedFileBytes = trackApi.downloadTrackFile(trackToDownload.id)
                val newFile = localFolder.createFile("audio/mpeg", filename)
                if (newFile != null) {
                    context.contentResolver.openOutputStream(newFile.uri)?.use {
                        it.write(downloadedFileBytes)
                    }
                }
            } catch (e: Exception) {
                onProgress(currentStep, totalSteps, "Ошибка скачивания: $filename")
                Log.e("SyncApi", "Ошибка скачивания $filename", e)
            }
        }

        onProgress(totalSteps, totalSteps, "Синхронизация завершена.")
    }
}
