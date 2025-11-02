package com.datapeice.astolfoplayer.app.data

import com.datapeice.astolfoplayer.app.domain.lyrics.Lyrics
import com.datapeice.astolfoplayer.app.domain.result.DataError
import com.datapeice.astolfoplayer.app.domain.result.Result
import com.datapeice.astolfoplayer.app.domain.track.Track

interface LyricsReader {
    fun readFromTag(track: Track): Result<Lyrics?, DataError.Local>
}