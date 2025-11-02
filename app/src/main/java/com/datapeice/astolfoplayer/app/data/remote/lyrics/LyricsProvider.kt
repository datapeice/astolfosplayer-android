package com.datapeice.astolfoplayer.app.data.remote.lyrics

import com.datapeice.astolfoplayer.app.domain.lyrics.Lyrics
import com.datapeice.astolfoplayer.app.domain.result.DataError
import com.datapeice.astolfoplayer.app.domain.result.Result
import com.datapeice.astolfoplayer.app.domain.track.Track

interface LyricsProvider {
    suspend fun getLyrics(track: Track): Result<Lyrics, DataError.Network>
    suspend fun postLyrics(track: Track, lyrics: Lyrics): Result<Unit, DataError.Network>
}