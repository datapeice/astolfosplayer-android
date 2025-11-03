package com.datapeice.astolfosplayer.app.domain.track

import androidx.compose.ui.util.fastFilter

fun List<Track>.filterTracks(query: String): List<Track> {
    return fastFilter {
        if (query.isBlank()) return@fastFilter true

        buildString {
            append(it.title + ' ')
            append(it.album + ' ')
            append(it.artist + ' ')
            append(it.albumArtist + ' ')
            append(it.genre + ' ')
            append(it.year + ' ')
        }.contains(query, ignoreCase = true)
    }
}