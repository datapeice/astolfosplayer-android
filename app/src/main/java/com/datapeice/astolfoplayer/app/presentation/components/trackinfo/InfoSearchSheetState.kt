package com.datapeice.astolfoplayer.app.presentation.components.trackinfo

import com.datapeice.astolfoplayer.app.domain.metadata.MetadataSearchResult

data class InfoSearchSheetState(
    val isLoading: Boolean = false,
    val searchResults: List<MetadataSearchResult> = emptyList()
)