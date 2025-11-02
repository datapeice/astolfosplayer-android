package com.datapeice.astolfoplayer.app.domain.track

import androidx.compose.ui.util.fastFilter

fun List<Playlist>.filterPlaylists(query: String): List<Playlist> {
    return fastFilter {
        if (query.isBlank()) return@fastFilter true
        it.name?.contains(
            query,
            ignoreCase = true
        ) == true
    }
}