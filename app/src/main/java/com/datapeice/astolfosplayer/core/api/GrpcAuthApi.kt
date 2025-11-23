package com.datapeice.astolfosplayer.core.api

import android.content.Context
import com.datapeice.astolfosplayer.core.data.Settings
import auth.Auth.LoginRequest
import auth.Auth.LoginResponse
import auth.Auth.RegisterRequest
import auth.Auth.RegisterResponse
import auth.AuthServiceGrpcKt.AuthServiceCoroutineStub

/**
 * Интерфейс для работы с аутентификацией через gRPC.
 */
interface AuthApi {
    suspend fun login(username: String, password: String): LoginResponse
    suspend fun register(username: String, password: String, securityKey: String): RegisterResponse
}

/**
 * Реализация AuthApi с использованием gRPC.
 */
class GrpcAuthApi(
    private val context: Context,
    private val settings: Settings,
    private val channelProvider: GrpcChannelProvider
) : AuthApi {

    private val authStub by lazy { AuthServiceCoroutineStub(channelProvider.authChannel) }

    override suspend fun login(username: String, password: String): LoginResponse {
        val request = LoginRequest.newBuilder()
            .setUsername(username)
            .setPassword(password)
            .build()

        return authStub.login(request)
    }

    override suspend fun register(username: String, password: String, securityKey: String): RegisterResponse {
        val request = RegisterRequest.newBuilder()
            .setUsername(username)
            .setPassword(password)
            .setSecurityKey(securityKey)
            .build()

        return authStub.register(request)
    }
}
