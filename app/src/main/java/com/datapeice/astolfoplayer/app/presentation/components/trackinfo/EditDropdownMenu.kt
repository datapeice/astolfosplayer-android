package com.datapeice.astolfoplayer.app.presentation.components.trackinfo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.datapeice.astolfoplayer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDropdownMenu(
    isExpanded: Boolean,
    onLookForMetadataClick: () -> Unit,
    onManualEditingClick: () -> Unit,
    onLyricsControlClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = isExpanded,
        onDismissRequest = onDismissRequest,
        shape = ShapeDefaults.Medium,
        modifier = modifier
    ) {
        val context = LocalContext.current

        DropdownMenuItem(
            text = {
                Text(
                    text = context.resources.getString(R.string.look_for_metadata),
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.TravelExplore,
                    contentDescription = null
                )
            },
            onClick = {
                onDismissRequest()
                onLookForMetadataClick()
            }
        )

        DropdownMenuItem(
            text = {
                Text(
                    text = context.resources.getString(R.string.edit_manually),
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null
                )
            },
            onClick = {
                onDismissRequest()
                onManualEditingClick()
            }
        )

        DropdownMenuItem(
            text = {
                Text(
                    text = context.resources.getString(R.string.lyrics_control),
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Lyrics,
                    contentDescription = null
                )
            },
            onClick = {
                onDismissRequest()
                onLyricsControlClick()
            }
        )
    }
}