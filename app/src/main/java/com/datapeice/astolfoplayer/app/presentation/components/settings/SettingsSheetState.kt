package com.datapeice.astolfoplayer.app.presentation.components.settings

import com.datapeice.astolfoplayer.EqualizerController
import com.datapeice.astolfoplayer.core.data.MusicScanner
import com.datapeice.astolfoplayer.core.data.Settings

data class SettingsSheetState(
    val settings: Settings,
    val musicScanner: MusicScanner,
    val isShown: Boolean = false,
    val equalizerController: EqualizerController,
    val foldersWithAudio: Set<String> = emptySet()
)
