package com.datapeice.astolfoplayer.app.presentation.components.trackinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.datapeice.astolfoplayer.R
import com.datapeice.astolfoplayer.app.domain.lyrics.Lyrics
import com.datapeice.astolfoplayer.app.presentation.components.topbar.ColumnWithCollapsibleTopBar

@Composable
fun LyricsControlSheet(
    state: LyricsControlSheetState,
    onDeleteLyricsClick: () -> Unit,
    onFetchLyricsFromRemoteClick: () -> Unit,
    onPickLyricsClick: () -> Unit,
    onCopyLyricsFromTagClick: () -> Unit,
    onWriteLyricsToTagClick: () -> Unit,
    onPublishLyricsOnRemoteClick: () -> Unit,
    onBackClick: () -> Unit,
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
                    contentDescription = context.resources.getString(R.string.back_to_track_info)
                )
            }

            Text(
                text = context.resources.getString(R.string.lyrics_control),
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
        contentVerticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        LyricsSourceInfo(
            title = context.resources.getString(R.string.lyrics_in_tag),
            supportingText = context.resources.getString(R.string.lyrics_in_tag_explain),
            lyrics = state.lyricsFromTag,
            areLyricsFromTag = true
        )

        LyricsSourceInfo(
            title = context.resources.getString(R.string.lyrics_in_local_database),
            supportingText = context.resources.getString(R.string.lyrics_in_local_database_explain),
            lyrics = state.lyricsFromRepository,
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        LyricsActions(
            state = state,
            onClearDbEntryClick = onDeleteLyricsClick,
            onImportFromFileClick = onPickLyricsClick,
            onCopyFromTagClick = onCopyLyricsFromTagClick,
            onWriteToTagClick = onWriteLyricsToTagClick,
            onFetchFromRemoteClick = onFetchLyricsFromRemoteClick,
            onPublishOnRemoteClick = onPublishLyricsOnRemoteClick
        )
    }
}

@Composable
fun LyricsSourceInfo(
    title: String,
    supportingText: String,
    lyrics: Lyrics?,
    areLyricsFromTag: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        val context = LocalContext.current
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = supportingText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val chipColors = FilterChipDefaults.filterChipColors(
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row {
            if (!areLyricsFromTag) {
                val hasSyncedLyrics by remember(lyrics) {
                    mutableStateOf(lyrics?.synced != null)
                }
                FilterChip(
                    selected = hasSyncedLyrics,
                    onClick = {},
                    label = {
                        Text(text = context.resources.getString(R.string.synced))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (hasSyncedLyrics) {
                                Icons.Rounded.Check
                            } else Icons.Rounded.Close,
                            contentDescription = null
                        )
                    },
                    colors = chipColors
                )

                Spacer(modifier = Modifier.width(16.dp))
            }

            val hasPlainLyrics by remember(lyrics) {
                mutableStateOf(lyrics?.plain != null)
            }
            FilterChip(
                selected = hasPlainLyrics,
                onClick = {},
                label = {
                    Text(text = context.resources.getString(R.string.plain))
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (hasPlainLyrics) {
                            Icons.Rounded.Check
                        } else Icons.Rounded.Close,
                        contentDescription = null
                    )
                },
                colors = chipColors
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LyricsActions(
    state: LyricsControlSheetState,
    onClearDbEntryClick: () -> Unit,
    onCopyFromTagClick: () -> Unit,
    onWriteToTagClick: () -> Unit,
    onImportFromFileClick: () -> Unit,
    onFetchFromRemoteClick: () -> Unit,
    onPublishOnRemoteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        val context = LocalContext.current
        Text(
            text = context.resources.getString(R.string.lyrics_actions),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = context.resources.getString(R.string.lyrics_tools_explained),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val enableChips by remember(state) {
                mutableStateOf(!(state.isWritingToTag || state.isReadingFromFile || state.isFetchingFromRemote || state.isPublishingOnRemote))
            }

            state.lyricsFromRepository?.let {
                AssistChip(
                    onClick = onClearDbEntryClick,
                    label = {
                        Text(text = context.resources.getString(R.string.clear_db_entry))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    enabled = enableChips
                )
            }

            AssistChip(
                onClick = onImportFromFileClick,
                label = {
                    Text(text = context.resources.getString(R.string.import_from_file))
                },
                leadingIcon = {
                    if (!state.isReadingFromFile) {
                        Icon(
                            imageVector = Icons.Rounded.Description,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                enabled = enableChips
            )

            state.lyricsFromTag?.let {
                AssistChip(
                    onClick = onCopyFromTagClick,
                    label = {
                        Text(text = context.resources.getString(R.string.copy_from_tag))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.LocalOffer,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    enabled = enableChips
                )
            }

            state.lyricsFromRepository?.let {
                AssistChip(
                    onClick = onWriteToTagClick,
                    label = {
                        Text(text = context.resources.getString(R.string.write_to_tag))
                    },
                    leadingIcon = {
                        if (!state.isWritingToTag) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    enabled = enableChips
                )
            }

            AssistChip(
                onClick = onFetchFromRemoteClick,
                label = {
                    Text(text = context.resources.getString(R.string.fetch_from_lrclib))
                },
                leadingIcon = {
                    if (!state.isFetchingFromRemote) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                enabled = enableChips
            )

            /*state.lyricsFromRepository?.let {
                AssistChip(
                    onClick = onPublishOnRemoteClick,
                    label = {
                        Text(text = context.resources.getString(R.string.publish_on_lrclib))
                    },
                    leadingIcon = {
                        if (!state.isPublishingOnRemote) {
                            Icon(
                                imageVector = Icons.Rounded.Upload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    enabled = enableChips
                )
            }*/
        }
    }
}