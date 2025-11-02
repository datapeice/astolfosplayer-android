package com.datapeice.astolfoplayer.app.presentation.components.settings

import androidx.compose.ui.graphics.vector.ImageVector

data class SettingSegmentOption(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit
)