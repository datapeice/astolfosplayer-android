package com.datapeice.astolfosplayer.core.api

import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Интерфейс, который определяет контракты для работы с API аутентификации.
 */
interface AuthApi {
    suspend fun login(serverAddress: String, authRequest: AuthRequest): HttpResponse
    suspend fun register(serverAddress: String, authRequest: AuthRequest): HttpResponse
}

/**
 * Реализация AuthApi с использованием Ktor.
 */
class KtorAuthApi(
    private val httpClient: HttpClient
) : AuthApi {

    override suspend fun login(serverAddress: String, authRequest: AuthRequest): HttpResponse {
        return httpClient.post("$serverAddress/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(authRequest)
        }
    }

    override suspend fun register(serverAddress: String, authRequest: AuthRequest): HttpResponse {
        return httpClient.post("$serverAddress/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(authRequest)
        }
    }
}



