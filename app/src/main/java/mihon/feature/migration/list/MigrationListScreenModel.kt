package mihon.feature.migration.list

import androidx.annotation.FloatRange
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.domain.chapter.interactor.SyncChaptersWithSource
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.getNameForMangaInfo
import exh.util.ThrottleManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import logcat.LogPriority
import mihon.domain.migration.usecases.MigrateMangaUseCase
import mihon.domain.source.interactor.UpdateMangaFromRemote
import mihon.feature.migration.list.models.MigratingManga
import mihon.feature.migration.list.models.MigratingManga.SearchResult
import mihon.feature.migration.list.search.SmartSourceSearchEngine
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.core.common.util.lang.withUIContext
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.chapter.interactor.GetChaptersByMangaId
import tachiyomi.domain.manga.interactor.GetManga
import tachiyomi.domain.manga.interactor.NetworkToLocalManga
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.service.SourceManager
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class MigrationListScreenModel(
    mangaIds: Collection<Long>,
    extraSearchQuery: String?,
    private val preferences: SourcePreferences = Injekt.get(),
    private val sourceManager: SourceManager = Injekt.get(),
    private val getManga: GetManga = Injekt.get(),
    private val networkToLocalManga: NetworkToLocalManga = Injekt.get(),
    private val syncChaptersWithSource: SyncChaptersWithSource = Injekt.get(),
    private val getChaptersByMangaId: GetChaptersByMangaId = Injekt.get(),
    private val migrateManga: MigrateMangaUseCase = Injekt.get(),
    private val updateMangaFromRemote: UpdateMangaFromRemote = Injekt.get(),
) : StateScreenModel<MigrationListScreenModel.State>(State()) {

    private val smartSearchEngine = SmartSourceSearchEngine(
        extraSearchParams = extraSearchQuery,
        eligibleThreshold = MIN_STRICT_MATCH_THRESHOLD,
    )

    // SY -->
    private val throttleManager = ThrottleManager()
    // SY <--

    val items
        inline get() = state.value.items

    private val hideWithoutUpdates = preferences.migrationHideWithoutUpdates.get()

    private val navigateBackChannel = Channel<Unit>()
    val navigateBackEvent = navigateBackChannel.receiveAsFlow()

    private var migrateJob: Job? = null
    private var searchJob: Job? = null

    init {
        screenModelScope.launchIO {
            val manga = mangaIds
                .map {
                    async {
                        val manga = getManga.await(it) ?: return@async null
                        val chapterInfo = getChapterInfo(it)
                        MigratingManga(
                            manga = manga,
                            chapterCount = chapterInfo.chapterCount,
                            latestChapter = chapterInfo.latestChapter,
                            source = sourceManager.getOrStub(manga.source).getNameForMangaInfo(),
                            parentContext = screenModelScope.coroutineContext,
                        )
                    }
                }
                .awaitAll()
                .filterNotNull()
            mutableState.update {
                it.copy(
                    items = manga,
                    loadedCount = manga.size.coerceAtMost(MIGRATION_BATCH_SIZE),
                )
            }
            searchJob = screenModelScope.launchIO {
                try {
                    runMigrations(manga.take(MIGRATION_BATCH_SIZE))
                } finally {
                    searchJob = null
                }
            }
        }
    }

    private suspend fun getChapterInfo(id: Long) = getChaptersByMangaId.await(id).let { chapters ->
        ChapterInfo(
            latestChapter = chapters.maxOfOrNull { it.chapterNumber },
            chapterCount = chapters.size,
        )
    }

    private suspend fun Manga.toSuccessSearchResult(): SearchResult.Success {
        val chapterInfo = getChapterInfo(id)
        val source = sourceManager.getOrStub(source).getNameForMangaInfo()
        return SearchResult.Success(
            manga = this,
            chapterCount = chapterInfo.chapterCount,
            latestChapter = chapterInfo.latestChapter,
            source = source,
        )
    }

    private suspend fun runMigrations(mangas: List<MigratingManga>) {
        // SY -->
        throttleManager.resetThrottle()
        // SY <--
        val prioritizeByChapters = preferences.migrationPrioritizeByChapters.get()
        val deepSearchMode = preferences.migrationDeepSearchMode.get()

        val sources = preferences.migrationSources.get()
            .mapNotNull { sourceManager.get(it) }

        for (manga in mangas) {
            if (!currentCoroutineContext().isActive) break
            if (manga.manga.id !in state.value.mangaIds) continue
            if (manga.searchResult.value != SearchResult.Searching) continue
            if (!manga.migrationScope.isActive) continue

            val result = try {
                manga.migrationScope.async {
                    if (prioritizeByChapters) {
                        val sourceSemaphore = Semaphore(5)
                        sources.map { source ->
                            async innerAsync@{
                                sourceSemaphore.withPermit {
                                    val result = searchSource(manga.manga, source, deepSearchMode)
                                    if (result == null || result.second.chapterCount == 0) return@innerAsync null
                                    result
                                }
                            }
                        }
                            .mapNotNull { it.await() }
                            .maxByOrNull { it.second.latestChapter ?: 0.0 }
                    } else {
                        sources.forEach { source ->
                            val result = searchSource(manga.manga, source, deepSearchMode)
                            if (result != null) return@async result
                        }
                        null
                    }
                }
                    .await()
            } catch (_: CancellationException) {
                continue
            }

            if (result != null && result.first.thumbnailUrl == null) {
                try {
                    updateMangaFromRemote(result.first, fetchDetails = true, manualFetch = true).getOrThrow().manga
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                }
            }

            manga.searchResult.value = result?.first?.toSuccessSearchResult() ?: SearchResult.NotFound

            // Keep unmatched titles visible so they can be retried or matched manually from
            // the dedicated Unfound migrations section.
            if (result != null &&
                hideWithoutUpdates &&
                (result.second.latestChapter ?: 0.0) <= (manga.latestChapter ?: 0.0)
            ) {
                removeManga(manga)
            }

            updateMigrationProgress()
        }
    }

    private suspend fun searchSource(
        manga: Manga,
        source: Source,
        deepSearchMode: Boolean,
    ): Pair<Manga, ChapterInfo>? {
        return try {
            val searchResult = if (deepSearchMode) {
                smartSearchEngine.deepSearch(source, manga.title)
            } else {
                smartSearchEngine.regularSearch(source, manga.title)
            }

            if (searchResult == null || (searchResult.url == manga.url && source.id == manga.source)) return null

            val localManga = networkToLocalManga(searchResult)
            try {
                updateMangaFromRemote(
                    localManga,
                    fetchChapters = true,
                    // SY -->
                    throttleFunc = throttleManager::throttle,
                    // SY <--
                ).getOrThrow()
            } catch (e: Exception) {
                logcat(LogPriority.ERROR, e)
            }
            localManga to getChapterInfo(localManga.id)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun updateMigrationProgress() {
        mutableState.update { state ->
            val loadedItems = state.loadedItems
            state.copy(
                finishedCount = loadedItems.count { it.searchResult.value != SearchResult.Searching },
                migrationComplete = migrationComplete(),
            )
        }
        if (items.isEmpty()) {
            navigateBack()
        }
    }

    private fun migrationComplete(): Boolean {
        val loadedItems = state.value.loadedItems
        return loadedItems.isNotEmpty() &&
            loadedItems.all { it.searchResult.value != SearchResult.Searching } &&
            loadedItems.any { it.searchResult.value is SearchResult.Success }
    }

    fun loadNextBatch() {
        if (searchJob?.isActive == true || migrateJob?.isActive == true) return

        val state = state.value
        if (!state.canLoadMore) return

        val nextLoadedCount = (state.loadedCount + MIGRATION_BATCH_SIZE).coerceAtMost(state.items.size)
        val batch = state.items.subList(state.loadedCount, nextLoadedCount)

        mutableState.update { it.copy(loadedCount = nextLoadedCount, migrationComplete = false) }
        searchJob = screenModelScope.launchIO {
            try {
                runMigrations(batch)
            } finally {
                searchJob = null
            }
        }
    }

    fun retryManga(mangaId: Long) {
        if (searchJob?.isActive == true || migrateJob?.isActive == true) return

        val item = items.find { it.manga.id == mangaId } ?: return
        item.searchResult.value = SearchResult.Searching
        mutableState.update { it.copy(migrationComplete = false) }
        searchJob = screenModelScope.launchIO {
            try {
                runMigrations(listOf(item))
            } finally {
                searchJob = null
            }
        }
    }

    fun useMangaForMigration(current: Long, target: Long, onMissingChapters: () -> Unit) {
        val migratingManga = items.find { it.manga.id == current } ?: return
        migratingManga.searchResult.value = SearchResult.Searching
        screenModelScope.launchIO {
            val result = migratingManga.migrationScope.async {
                val manga = getManga.await(target) ?: return@async null
                try {
                    val source = sourceManager.get(manga.source)!!
                    updateMangaFromRemote(
                        source = source,
                        manga = manga,
                        fetchChapters = true,
                        // SY -->
                        throttleFunc = throttleManager::throttle,
                        // SY <--
                    ).getOrThrow().manga
                } catch (_: Exception) {
                    null
                }
            }
                .await()

            if (result == null) {
                migratingManga.searchResult.value = SearchResult.NotFound
                withUIContext { onMissingChapters() }
                return@launchIO
            }

            migratingManga.searchResult.value = result.toSuccessSearchResult()
            updateMigrationProgress()
        }
    }

    fun migrateMangas() {
        migrateMangas(replace = true)
    }

    fun copyMangas() {
        migrateMangas(replace = false)
    }

    private fun migrateMangas(replace: Boolean) {
        migrateJob = screenModelScope.launchIO {
            mutableState.update { it.copy(dialog = Dialog.Progress(0f)) }
            val loadedItems = state.value.loadedItems
            try {
                loadedItems.forEachIndexed { index, manga ->
                    try {
                        ensureActive()
                        val target = manga.searchResult.value.let {
                            if (it is SearchResult.Success) {
                                it.manga
                            } else {
                                null
                            }
                        }
                        if (target != null) {
                            migrateManga(
                                current = manga.manga,
                                target = target,
                                replace = replace,
                                // SY -->
                                throttleFunc = throttleManager::throttle,
                                // SY <--
                            )
                            removeManga(manga)
                            manga.migrationScope.cancel()
                        }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        logcat(LogPriority.WARN, throwable = e)
                    }
                    mutableState.update {
                        it.copy(dialog = Dialog.Progress(((index + 1f) / loadedItems.size).coerceAtMost(1f)))
                    }
                }

                updateMigrationProgress()
            } finally {
                mutableState.update { it.copy(dialog = null) }
                migrateJob = null
            }
        }
    }

    fun cancelMigrate() {
        migrateJob?.cancel()
        migrateJob = null
    }

    private suspend fun navigateBack() {
        navigateBackChannel.send(Unit)
    }

    fun migrateNow(mangaId: Long, replace: Boolean) {
        screenModelScope.launchIO {
            val manga = items.find { it.manga.id == mangaId } ?: return@launchIO
            val target = (manga.searchResult.value as? SearchResult.Success)?.manga ?: return@launchIO
            migrateManga(current = manga.manga, target = target, replace = replace)

            removeManga(mangaId)
        }
    }

    fun removeManga(mangaId: Long) {
        screenModelScope.launchIO {
            val item = items.find { it.manga.id == mangaId } ?: return@launchIO
            removeManga(item)
            item.migrationScope.cancel()
            updateMigrationProgress()
        }
    }

    private fun removeManga(item: MigratingManga) {
        mutableState.update { state ->
            val itemIndex = state.items.indexOf(item)
            val updatedItems = state.items.toMutableList().apply { remove(item) }
            state.copy(
                items = updatedItems,
                loadedCount = when {
                    itemIndex in 0 until state.loadedCount -> state.loadedCount - 1
                    else -> state.loadedCount
                }.coerceIn(0, updatedItems.size),
            )
        }
    }

    override fun onDispose() {
        super.onDispose()
        items.forEach {
            it.migrationScope.cancel()
        }
        searchJob?.cancel()
    }

    fun showMigrateDialog(copy: Boolean) {
        mutableState.update { state ->
            val loadedItems = state.loadedItems
            state.copy(
                dialog = Dialog.Migrate(
                    copy = copy,
                    totalCount = loadedItems.size,
                    skippedCount = loadedItems.count { it.searchResult.value == SearchResult.NotFound },
                ),
            )
        }
    }

    fun showExitDialog() {
        mutableState.update {
            it.copy(dialog = Dialog.Exit)
        }
    }

    fun dismissDialog() {
        mutableState.update { it.copy(dialog = null) }
    }

    data class ChapterInfo(
        val latestChapter: Double?,
        val chapterCount: Int,
    )

    sealed interface Dialog {
        data class Migrate(val copy: Boolean, val totalCount: Int, val skippedCount: Int) : Dialog
        data class Progress(@FloatRange(0.0, 1.0) val progress: Float) : Dialog
        data object Exit : Dialog
    }

    data class State(
        val items: List<MigratingManga> = listOf(),
        val loadedCount: Int = 0,
        val finishedCount: Int = 0,
        val migrationComplete: Boolean = false,
        val dialog: Dialog? = null,
    ) {
        val mangaIds: List<Long> = items.map { it.manga.id }
        val loadedItems: List<MigratingManga> = items.take(loadedCount)
        val canLoadMore: Boolean = loadedCount < items.size
    }

    private companion object {
        const val MIGRATION_BATCH_SIZE = 10
        const val MIN_STRICT_MATCH_THRESHOLD = 0.72
    }
}
