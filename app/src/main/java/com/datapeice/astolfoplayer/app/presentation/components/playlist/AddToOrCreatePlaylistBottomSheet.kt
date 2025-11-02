package com.datapeice.astolfoplayer.app.presentation.components.playlist

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.datapeice.astolfoplayer.R
import com.datapeice.astolfoplayer.app.domain.track.Playlist
import com.datapeice.astolfoplayer.app.presentation.components.NothingYet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToOrCreatePlaylistBottomSheet(
    playlists: List<Playlist>,
    createOnly: Boolean,
    onDismissRequest: () -> Unit,
    onCreateClick: (String) -> Unit,
    onPlaylistSelection: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        dragHandle = null,
        modifier = modifier
            .safeDrawingPadding()
    ) {
        val context = LocalContext.current
        var showCreateSheet by remember {
            mutableStateOf(createOnly)
        }
        AnimatedContent(
            targetState = showCreateSheet,
            label = "playlist-sheet-content-animation"
        ) { state ->
            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .padding(top = 16.dp, bottom = 28.dp)
            ) {
                when (state) {
                    true -> {
                        if (!createOnly) {
                            BackHandler {
                                showCreateSheet = false
                            }
                        }

                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (!createOnly) {
                                IconButton(
                                    onClick = {
                                        showCreateSheet = false
                                    },
                                    modifier = Modifier.align(Alignment.CenterStart)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowBackIosNew,
                                        contentDescription = context.resources.getString(R.string.back_to_add_to_playlist)
                                    )
                                }
                            }

                            Text(
                                text = context.resources.getString(R.string.create_playlist),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        var playlistName by remember {
                            mutableStateOf("")
                        }
                        var error by remember {
                            mutableStateOf<String?>(null)
                        }
                        OutlinedTextField(
                            value = playlistName,
                            onValueChange = {
                                playlistName = it.trimStart().filter {
                                    it.isDigit() || it.isLetter() || it == ' '
                                }
                                error = null
                            },
                            singleLine = true,
                            label = {
                                Text(text = context.resources.getString(R.string.name))
                            },
                            placeholder = {
                                Text(text = context.resources.getString(R.string.new_playlist_title_placeholder))
                            },
                            isError = error != null,
                            supportingText = error?.let {
                                {
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            shape = ShapeDefaults.Large,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FilledTonalButton(
                            onClick = {
                                playlistName = playlistName.trim()
                                when {
                                    playlistName.isBlank() -> {
                                        error =
                                            context.resources.getString(R.string.blank_playlist_name_error)
                                    }

                                    playlistName in playlists.map { it.name } -> {
                                        error =
                                            context.resources.getString(R.string.playlist_already_exists_error)
                                    }

                                    else -> {
                                        onCreateClick(playlistName)
                                        if (createOnly) {
                                            onDismissRequest()
                                        } else {
                                            showCreateSheet = false
                                        }
                                    }
                                }
                            },
                            shape = ShapeDefaults.Large,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = context.resources.getString(R.string.create),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    false -> {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = context.resources.getString(R.string.add_to_playlist),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.Center)
                            )

                            IconButton(
                                onClick = {
                                    showCreateSheet = true
                                },
                                modifier = Modifier.align(Alignment.CenterEnd)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = context.resources.getString(R.string.create_playlist)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        if (playlists.isNotEmpty()) {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(150.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                items(
                                    items = playlists,
                                    key = { "${it.name}-${it.trackList}" }
                                ) { playlist ->
                                    PlaylistCard(
                                        title = playlist.name
                                            ?: context.resources.getString(R.string.unknown),
                                        trackCount = playlist.trackList.size,
                                        coverArtPreviewUris = playlist.trackList
                                            .take(4)
                                            .map { it.coverArtUri },
                                        modifier = Modifier
                                            .clip(ShapeDefaults.Large)
                                            .clickable {
                                                onPlaylistSelection(playlist)
                                                onDismissRequest()
                                            }
                                    )
                                }
                            }
                        } else {
                            NothingYet()
                        }
                    }
                }
            }
        }
    }
}