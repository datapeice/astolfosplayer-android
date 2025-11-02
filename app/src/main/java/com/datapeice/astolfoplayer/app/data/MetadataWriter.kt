package com.datapeice.astolfoplayer.app.data

import android.content.IntentSender
import com.datapeice.astolfoplayer.app.domain.metadata.Metadata
import com.datapeice.astolfoplayer.app.domain.result.DataError
import com.datapeice.astolfoplayer.app.domain.result.Result
import com.datapeice.astolfoplayer.app.domain.track.Track

interface MetadataWriter {
    val unsupportedArtworkEditFormats: List<String>
    fun writeMetadata(track: Track, metadata: Metadata, onSecurityError: (IntentSender) -> Unit): Result<Unit, DataError.Local>
}