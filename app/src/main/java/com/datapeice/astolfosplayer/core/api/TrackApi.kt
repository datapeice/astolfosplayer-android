package com.datapeice.astolfosplayer.core.api

import com.datapeice.astolfosplayer.core.data.Settings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import java.io.File

/**
 * Интерфейс для работы с треками на сервере.
 */
interface TrackApi {
    suspend fun getAllTracks(): List<Track>
    suspend fun getTrack(trackId: String): Track
    suspend fun downloadTrackFile(trackId: String): ByteArray
    suspend fun uploadTrack(
        file: File,
        title: String?,
        artist: String?,
        album: String?,
        duration: Int?
    ): Track

    suspend fun deleteTrack(trackId: String)
}

/**
 * Реализация TrackApi с использованием Ktor.
 */
class KtorTrackApi(
    private val httpClient: HttpClient,
    private val settings: Settings // Нужен для получения адреса и токена
) : TrackApi {

    private val serverAddress: String
        get() = settings.serverAddress

    override suspend fun getAllTracks(): List<Track> {
        return httpClient.get("$serverAddress/api/tracks") {
            bearerAuth(settings.accessToken)
        }.body()
    }

    override suspend fun getTrack(trackId: String): Track {
        return httpClient.get("$serverAddress/api/tracks/$trackId") {
            bearerAuth(settings.accessToken)
        }.body()
    }

    override suspend fun downloadTrackFile(trackId: String): ByteArray {
        return httpClient.get("$serverAddress/api/tracks/$trackId/file") {
            bearerAuth(settings.accessToken)
        }.body()
    }

    override suspend fun uploadTrack(
        file: File,
        title: String?,
        artist: String?,
        album: String?,
        duration: Int?
    ): Track {
        val response = httpClient.post("$serverAddress/api/tracks/upload") {
            bearerAuth(settings.accessToken)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "file",
                            file.readBytes(),
                            Headers.build {
                                append(HttpHeaders.ContentType, "audio/mpeg") // Укажите правильный тип файла
                                append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                            }
                        )
                        // Добавляем опциональные поля, только если они не null
                        title?.let { append("title", it) }
                        artist?.let { append("artist", it) }
                        album?.let { append("album", it) }
                        duration?.let { append("duration", it.toString()) }
                    }
                )
            )
        }
        return response.body()
    }

    override suspend fun deleteTrack(trackId: String) {
        httpClient.delete("$serverAddress/api/tracks/$trackId") {
            bearerAuth(settings.accessToken)
        }
    }
}
