package com.datapeice.astolfoplayer.app.presentation.components.topbar

import androidx.annotation.StringRes
import com.datapeice.astolfoplayer.R

enum class Tab(@StringRes val titleResId: Int) {
    Playlists(R.string.playlists),
    Tracks(R.string.tracks),
    Albums(R.string.albums),
    Artists(R.string.artists),
    Genres(R.string.genres),
    Folders(R.string.folders)
}