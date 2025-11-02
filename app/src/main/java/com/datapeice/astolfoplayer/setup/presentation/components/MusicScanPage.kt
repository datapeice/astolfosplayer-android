package com.datapeice.astolfoplayer.setup.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.datapeice.astolfoplayer.R
import com.datapeice.astolfoplayer.app.presentation.components.settings.MusicScanSettingsContent
import com.datapeice.astolfoplayer.core.data.MusicScanner
import com.datapeice.astolfoplayer.core.data.Settings

@Composable
fun MusicScanPage(
    settings: Settings,
    musicScanner: MusicScanner,
    foldersWithAudio: Set<String>,
    onScanFoldersClick: () -> Unit,
    onFolderPick: (scan: Boolean) -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(28.dp)
    ) {
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .padding(bottom  = 64.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SetupPageHeader(
                title = context.resources.getString(R.string.music_scan),
                icon = Icons.Rounded.Radar
            )

            MusicScanSettingsContent(
                settings = settings,
                musicScanner = musicScanner,
                foldersWithAudio = foldersWithAudio,
                onFolderPick = onFolderPick,
                onScanFoldersClick = onScanFoldersClick
            )
        }

        Button(
            onClick = onNextClick,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Text(
                text = context.resources.getString(R.string.next)
            )
        }
    }
}