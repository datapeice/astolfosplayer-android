package com.datapeice.astolfosplayer.app.presentation.components.trackinfo

import kotlinx.serialization.Serializable

@Serializable
sealed interface TrackInfoRoutes {
    @Serializable
    data object Info: TrackInfoRoutes
    @Serializable
    data object InfoSearch: TrackInfoRoutes
    @Serializable
    data object Changes: TrackInfoRoutes
    @Serializable
    data object ManualEditing: TrackInfoRoutes
    @Serializable
    data object LyricsControl: TrackInfoRoutes
}