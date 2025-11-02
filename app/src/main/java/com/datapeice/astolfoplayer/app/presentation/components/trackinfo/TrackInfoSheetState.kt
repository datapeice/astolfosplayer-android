package com.datapeice.astolfoplayer.app.presentation.components.trackinfo

import com.datapeice.astolfoplayer.app.domain.track.Track

data class TrackInfoSheetState(
    val isShown: Boolean = false,
    val track: Track? = null,
    val showRisksOfMetadataEditingDialog: Boolean = true,
    val isCoverArtEditable: Boolean = true,
    val infoSearchSheetState: InfoSearchSheetState = InfoSearchSheetState(),
    val changesSheetState: ChangesSheetState = ChangesSheetState(),
    val manualInfoEditSheetState: ManualInfoEditSheetState = ManualInfoEditSheetState(),
    val lyricsControlSheetState: LyricsControlSheetState = LyricsControlSheetState()
)
