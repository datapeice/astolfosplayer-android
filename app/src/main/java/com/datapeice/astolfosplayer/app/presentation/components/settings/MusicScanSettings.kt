package com.datapeice.astolfosplayer.app.presentation.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Timelapse
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.datapeice.astolfosplayer.R
import com.datapeice.astolfosplayer.app.presentation.components.topbar.ColumnWithCollapsibleTopBar
import com.datapeice.astolfosplayer.app.presentation.components.settings.SettingFolderItem
import com.datapeice.astolfosplayer.core.data.MusicScanner
import com.datapeice.astolfosplayer.core.data.Settings
import kotlinx.coroutines.launch

/**
 * Основной Composable для экрана настроек сканирования.
 */
@Composable
fun MusicScanSettings(
    settings: Settings,
    musicScanner: MusicScanner,
    onFolderPick: () -> Unit,
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
                text = context.resources.getString(R.string.music_scan),
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
        contentVerticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        MusicScanSettingsContent(
            settings = settings,
            musicScanner = musicScanner,
            onFolderPick = onFolderPick
        )
    }
}

/**
 * Приватный Composable, который содержит всю логику отображения настроек сканирования.
 */
@Composable
private fun MusicScanSettingsContent(
    settings: Settings,
    musicScanner: MusicScanner,
    onFolderPick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier
    ) {
        val context = LocalContext.current

        // Получаем ОДНУ папку для сканирования из настроек
        val mainScanFolder by settings.extraScanFolders.collectAsState()
        val folderPath = mainScanFolder.firstOrNull()

        // --- Блок для выбора основной папки ---
        SettingFolderItem(
            title = context.resources.getString(R.string.folders_with_audio), // TODO: stringResource
            folderPath = folderPath,
            onFolderClick = onFolderPick
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // --- Ваши существующие настройки ---

        var ignoreShortTracks by remember {
            mutableStateOf(settings.ignoreShortTracks)
        }
        SettingSwitch(
            title = context.resources.getString(R.string.ignore_short_tracks),
            supportingText = context.resources.getString(R.string.ignore_short_tracks_explain),
            icon = Icons.Rounded.Timelapse,
            isChecked = ignoreShortTracks,
            onCheckedChange = {
                ignoreShortTracks = it
                settings.ignoreShortTracks = it
            },
            modifier = Modifier.fillMaxWidth()
        )

        val scanOnAppLaunch by settings.scanOnAppLaunch.collectAsState()
        SettingSwitch(
            title = context.resources.getString(R.string.refresh_on_app_launch),
            supportingText = context.resources.getString(R.string.refresh_on_app_launch_explain),
            icon = Icons.Rounded.Autorenew,
            isChecked = scanOnAppLaunch,
            onCheckedChange = settings::updateScanOnAppLaunch
        )

        val coroutineScope = rememberCoroutineScope()
        SettingIconButton(
            title = context.resources.getString(R.string.refresh),
            supportingText = context.resources.getString(R.string.refresh_explain),
            icon = Icons.Rounded.Storage,
            buttonIcon = Icons.Rounded.Refresh,
            buttonContentDescription = context.resources.getString(R.string.refresh_explain),
            onButtonClick = {
                coroutineScope.launch {
                    musicScanner.refreshMedia()
                }
            }
        )

        SettingIconButton(
            title = context.resources.getString(R.string.scan_folder),
            supportingText = context.resources.getString(R.string.rescan_folder), // TODO: stringResource
            icon = Icons.Rounded.Folder,
            buttonIcon = Icons.Rounded.Radar,
            buttonContentDescription = context.resources.getString(R.string.scan_folder_explain),
            onButtonClick = {
                coroutineScope.launch {
                    // --- ИСПРАВЛЕНО ЗДЕСЬ ---
                    // Вызываем общий метод refreshMedia, который использует папки из настроек
                    musicScanner.refreshMedia()
                }
            }
        )
    }
}

/**
 * Новый Composable для отображения и выбора одной папки.
 */
/**
 * Новый Composable для отображения и выбора одной папки.
 */
/**
 * Новый Composable для отображения и выбора одной папки, стилизованный под остальные настройки.
 */
/**
 * Новый Composable для отображения и выбора одной папки, стилизованный под остальные настройки.
 */
@Composable
private fun SettingFolderItem(
    title: String,
    folderPath: String?,
    onFolderClick: () -> Unit
) {
    val context = LocalContext.current
    val displayPath = if (folderPath.isNullOrBlank()) {
        context.resources.getString(R.string.folder_not_selected) // TODO: stringResource
    } else {
        // Простая логика для отображения имени папки
        folderPath.substringAfterLast('/').ifEmpty { folderPath }
    }

    // Используем наш новый, правильный компонент
    SettingClickableItem(
        title = title,
        supportingText = displayPath,
        icon = Icons.Rounded.Folder,
        onClick = onFolderClick
    )
}
