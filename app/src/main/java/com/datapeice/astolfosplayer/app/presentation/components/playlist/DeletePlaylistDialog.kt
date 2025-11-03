package com.datapeice.astolfosplayer.app.presentation.components.playlist

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.datapeice.astolfosplayer.R

@Composable
fun DeletePlaylistDialog(
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(text = context.resources.getString(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(text = context.resources.getString(R.string.cancel))
            }
        },
        title = {
            Text(text = context.resources.getString(R.string.delete_playlist_dialog_title))
        },
        text = {
            Text(text = context.resources.getString(R.string.delete_playlist_dialog_text))
        }
    )
}