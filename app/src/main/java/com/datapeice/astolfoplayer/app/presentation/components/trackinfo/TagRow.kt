package com.datapeice.astolfoplayer.app.presentation.components.trackinfo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TagRow(
    tag: String,
    value: String,
    enableCopy: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = tag,
            color = MaterialTheme.colorScheme.primary
        )
        val context = LocalContext.current
        Text(
            text = value,
            textAlign = TextAlign.End,
            modifier = Modifier
                .padding(start = 16.dp)
                .then(
                    if (enableCopy) {
                        Modifier.clickable {
                            val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager
                            val clip = ClipData.newPlainText(null, value)
                            clipboardManager?.setPrimaryClip(clip)
                        }
                    } else Modifier
                )

        )
    }
}