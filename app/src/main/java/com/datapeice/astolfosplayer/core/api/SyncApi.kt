package com.datapeice.astolfosplayer.core.api

import android.util.Log
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
import io.ktor.client.statement.readBytes
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import java.io.File

/**
 * Интерфейс для работы с API синхронизации.
 */
interface SyncApi {
    suspend fun getSyncStatus(): List<SyncStatusItem>
    suspend fun batchUpload(files: List<File>)
    suspend fun performSync(
        localFolder: File,
        onProgress: (message: String) -> Unit
    )
}

/**
 * Реализация SyncApi с использованием Ktor.
 */
class KtorSyncApi(
    private val httpClient: HttpClient,
    private val settings: Settings,
    private val trackApi: TrackApi // Добавили зависимость от TrackApi
) : SyncApi {

    private val serverAddress: String
        get() = settings.serverAddress

    override suspend fun getSyncStatus(): List<SyncStatusItem> {
        return httpClient.get("$serverAddress/api/sync/status") {
            bearerAuth(settings.accessToken)
        }.body()
    }

    override suspend fun batchUpload(files: List<File>) {
        httpClient.post("$serverAddress/api/sync/batch-upload") {
            bearerAuth(settings.accessToken)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        files.forEach { file ->
                            append("files", file.readBytes(), Headers.build {
                                append(HttpHeaders.ContentType, "audio/mpeg") // Укажите правильный тип файла
                                append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                            })
                        }
                    }
                )
            )
        }
    }

    /**
     * Реализация полной логики синхронизации, аналогично Python-скрипту.
     */
    /**
     * Реализация полной логики синхронизации, аналогично Python-скрипту.
     */
    override suspend fun performSync(
        localFolder: File,
        onProgress: (message: String) -> Unit
    ) {
        // --- 1. Получаем статус с сервера ---
        onProgress("Получение статуса с сервера...")
        val serverFiles = try {
            getSyncStatus()
        } catch (e: Exception) {
            onProgress("Ошибка: Не удалось получить статус с сервера. ${e.message}")
            return
        }
        val serverHashes = serverFiles.associateBy({ it.fileHash }, { it.filename })
        onProgress("На сервере ${serverHashes.size} файлов.")

        // --- 2. Загрузка новых файлов на сервер ---
        onProgress("Поиск новых локальных файлов для загрузки...")
        val localFiles = localFolder.listFiles { _, name -> name.endsWith(".mp3", true) } ?: emptyArray()

        for (localFile in localFiles) {
            val fileHash = FileHasher.calculateSha256(localFile)
            if (fileHash == null) {
                onProgress("Не удалось рассчитать хэш для ${localFile.name}")
                continue
            }

            if (fileHash !in serverHashes) {
                try {
                    onProgress("Загрузка: ${localFile.name}...")
                    trackApi.uploadTrack(localFile, localFile.nameWithoutExtension, null, null, null)
                    onProgress("Загружено: ${localFile.name}")
                } catch (e: Exception) {
                    onProgress("Ошибка загрузки ${localFile.name}: ${e.message}")
                }
            } else {
                Log.d("SyncApi", "Файл ${localFile.name} уже есть на сервере.")
            }
        }

        // --- 3. Скачивание файлов с сервера, которых нет локально ---
        onProgress("Поиск новых серверных файлов для скачивания...")
        val localHashes = localFiles.mapNotNull { file ->
            FileHasher.calculateSha256(file)?.let { hash -> hash to file.name }
        }.toMap()

        // --- ИЗМЕНЕНИЕ 1: Выносим получение всех треков из lazy ---
        // Мы получим их только один раз, если они действительно понадобятся.
        var allServerTracks: List<Track>? = null

        for ((fileHash, filename) in serverHashes) {
            if (fileHash !in localHashes) {
                try {
                    // --- ИЗМЕНЕНИЕ 2: Убираем поиск по 'id', так как его нет в SyncStatusItem ---
                    // Просто ищем трек по хэшу или имени файла.
                    // Сначала попробуем найти в полном списке, если он уже загружен
                    var trackId = allServerTracks?.find { it.fileHash == fileHash || it.filename == filename }?.id

                    // Если не нашли и список еще не был загружен, загружаем его
                    if (trackId == null && allServerTracks == null) {
                        onProgress("Загрузка полного списка треков с сервера...")
                        allServerTracks = trackApi.getAllTracks()
                        // Повторяем поиск
                        trackId = allServerTracks?.find { it.fileHash == fileHash || it.filename == filename }?.id
                    }

                    // --- ИЗМЕНЕНИЕ 3: Проверяем trackId на null ---
                    if (trackId == null) {
                        onProgress("Не удалось найти ID для скачивания файла: $filename")
                        continue
                    }

                    onProgress("Скачивание: $filename...")
                    val fileBytes = trackApi.downloadTrackFile(trackId) // Теперь trackId это точно String
                    val destinationFile = File(localFolder, filename)
                    destinationFile.writeBytes(fileBytes)
                    onProgress("Скачано: $filename")

                } catch (e: Exception) {
                    onProgress("Ошибка скачивания $filename: ${e.message}")
                }
            } else {
                Log.d("SyncApi", "Файл $filename уже есть локально.")
            }
        }
        onProgress("Синхронизация завершена.")
    }

}
