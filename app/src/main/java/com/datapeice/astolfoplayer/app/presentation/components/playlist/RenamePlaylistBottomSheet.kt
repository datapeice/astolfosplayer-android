package com.datapeice.astolfoplayer.app.presentation.components.playlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.datapeice.astolfoplayer.R
import com.datapeice.astolfoplayer.app.domain.track.Playlist

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenamePlaylistBottomSheet(
    playlists: List<Playlist>,
    initialName: String,
    onRenameClick: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        dragHandle = null,
        modifier = modifier
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .padding(top = 28.dp, bottom = 28.dp)
        ) {
            val context = LocalContext.current

            Text(
                text = context.resources.getString(R.string.rename_playlist),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            var playlistName by remember {
                mutableStateOf(initialName)
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
                        playlistName == initialName -> {
                            onDismissRequest()
                        }

                        playlistName.isBlank() -> {
                            error =
                                context.resources.getString(R.string.blank_playlist_name_error)
                        }

                        playlistName in playlists.map { it.name } -> {
                            error =
                                context.resources.getString(R.string.playlist_already_exists_error)
                        }

                        else -> {
                            onRenameClick(playlistName)
                            onDismissRequest()
                        }
                    }
                },
                shape = ShapeDefaults.Large,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = context.resources.getString(R.string.rename),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}