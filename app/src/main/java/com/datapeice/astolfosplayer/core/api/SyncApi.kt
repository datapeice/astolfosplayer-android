package com.datapeice.astolfosplayer.core.api

import com.datapeice.astolfosplayer.core.data.Settings
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
import java.io.File

/**
 * Интерфейс для работы с API синхронизации.
 */
interface SyncApi {
    suspend fun getSyncStatus(): List<SyncStatusItem>
    suspend fun batchUpload(files: List<File>)
}

/**
 * Реализация SyncApi с использованием Ktor.
 */
class KtorSyncApi(
    private val httpClient: HttpClient,
    private val settings: Settings
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
}
