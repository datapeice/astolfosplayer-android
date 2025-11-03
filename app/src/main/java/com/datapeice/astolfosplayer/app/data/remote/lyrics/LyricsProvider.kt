package com.datapeice.astolfosplayer.app.data.remote.lyrics

import com.datapeice.astolfosplayer.app.domain.lyrics.Lyrics
import com.datapeice.astolfosplayer.app.domain.result.DataError
import com.datapeice.astolfosplayer.app.domain.result.Result
import com.datapeice.astolfosplayer.app.domain.track.Track

interface LyricsProvider {
    suspend fun getLyrics(track: Track): Result<Lyrics, DataError.Network>
    suspend fun postLyrics(track: Track, lyrics: Lyrics): Result<Unit, DataError.Network>
}