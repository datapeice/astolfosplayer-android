package com.datapeice.astolfosplayer.core.api

import android.content.Context
import android.util.Log
import com.datapeice.astolfosplayer.core.data.Settings
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

class GrpcChannelProvider(
    private val context: Context,
    private val settings: Settings
) {
    // Порты ваших сервисов из docker-compose
    private val AUTH_PORT = 50051
    private val FILE_PORT = 50052
    private val SYNC_PORT = 50053

    // Каналы для каждого сервиса
    val authChannel: ManagedChannel
        get() = createChannel(AUTH_PORT)

    val fileChannel: ManagedChannel
        get() = createChannel(FILE_PORT)

    val syncChannel: ManagedChannel
        get() = createChannel(SYNC_PORT)

    private fun createChannel(port: Int): ManagedChannel {
        // ИСПРАВЛЕНО: Читаем строку напрямую из settings (это String, не Flow)
        var rawAddress = settings.serverAddress

        // Если в настройках пусто, пробуем адрес эмулятора
        if (rawAddress.isBlank()) {
            rawAddress = "10.0.2.2"
        }

        // ОЧИСТКА АДРЕСА: убираем http, порты и лишние символы
        val host = rawAddress
            .replace("http://", "")
            .replace("https://", "")
            .substringBefore(":") // Отрезаем порт, если пользователь его ввел
            .substringBefore("/")
            .trim()

        Log.d("GrpcChannelProvider", "Creating channel to: $host:$port (Raw input: '$rawAddress')")

        return ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext() // Разрешает работу без SSL/TLS (критично для локального сервера)
            .executor(Dispatchers.IO.asExecutor())
            .build()
    }
}