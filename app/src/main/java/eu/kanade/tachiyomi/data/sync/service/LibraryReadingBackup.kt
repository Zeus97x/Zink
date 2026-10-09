package eu.kanade.tachiyomi.data.sync.service

import eu.kanade.domain.sync.models.SyncSettings
import eu.kanade.tachiyomi.data.backup.models.Backup
import eu.kanade.tachiyomi.data.backup.models.BackupChapter
import eu.kanade.tachiyomi.data.backup.models.BackupHistory
import eu.kanade.tachiyomi.data.backup.models.BackupManga

/** Explicit cloud whitelist. Local backup options remain independent. */
internal object LibraryReadingBackup {
    val settings = SyncSettings(
        libraryEntries = true,
        chapters = true,
        history = true,
        readEntries = true,
        categories = false,
        tracking = false,
        appSettings = false,
        extensionStores = false,
        sourceSettings = false,
        privateSettings = false,
        customInfo = false,
        savedSearches = false,
    )

    fun restrict(backup: Backup): Backup = Backup(
        backupManga = backup.backupManga.map { manga ->
            val historyUrls = manga.history.mapTo(HashSet()) { it.url }
            BackupManga(
                source = manga.source,
                url = manga.url,
                title = manga.title,
                artist = manga.artist,
                author = manga.author,
                genre = manga.genre,
                status = manga.status,
                thumbnailUrl = manga.thumbnailUrl,
                dateAdded = manga.dateAdded,
                favorite = manga.favorite,
                lastModifiedAt = manga.lastModifiedAt,
                favoriteModifiedAt = manga.favoriteModifiedAt,
                version = manga.version,
                // Metadata can be fetched from the source on a newly synced device.
                // Existing local metadata is preserved by the reading-only restorer.
                initialized = false,
                // Merged title relationships are part of library identity, not extension configuration.
                mergedMangaReferences = manga.mergedMangaReferences,
                // Omitted chapters have no reading state to send. Keep changed unread entries
                // so clearing progress on one device still reaches the other device.
                chapters = manga.chapters.filter { chapter ->
                    chapter.read || chapter.lastPageRead != 0L || chapter.version != 0L ||
                        chapter.lastModifiedAt != 0L || chapter.url in historyUrls
                }.map { chapter ->
                    BackupChapter(
                        url = chapter.url,
                        name = chapter.name,
                        read = chapter.read,
                        lastPageRead = chapter.lastPageRead,
                        chapterNumber = chapter.chapterNumber,
                        sourceOrder = chapter.sourceOrder,
                        lastModifiedAt = chapter.lastModifiedAt,
                        version = chapter.version,
                    )
                },
                history = manga.history,
            )
        },
        backupSources = backup.backupSources,
    )

    fun merge(localBackup: Backup, remoteBackup: Backup): Backup {
        val local = restrict(localBackup)
        val remote = restrict(remoteBackup)
        val localByKey = local.backupManga.associateBy { it.source to it.url }
        val remoteByKey = remote.backupManga.associateBy { it.source to it.url }
        val mangas = (localByKey.keys + remoteByKey.keys).map { key ->
            val a = localByKey[key]
            val b = remoteByKey[key]
            when {
                a == null -> requireNotNull(b)
                b == null -> a
                else -> {
                    val chosen = if (compareValuesBy(a, b, { it.favoriteModifiedAt ?: 0 }, { it.version }) >= 0) a else b
                    val chapters = (a.chapters + b.chapters).groupBy { it.url }.map { (_, copies) ->
                        copies.maxWith(
                            compareBy<BackupChapter> { it.version }.thenBy { it.lastModifiedAt }
                                .thenBy { it.read }.thenBy { it.lastPageRead },
                        )
                    }
                    chosen.chapters = chapters
                    chosen.history = (a.history + b.history).groupBy { it.url }.map { (url, history) ->
                        // Max is idempotent: repeated sync must not double-count time spent reading.
                        BackupHistory(url, history.maxOf { it.lastRead }, history.maxOf { it.readDuration })
                    }
                    chosen
                }
            }
        }
        return Backup(
            backupManga = mangas,
            backupSources = (local.backupSources + remote.backupSources).distinctBy { it.sourceId },
        )
    }
}
