package com.datapeice.astolfosplayer.app.presentation.components.settings

sealed class SettingOption(
    val title: String,
    val onSelection: () -> Unit,
)