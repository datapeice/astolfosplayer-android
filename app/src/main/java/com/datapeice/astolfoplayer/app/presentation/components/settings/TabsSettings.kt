package com.datapeice.astolfoplayer.app.presentation.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.datapeice.astolfoplayer.R
import com.datapeice.astolfoplayer.app.presentation.components.topbar.ColumnWithCollapsibleTopBar
import com.datapeice.astolfoplayer.app.presentation.components.topbar.Tab
import com.datapeice.astolfoplayer.core.data.Settings
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun TabsSettings(
    settings: Settings,
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
                text = context.resources.getString(R.string.tabs),
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
        val tabs by settings.tabs.collectAsState()
        val defaultTab by remember {
            mutableStateOf(settings.defaultTab)
        }
        SettingTabOrder(
            tabs = tabs,
            defaultTab = defaultTab,
            onDefaultTabSelect = {
                settings.defaultTab = it
            },
            onReorder = {
                settings.updateTabOrder(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        )
    }
}

@Composable
fun SettingTabOrder(
    tabs: List<Tab>,
    defaultTab: Tab,
    onDefaultTabSelect: (Tab) -> Unit,
    onReorder: (List<Tab>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(ShapeDefaults.ExtraLarge)
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
            .padding(vertical = 16.dp)
    ) {
        val context = LocalContext.current
        Text(
            text = context.resources.getString(R.string.default_tab_and_order),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 20.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        var items by remember {
            mutableStateOf(tabs)
        }
        var defaultTab by remember {
            mutableStateOf(defaultTab)
        }
        val listState = rememberLazyListState()
        val reorderableListState = rememberReorderableLazyListState(listState) { from, to ->
            items = items.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        }

        var wasTriggered by remember {
            mutableStateOf(false)
        }
        LaunchedEffect(reorderableListState.isAnyItemDragging) {
            if (wasTriggered) {
                if (!reorderableListState.isAnyItemDragging) {
                    onReorder(items)
                }
            } else wasTriggered = true
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                items = items,
                key = { it }
            ) { tab ->
                ReorderableItem(
                    state = reorderableListState,
                    key = tab
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clip(ShapeDefaults.Medium)
                            .clickable {
                                defaultTab = tab
                                onDefaultTabSelect(tab)
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = tab == defaultTab,
                                onClick = {
                                    defaultTab = tab
                                    onDefaultTabSelect(tab)
                                }
                            )

                            Text(
                                text = context.resources.getString(tab.titleResId),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = {},
                            modifier = Modifier.draggableHandle()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DragHandle,
                                contentDescription = context.resources.getString(R.string.reorder_tab)
                            )
                        }
                    }
                }
            }
        }
    }
}