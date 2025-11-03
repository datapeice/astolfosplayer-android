package com.datapeice.astolfosplayer.app.presentation.components.trackinfo

import com.datapeice.astolfosplayer.app.domain.metadata.Metadata

data class ChangesSheetState(
    val isLoadingArt: Boolean = false,
    val isArtFromGallery: Boolean = false,
    val metadata: Metadata = Metadata()
)
