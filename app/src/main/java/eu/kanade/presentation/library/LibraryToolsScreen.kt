package eu.kanade.presentation.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.domain.manga.interactor.UpdateManga
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.presentation.util.Screen
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mihon.feature.migration.config.MigrationConfigScreen
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.interactor.GetLibraryManga
import tachiyomi.domain.manga.model.asMangaCover
import tachiyomi.domain.source.service.SourceManager
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.util.Locale

class LibraryToolsScreen(private val duplicates: Boolean) : Screen() {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = rememberScreenModel { LibraryToolsScreenModel() }
        val state by model.state.collectAsState()
        var limit by rememberSaveable { mutableStateOf(10) }
        var selection by rememberSaveable { mutableStateOf(emptyList<Long>()) }
        var pending by remember { mutableStateOf<LibraryManga?>(null) }
        val listState = rememberLazyListState()
        LaunchedEffect(state.items, state.loading) {
            if (!state.loading) {
                val ids = state.items.map { it.id }.toSet()
                selection = selection.filter { it in ids }
            }
        }
        val groups = remember(state.items, duplicates) {
            if (duplicates) {
                state.items.groupBy { duplicateTitleKey(it.manga.title) }
                    .filter { (key, entries) -> key.isNotEmpty() && entries.size > 1 }.values.toList()
            } else {
                state.items.map { listOf(it) }
            }
        }
        val totalGroups by rememberUpdatedState(groups.size)
        LaunchedEffect(listState) {
            snapshotFlow {
                listState.isScrollInProgress && listState.layoutInfo.visibleItemsInfo.any { it.key == "load_more" }
            }.collect { atBottom ->
                if (atBottom && limit < totalGroups) limit += 10
            }
        }
        Scaffold(
            topBar = {
                AppBar(
                    title = stringResource(if (duplicates) MR.strings.zink_delete_duplicates else MR.strings.action_migrate),
                    navigateUp = navigator::pop,
                )
            },
            floatingActionButton = {
                if (!duplicates && selection.isNotEmpty()) {
                    Button(onClick = { navigator.push(MigrationConfigScreen(selection)) }) {
                        Text(stringResource(MR.strings.zink_migrate_selected, selection.size))
                    }
                }
            },
        ) { padding ->
            LazyColumn(state = listState, contentPadding = padding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    if (state.loading) {
                        Text(stringResource(MR.strings.zink_library_loading), Modifier.padding(16.dp))
                    } else if (state.error != null) {
                        Text(stringResource(state.error!!), Modifier.padding(16.dp))
                        TextButton(onClick = model::refresh) { Text(stringResource(MR.strings.action_retry)) }
                    } else if (groups.isEmpty()) {
                        Text(
                            stringResource(if (duplicates) MR.strings.zink_duplicates_empty else MR.strings.zink_library_empty),
                            Modifier.padding(16.dp),
                        )
                    }
                }
                items(groups.take(limit), key = { it.first().id }) { group ->
                    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (duplicates) Text(group.first().manga.title, style = MaterialTheme.typography.titleMedium)
                        // Two columns stay readable; larger duplicate groups continue on another row.
                        group.chunked(2).forEach { pair ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                pair.forEach { entry ->
                                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp)) {
                                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            MangaCover.Book(
                                                data = entry.manga.asMangaCover(),
                                                modifier = if (duplicates) Modifier.fillMaxWidth() else Modifier.width(96.dp),
                                                shape = RoundedCornerShape(16.dp),
                                            )
                                            Text(entry.manga.title, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                            Text(model.sourceName(entry.manga.source), style = MaterialTheme.typography.bodySmall)
                                            Text(stringResource(MR.strings.zink_chapter_progress, entry.readCount, entry.totalChapters), style = MaterialTheme.typography.bodySmall)
                                            if (duplicates) {
                                                TextButton(onClick = { pending = entry }, enabled = !state.deleting && !state.loading) {
                                                    Text(stringResource(MR.strings.action_delete))
                                                }
                                            } else {
                                                Checkbox(
                                                    checked = entry.id in selection,
                                                    onCheckedChange = { checked ->
                                                        selection = if (checked) selection + entry.id else selection - entry.id
                                                    },
                                                )
                                            }
                                        }
                                    }
                                }
                                if (pair.size == 1 && duplicates) androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
                if (groups.size > limit) {
                    item(key = "load_more") {
                        Button(onClick = { limit += 10 }, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(stringResource(MR.strings.zink_load_ten_more))
                        }
                    }
                }
            }
        }
        pending?.let { entry ->
            AlertDialog(
                onDismissRequest = { pending = null },
                title = { Text(stringResource(MR.strings.zink_remove_duplicate_title)) },
                text = { Text(stringResource(MR.strings.zink_remove_duplicate_message, entry.manga.title, model.sourceName(entry.manga.source))) },
                confirmButton = {
                    TextButton(onClick = {
                        pending = null
                        model.remove(entry)
                    }) { Text(stringResource(MR.strings.action_remove)) }
                },
                dismissButton = { TextButton(onClick = { pending = null }) { Text(stringResource(MR.strings.action_cancel)) } },
            )
        }
    }
}

private class LibraryToolsScreenModel : StateScreenModel<LibraryToolsScreenModel.State>(State()) {
    private val library: GetLibraryManga = Injekt.get()
    private val updateManga: UpdateManga = Injekt.get()
    private val sources: SourceManager = Injekt.get()
    private var libraryJob: Job? = null

    init {
        refresh()
    }

    fun sourceName(id: Long): String = sources.get(id)?.name ?: "Unavailable source ($id)"

    fun refresh() {
        if (state.value.deleting) return
        libraryJob?.cancel()
        mutableState.update { it.copy(loading = true, error = null) }
        libraryJob = screenModelScope.launch {
            try {
                updateLibrary(library.await())
                library.subscribe().collect(::updateLibrary)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                mutableState.update { it.copy(loading = false, error = MR.strings.zink_library_load_error) }
            }
        }
    }

    private fun updateLibrary(entries: List<LibraryManga>) {
        val items = entries.distinctBy { it.id }.sortedBy { it.manga.title.lowercase(Locale.ROOT) }
        mutableState.update { it.copy(items = items, loading = false) }
    }

    fun remove(entry: LibraryManga) {
        if (state.value.deleting || state.value.loading) return
        if (state.value.items.none { it.id == entry.id }) return
        val key = duplicateTitleKey(entry.manga.title)
        if (state.value.items.count { duplicateTitleKey(it.manga.title) == key } < 2) return
        mutableState.update { it.copy(deleting = true, error = null) }
        screenModelScope.launch {
            try {
                check(updateManga.awaitUpdateFavorite(entry.id, false))
                mutableState.update { it.copy(items = it.items.filterNot { manga -> manga.id == entry.id }) }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                mutableState.update { it.copy(error = MR.strings.zink_duplicate_remove_error) }
            } finally {
                mutableState.update { it.copy(deleting = false) }
            }
        }
    }

    data class State(
        val items: List<LibraryManga> = emptyList(),
        val loading: Boolean = true,
        val deleting: Boolean = false,
        val error: StringResource? = null,
    )
}
