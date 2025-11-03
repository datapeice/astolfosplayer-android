package com.datapeice.astolfosplayer.app.domain.metadata

data class MetadataSearchResult(
    val id: String,
    val title: String,
    val artist: String,
    val albumId: String,
    val album: String,
    val albumArtist: String,
    val trackNumber: String? = null,
    val description: String? = null,
    val albumDescription: String? = null,
    val year: String? = null,
    val genres: List<String>? = null
)
