package com.datapeice.astolfoplayer.app.data.repository

import com.datapeice.astolfoplayer.app.domain.lyrics.Lyrics

interface LyricsRepository {
    fun getLyricsByUri(uri: String): Lyrics?
    suspend fun insertLyrics(lyrics: Lyrics)
    suspend fun updateLyrics(lyrics: Lyrics)
    suspend fun deleteLyrics(lyrics: Lyrics)
}