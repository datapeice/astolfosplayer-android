package com.datapeice.astolfoplayer.app.presentation.components.trackinfo

import com.datapeice.astolfoplayer.app.domain.lyrics.Lyrics

data class LyricsControlSheetState(
    val lyricsFromTag: Lyrics? = null,
    val lyricsFromRepository: Lyrics? = null,
    val isWritingToTag: Boolean = false,
    val isReadingFromFile: Boolean = false,
    val isFetchingFromRemote: Boolean = false,
    val isPublishingOnRemote: Boolean = false
)
