package com.datapeice.astolfosplayer.app.data

import com.datapeice.astolfosplayer.app.domain.lyrics.Lyrics
import com.datapeice.astolfosplayer.app.domain.result.DataError
import com.datapeice.astolfosplayer.app.domain.result.Result
import com.datapeice.astolfosplayer.app.domain.track.Track

interface LyricsReader {
    fun readFromTag(track: Track): Result<Lyrics?, DataError.Local>
}