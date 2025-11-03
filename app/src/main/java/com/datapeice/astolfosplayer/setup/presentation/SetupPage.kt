package com.datapeice.astolfosplayer.setup.presentation

import kotlinx.serialization.Serializable

@Serializable
sealed interface SetupPage {
    @Serializable
    data object Welcome : SetupPage
    @Serializable
    data object AudioPermission : SetupPage
    @Serializable
    data object ServerSetup : SetupPage
    @Serializable
    data object MusicScan : SetupPage
}