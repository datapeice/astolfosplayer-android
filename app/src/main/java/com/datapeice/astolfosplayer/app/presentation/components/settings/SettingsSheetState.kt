package com.datapeice.astolfosplayer.app.presentation.components.settings

import com.datapeice.astolfosplayer.EqualizerController
import com.datapeice.astolfosplayer.core.data.MusicScanner
import com.datapeice.astolfosplayer.core.data.Settings
import com.datapeice.astolfosplayer.setup.presentation.SetupViewModel

data class SettingsSheetState(
    val settings: Settings,
    val musicScanner: MusicScanner,
    val isShown: Boolean = false,
    val equalizerController: EqualizerController,
    val foldersWithAudio: Set<String> = emptySet(),
    val setupViewModel: SetupViewModel // <-- ДОБАВЬТЕ ЭТУ СТРОКУ)
)
