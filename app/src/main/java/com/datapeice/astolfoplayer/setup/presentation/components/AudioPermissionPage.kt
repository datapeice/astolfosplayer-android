package com.datapeice.astolfoplayer.setup.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.datapeice.astolfoplayer.R

@Composable
fun AudioPermissionPage(
    onGrantAudioPermissionClick: () -> Unit,
    onNextClick: () -> Unit,
    isAudioPermissionGrantedState: State<Boolean>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(28.dp)
    ) {
        val context = LocalContext.current
        val isAudioPermissionGranted by remember {
            isAudioPermissionGrantedState
        }
        Column(
            modifier = Modifier.align(alignment = Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SetupPageHeader(
                title = context.resources.getString(R.string.permissions),
                icon = Icons.Rounded.Security
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .fillMaxWidth()
                    .clip(shape = ShapeDefaults.ExtraLarge)
                    .background(color = MaterialTheme.colorScheme.surfaceContainer)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = ShapeDefaults.ExtraLarge
                    )
                    .clickable {
                        if (!isAudioPermissionGranted) {
                            onGrantAudioPermissionClick()
                        }
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(
                            color = if (isAudioPermissionGranted) {
                                MaterialTheme.colorScheme.primary
                            } else MaterialTheme.colorScheme.tertiary
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = isAudioPermissionGranted,
                        label = "permission-icon"
                    ) { isGranted ->
                        if (isGranted) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(25.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.LibraryMusic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = context.resources.getString(R.string.audio),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = context.resources.getString(R.string.explain_audio_permission_requirement),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        AnimatedContent(
            targetState = isAudioPermissionGranted,
            label = "permission-button-content",
            modifier = Modifier.align(Alignment.BottomEnd)
        ) { isGranted ->
            if (isGranted) {
                Button(
                    onClick = onNextClick,
                ) {
                    Text(
                        text = context.resources.getString(R.string.next)
                    )
                }
            } else {
                Button(
                    onClick = onGrantAudioPermissionClick,
                ) {
                    Text(
                        text = context.resources.getString(R.string.grant_permission)
                    )
                }
            }
        }
    }
}