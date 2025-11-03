package com.datapeice.astolfosplayer.core.api


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- Модели для аутентификации ---

@Serializable
data class AuthRequest(
    val username: String,
    val password: String
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String
)

// --- Модели для треков ---

@Serializable
data class Track(
    val id: String, // Предполагаем, что у трека есть ID
    @SerialName("file_hash") val fileHash: String,
    val filename: String,
    val title: String?,
    val artist: String?,
    val album: String?,
    val duration: Int?
)

// --- Модели для синхронизации ---

@Serializable
data class SyncStatusItem(
    @SerialName("file_hash") val fileHash: String,
    val filename: String,
    @SerialName("uploaded_at") val uploadedAt: String
)


