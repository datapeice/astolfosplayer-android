package com.datapeice.astolfosplayer.app.presentation.components.trackinfo

import com.datapeice.astolfosplayer.app.domain.metadata.MetadataSearchResult

data class InfoSearchSheetState(
    val isLoading: Boolean = false,
    val searchResults: List<MetadataSearchResult> = emptyList()
)