package com.datapeice.astolfosplayer.app.data

import android.content.IntentSender
import com.datapeice.astolfosplayer.app.domain.metadata.Metadata
import com.datapeice.astolfosplayer.app.domain.result.DataError
import com.datapeice.astolfosplayer.app.domain.result.Result
import com.datapeice.astolfosplayer.app.domain.track.Track

interface MetadataWriter {
    val unsupportedArtworkEditFormats: List<String>
    fun writeMetadata(track: Track, metadata: Metadata, onSecurityError: (IntentSender) -> Unit): Result<Unit, DataError.Local>
}