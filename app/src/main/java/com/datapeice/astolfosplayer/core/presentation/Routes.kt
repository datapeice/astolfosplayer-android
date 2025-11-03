package com.datapeice.astolfosplayer.core.presentation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Routes {
    @Serializable
    data object Setup: Routes
    @Serializable
    data object Player: Routes
}