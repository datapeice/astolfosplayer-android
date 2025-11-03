package com.datapeice.astolfosplayer.app.domain.track

import kotlinx.serialization.Serializable

@Serializable
data class Playlist(
    val name: String?,
    val trackList: List<Track>
)
