package com.datapeice.astolfoplayer.app.presentation.components.trackinfo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.datapeice.astolfoplayer.R

@Composable
fun RisksOfMetadataEditingDialog(
    onCancelClick: () -> Unit,
    onAcceptClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onCancelClick,
        confirmButton = {
            TextButton(
                onClick = onAcceptClick
            ) {
                Text(text = context.resources.getString(R.string.accept))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancelClick
            ) {
                Text(text = context.resources.getString(R.string.cancel))
            }
        },
        title = {
            Text(text = context.resources.getString(R.string.risks_of_metadata_editing))
        },
        text = {
            Text(text = context.resources.getString(R.string.risks_of_metadata_editing_explain))
        },
        icon = {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null
            )
        },
        modifier = modifier
    )
}