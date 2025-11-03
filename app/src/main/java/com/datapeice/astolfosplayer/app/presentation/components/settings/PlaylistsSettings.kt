package com.datapeice.astolfosplayer.app.presentation.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Queue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.datapeice.astolfosplayer.R
import com.datapeice.astolfosplayer.app.presentation.components.topbar.ColumnWithCollapsibleTopBar
import com.datapeice.astolfosplayer.core.data.Settings

@Composable
fun PlaylistsSettings(
    settings: Settings,
    onPlaylistPick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var collapseFraction by remember {
        mutableFloatStateOf(0f)
    }

    ColumnWithCollapsibleTopBar(
        topBarContent = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = context.resources.getString(R.string.back)
                )
            }

            Text(
                text = context.resources.getString(R.string.playlists),
                fontSize = lerp(
                    MaterialTheme.typography.titleLarge.fontSize,
                    MaterialTheme.typography.displaySmall.fontSize,
                    collapseFraction
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp)
            )
        },
        collapseFraction = {
            collapseFraction = it
        },
        contentPadding = PaddingValues(horizontal = 28.dp),
        contentHorizontalAlignment = Alignment.CenterHorizontally,
        contentVerticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        val replaceSearchWithFilter by settings.replaceSearchWithFilter.collectAsState()
        SettingSwitch(
            title = context.resources.getString(R.string.filter_instead_of_search),
            supportingText = context.resources.getString(R.string.filter_instead_of_search_explain),
            icon = Icons.Rounded.FilterList,
            isChecked = replaceSearchWithFilter,
            onCheckedChange = {
                settings.updateReplaceSearchWithFilter(it)
            },
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        SettingIconButton(
            title = context.resources.getString(R.string.import_playlist),
            supportingText = context.resources.getString(R.string.import_playlist_explain),
            icon = Icons.Rounded.Queue,
            buttonIcon = Icons.Rounded.Folder,
            buttonContentDescription = context.resources.getString(R.string.import_playlist),
            onButtonClick = onPlaylistPick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}