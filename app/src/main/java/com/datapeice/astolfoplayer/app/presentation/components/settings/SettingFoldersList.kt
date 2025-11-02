package com.datapeice.astolfoplayer.app.presentation.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach

@Composable
fun SettingFoldersPicked(
    title: String,
    paths: List<String>,
    addFolderContentDescription: String,
    removeFolderContentDescription: String,
    onPickFolderClick: () -> Unit,
    onRemoveFolderClick: (path: String) -> Unit,
    availableOptionsTitle: String,
    availableOptions: List<String>,
    addFolderFromOptionsContentDescription: String,
    onAddFolderFromOptionsClick: ((path: String) -> Unit),
    onScanClick: (() -> Unit),
    scanContentDescription: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(ShapeDefaults.ExtraLarge)
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
            .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp)
            )

            IconButton(
                onClick = onPickFolderClick
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = addFolderContentDescription
                )
            }
        }

        paths.fastForEach { path ->
            Column {
                FolderEntry(
                    path = path,
                    onRemoveFolderClick = {
                        onRemoveFolderClick(path)
                    },
                    removeFolderContentDescription = removeFolderContentDescription,
                    modifier = Modifier
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = availableOptionsTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp)
            )

            IconButton(
                onClick = onScanClick
            ) {
                Icon(
                    imageVector = Icons.Rounded.Radar,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = scanContentDescription
                )
            }
        }

        availableOptions.fastFilter { it !in paths }.fastForEach { path ->
            Column {
                FolderOption(
                    path = path,
                    onAddClick = {
                        onAddFolderFromOptionsClick(path)
                    },
                    addFolderFromOptionsContentDescription = addFolderFromOptionsContentDescription,
                    modifier = Modifier
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun FolderEntry(
    path: String,
    onRemoveFolderClick: () -> Unit,
    removeFolderContentDescription: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Rounded.Folder,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null
            )

            Column(
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
                    .weight(1f)
            ) {
                Text(
                    text = path.substringAfterLast('/'),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = path,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(
            onClick = onRemoveFolderClick
        ) {
            Icon(
                imageVector = Icons.Rounded.Remove,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = "$removeFolderContentDescription $path"
            )
        }
    }
}

@Composable
fun FolderOption(
    path: String,
    onAddClick: () -> Unit,
    addFolderFromOptionsContentDescription: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Rounded.Folder,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null
            )

            Column(
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
                    .weight(1f)
            ) {
                Text(
                    text = path.substringAfterLast('/'),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = path,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(
            onClick = onAddClick
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = "$addFolderFromOptionsContentDescription $path"
            )
        }
    }
}