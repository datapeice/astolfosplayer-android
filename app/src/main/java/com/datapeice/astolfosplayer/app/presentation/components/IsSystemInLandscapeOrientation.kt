package com.datapeice.astolfosplayer.app.presentation.components

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun isSystemInLandscapeOrientation(): Boolean {
    val context = LocalContext.current
    return remember(context.resources.configuration.orientation) {
        context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }
}