package mihon.feature.migration.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.presentation.util.animateItemFastScroll
import eu.kanade.presentation.util.formatChapterNumber
import eu.kanade.presentation.util.rememberResourceBitmapPainter
import eu.kanade.tachiyomi.R
import kotlinx.coroutines.flow.collect
import mihon.feature.migration.list.models.MigratingManga
import tachiyomi.domain.manga.model.Manga
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.Badge
import tachiyomi.presentation.core.components.BadgeGroup
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.components.LightningProgressIndicator
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.components.material.topSmallPaddingValues
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.plus

@Composable
fun MigrationListScreenContent(
    items: List<MigratingManga>,
    canLoadMore: Boolean,
    migrationComplete: Boolean,
    finishedCount: Int,
    onItemClick: (Manga) -> Unit,
    onSearchManually: (MigratingManga) -> Unit,
    onRetry: (Long) -> Unit,
    onSkip: (Long) -> Unit,
    onMigrate: (Long) -> Unit,
    onCopy: (Long) -> Unit,
    onLoadMore: () -> Unit,
    openMigrationDialog: (Boolean) -> Unit,
) {
    val listState = rememberLazyListState()
    val hasMore by rememberUpdatedState(canLoadMore)
    val batchReady by rememberUpdatedState(items.none { it.searchResult.value == MigratingManga.SearchResult.Searching })
    val loadMore by rememberUpdatedState(onLoadMore)
    LaunchedEffect(listState) {
        snapshotFlow {
            hasMore && batchReady && listState.isScrollInProgress &&
                listState.layoutInfo.visibleItemsInfo.any { it.key == "load-more" }
        }.collect { atBottom -> if (atBottom) loadMore() }
    }
    val itemResults = mutableListOf<Pair<MigratingManga, MigratingManga.SearchResult>>()
    for (item in items) {
        val result by item.searchResult.collectAsState()
        itemResults += item to result
    }
    val foundItems = itemResults.filter { (_, result) ->
        result != MigratingManga.SearchResult.NotFound
    }
    val unfoundItems = itemResults.filter { (_, result) ->
        result == MigratingManga.SearchResult.NotFound
    }

    Scaffold(
        topBar = { scrollBehavior ->
            AppBar(
                title = if (items.isNotEmpty()) {
                    stringResource(MR.strings.migrationListScreenTitleWithProgress, finishedCount, items.size)
                } else {
                    stringResource(MR.strings.migrationListScreenTitle)
                },
                actions = {
                    AppBarActions(
                        listOf(
                            AppBar.Action(
                                title = stringResource(MR.strings.migrationListScreen_copyActionLabel),
                                icon = if (items.size == 1) Icons.Outlined.ContentCopy else Icons.Outlined.CopyAll,
                                onClick = { openMigrationDialog(true) },
                                enabled = migrationComplete && itemResults.any { it.second is MigratingManga.SearchResult.Success },
                            ),
                            AppBar.Action(
                                title = stringResource(MR.strings.migrationListScreen_migrateActionLabel),
                                icon = if (items.size == 1) Icons.Outlined.Done else Icons.Outlined.DoneAll,
                                onClick = { openMigrationDialog(false) },
                                enabled = migrationComplete && itemResults.any { it.second is MigratingManga.SearchResult.Success },
                            ),
                        ),
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { contentPadding ->
        FastScrollLazyColumn(state = listState, contentPadding = contentPadding + topSmallPaddingValues) {
            items(foundItems, key = { it.first.manga.id }) { (item, result) ->
                MigrationListRow(
                    item = item,
                    result = result,
                    onItemClick = onItemClick,
                    onSearchManually = onSearchManually,
                    onRetry = onRetry,
                    onSkip = onSkip,
                    onMigrate = onMigrate,
                    onCopy = onCopy,
                )
            }

            if (unfoundItems.isNotEmpty()) {
                item(key = "unfound-migrations-header") {
                    Text(
                        text = stringResource(MR.strings.zink_unfound_migrations),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = MaterialTheme.padding.medium,
                                top = MaterialTheme.padding.large,
                                end = MaterialTheme.padding.medium,
                                bottom = MaterialTheme.padding.small,
                            ),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                items(unfoundItems, key = { "unfound-${it.first.manga.id}" }) { (item, result) ->
                    MigrationListRow(
                        item = item,
                        result = result,
                        onItemClick = onItemClick,
                        onSearchManually = onSearchManually,
                        onRetry = onRetry,
                        onSkip = onSkip,
                        onMigrate = onMigrate,
                        onCopy = onCopy,
                    )
                }
            }
            if (canLoadMore) {
                item(key = "load-more") {
                    Button(
                        modifier = Modifier.fillMaxWidth().padding(MaterialTheme.padding.medium),
                        onClick = onLoadMore,
                        enabled = batchReady,
                    ) {
                        Text(stringResource(MR.strings.zink_load_ten_more))
                    }
                }
            }
        }
    }
}

@Composable
private fun MigrationListRow(
    item: MigratingManga,
    result: MigratingManga.SearchResult,
    onItemClick: (Manga) -> Unit,
    onSearchManually: (MigratingManga) -> Unit,
    onRetry: (Long) -> Unit,
    onSkip: (Long) -> Unit,
    onMigrate: (Long) -> Unit,
    onCopy: (Long) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .animateItemFastScroll()
            .padding(
                start = MaterialTheme.padding.medium,
                end = MaterialTheme.padding.small,
                top = MaterialTheme.padding.small,
                bottom = MaterialTheme.padding.small,
            )
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(8.dp)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MigrationListItem(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.Top)
                .fillMaxHeight(),
            manga = item.manga,
            source = item.source,
            chapterCount = item.chapterCount,
            latestChapter = item.latestChapter,
            onClick = { onItemClick(item.manga) },
        )

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
            contentDescription = null,
            modifier = Modifier.weight(0.2f),
        )

        MigrationListItemResult(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.Top)
                .fillMaxHeight(),
            result = result,
            onItemClick = onItemClick,
        )

        MigrationListItemAction(
            modifier = Modifier.weight(0.2f),
            result = result,
            onSearchManually = { onSearchManually(item) },
            onRetry = { onRetry(item.manga.id) },
            onSkip = { onSkip(item.manga.id) },
            onMigrate = { onMigrate(item.manga.id) },
            onCopy = { onCopy(item.manga.id) },
        )
    }
}

@Composable
fun MigrationListItem(
    modifier: Modifier,
    manga: Manga,
    source: String,
    chapterCount: Int,
    latestChapter: Double?,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .widthIn(max = 150.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(MangaCover.Book.ratio),
        ) {
            MangaCover.Book(
                modifier = Modifier.fillMaxWidth(),
                data = manga,
                shape = RoundedCornerShape(16.dp),
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            1f to MaterialTheme.colorScheme.background,
                        ),
                    )
                    .fillMaxHeight(0.4f)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
            )
            Text(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomStart),
                text = manga.title,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.labelMedium,
            )
            BadgeGroup(modifier = Modifier.padding(4.dp)) {
                Badge(text = "$chapterCount")
            }
        }

        Column(
            modifier = Modifier
                .padding(MaterialTheme.padding.extraSmall),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = source,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall,
            )
            val formattedLatestChapters = remember(latestChapter) {
                latestChapter?.let(::formatChapterNumber)
            }
            Text(
                text = stringResource(
                    MR.strings.migrationListScreen_latestChapterLabel,
                    formattedLatestChapters ?: stringResource(MR.strings.migrationListScreen_unknownLatestChapter),
                ),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun MigrationListItemResult(
    modifier: Modifier,
    result: MigratingManga.SearchResult,
    onItemClick: (Manga) -> Unit,
) {
    Box(modifier.height(IntrinsicSize.Min)) {
        when (result) {
            MigratingManga.SearchResult.Searching -> {
                Box(
                    modifier = Modifier
                        .widthIn(max = 150.dp)
                        .fillMaxSize()
                        .aspectRatio(MangaCover.Book.ratio),
                    contentAlignment = Alignment.Center,
                ) {
                    LightningProgressIndicator()
                }
            }
            MigratingManga.SearchResult.NotFound -> {
                Column(
                    Modifier
                        .widthIn(max = 150.dp)
                        .fillMaxSize()
                        .padding(4.dp),
                ) {
                    Image(
                        painter = rememberResourceBitmapPainter(id = R.drawable.cover_error),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(MangaCover.Book.ratio)
                            .clip(MaterialTheme.shapes.extraSmall),
                        contentScale = ContentScale.Crop,
                    )
                    Text(
                        text = stringResource(MR.strings.migrationListScreen_noMatchFoundText),
                        modifier = Modifier.padding(MaterialTheme.padding.extraSmall),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
            is MigratingManga.SearchResult.Success -> {
                MigrationListItem(
                    modifier = Modifier.fillMaxSize(),
                    manga = result.manga,
                    source = result.source,
                    chapterCount = result.chapterCount,
                    latestChapter = result.latestChapter,
                    onClick = { onItemClick(result.manga) },
                )
            }
        }
    }
}

@Composable
private fun MigrationListItemAction(
    modifier: Modifier,
    result: MigratingManga.SearchResult,
    onSearchManually: () -> Unit,
    onRetry: () -> Unit,
    onSkip: () -> Unit,
    onMigrate: () -> Unit,
    onCopy: () -> Unit,
) {
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    val closeMenu = { menuExpanded = false }
    Box(modifier) {
        when (result) {
            MigratingManga.SearchResult.Searching -> {
                IconButton(onClick = onSkip) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                    )
                }
            }
            MigratingManga.SearchResult.NotFound, is MigratingManga.SearchResult.Success -> {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = null,
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = closeMenu,
                    offset = DpOffset(8.dp, (-56).dp),
                ) {
                    if (result == MigratingManga.SearchResult.NotFound) {
                        DropdownMenuItem(
                            text = { Text(stringResource(MR.strings.action_retry)) },
                            onClick = {
                                closeMenu()
                                onRetry()
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(stringResource(MR.strings.migrationListScreen_searchManuallyActionLabel)) },
                        onClick = {
                            closeMenu()
                            onSearchManually()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(MR.strings.migrationListScreen_skipActionLabel)) },
                        onClick = {
                            closeMenu()
                            onSkip()
                        },
                    )
                    if (result is MigratingManga.SearchResult.Success) {
                        DropdownMenuItem(
                            text = { Text(stringResource(MR.strings.migrationListScreen_migrateNowActionLabel)) },
                            onClick = {
                                closeMenu()
                                onMigrate()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(MR.strings.migrationListScreen_copyNowActionLabel)) },
                            onClick = {
                                closeMenu()
                                onCopy()
                            },
                        )
                    }
                }
            }
        }
    }
}
