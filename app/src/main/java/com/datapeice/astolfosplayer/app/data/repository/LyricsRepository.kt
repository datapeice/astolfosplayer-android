package com.datapeice.astolfosplayer.app.data.repository

import com.datapeice.astolfosplayer.app.domain.lyrics.Lyrics

interface LyricsRepository {
    // Добавляем suspend, чтобы можно было безопасно читать из БД в IO потоке
    suspend fun getLyricsByUri(uri: String): Lyrics?

    suspend fun insertLyrics(lyrics: Lyrics)

    suspend fun updateLyrics(lyrics: Lyrics)

    suspend fun deleteLyrics(lyrics: Lyrics)
}