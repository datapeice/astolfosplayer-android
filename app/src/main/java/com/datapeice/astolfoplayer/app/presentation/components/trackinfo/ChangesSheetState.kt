package com.datapeice.astolfoplayer.app.presentation.components.trackinfo

import com.datapeice.astolfoplayer.app.domain.metadata.Metadata

data class ChangesSheetState(
    val isLoadingArt: Boolean = false,
    val isArtFromGallery: Boolean = false,
    val metadata: Metadata = Metadata()
)
