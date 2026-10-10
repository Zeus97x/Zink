package eu.kanade.tachiyomi.data.sync.service

import eu.kanade.tachiyomi.data.backup.models.Backup
import eu.kanade.tachiyomi.data.backup.models.BackupCategory
import eu.kanade.tachiyomi.data.backup.models.BackupChapter
import eu.kanade.tachiyomi.data.backup.models.BackupHistory
import eu.kanade.tachiyomi.data.backup.models.BackupManga
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LibraryReadingBackupTest {
    @Test
    fun stripsLocalConfigurationAndBookmarksButKeepsReadingState() {
        val original = Backup(
            backupManga = listOf(
                BackupManga(
                    source = 1, url = "/series", categories = listOf(1), viewer_flags = 42,
                    chapterFlags = 99, notes = "local note",
                    chapters = listOf(BackupChapter("/chapter", "Chapter", read = true, bookmark = true, lastPageRead = 5)),
                    history = listOf(BackupHistory("/chapter", 123, 60)),
                ),
            ),
            backupCategories = listOf(BackupCategory("Local category")),
        )
        val restricted = LibraryReadingBackup.restrict(original)
        val manga = restricted.backupManga.single()
        assertTrue(restricted.backupCategories.isEmpty())
        assertTrue(manga.categories.isEmpty())
        assertEquals(null, manga.viewer_flags)
        assertEquals(0, manga.chapterFlags)
        assertEquals("", manga.notes)
        assertFalse(manga.chapters.single().bookmark)
        assertTrue(manga.chapters.single().read)
        assertEquals(5L, manga.chapters.single().lastPageRead)
        assertEquals(original.backupManga.single().history, manga.history)
        assertTrue(original.backupManga.single().chapters.single().bookmark)
    }

    @Test
    fun newerUnreadStateWinsAndHistoryMergingIsIdempotent() {
        val local = Backup(
            listOf(
                BackupManga(
                    source = 1, url = "/series", favorite = true, favoriteModifiedAt = 10,
                    chapters = listOf(BackupChapter("/chapter", "Chapter", read = true, lastPageRead = 20, version = 1)),
                    history = listOf(BackupHistory("/chapter", 100, 60)),
                ),
            ),
        )
        val remote = Backup(
            listOf(
                BackupManga(
                    source = 1, url = "/series", favorite = false, favoriteModifiedAt = 20,
                    chapters = listOf(BackupChapter("/chapter", "Chapter", read = false, version = 2)),
                    history = listOf(BackupHistory("/chapter", 90, 70)),
                ),
            ),
        )
        val merged = LibraryReadingBackup.merge(local, remote)
        val manga = merged.backupManga.single()
        assertFalse(manga.favorite)
        assertFalse(manga.chapters.single().read)
        assertEquals(0L, manga.chapters.single().lastPageRead)
        assertEquals(BackupHistory("/chapter", 100, 70), manga.history.single())
        assertEquals(manga.history, LibraryReadingBackup.merge(merged, remote).backupManga.single().history)
        assertTrue(local.backupManga.single().chapters.single().read)
    }

    @Test
    fun omitsUntouchedUnreadCatalogueButKeepsResetsAndHistory() {
        val original = Backup(
            listOf(
                BackupManga(
                    source = 1, url = "/series", description = "Large source description",
                    chapters = listOf(
                        BackupChapter("/untouched", "Untouched"),
                        BackupChapter("/read", "Read", read = true),
                        BackupChapter("/progress", "Progress", lastPageRead = 3),
                        BackupChapter("/reset", "Reset", version = 2),
                        BackupChapter("/legacy-reset", "Legacy reset", lastModifiedAt = 100),
                        BackupChapter("/history", "History"),
                    ),
                    history = listOf(BackupHistory("/history", 123, 60)),
                ),
            ),
        )
        val compact = LibraryReadingBackup.restrict(original)
        assertEquals(
            listOf("/read", "/progress", "/reset", "/legacy-reset", "/history"),
            compact.backupManga.single().chapters.map { it.url },
        )
        assertEquals(null, compact.backupManga.single().description)
        assertEquals(6, original.backupManga.single().chapters.size)
        assertEquals("Large source description", original.backupManga.single().description)
    }

    @Test
    fun compactionKeepsLibraryTitlesEvenWithoutReadingState() {
        val original = Backup(
            listOf(
                BackupManga(
                    source = 1, url = "/unread-series", title = "Unread series", favorite = true,
                    chapters = (1..10000).map { BackupChapter("/chapter/$it", "Chapter $it") },
                ),
            ),
        )
        val compact = LibraryReadingBackup.restrict(original)
        assertTrue(compact.backupManga.single().favorite)
        assertEquals("Unread series", compact.backupManga.single().title)
        assertTrue(compact.backupManga.single().chapters.isEmpty())
        assertEquals(10000, original.backupManga.single().chapters.size)
        val originalBytes = ProtoBuf.encodeToByteArray(Backup.serializer(), original)
        val compactBytes = ProtoBuf.encodeToByteArray(Backup.serializer(), compact)
        assertTrue(compactBytes.size < originalBytes.size / 100)
        assertTrue(CloudBackupCodec.decode(CloudBackupCodec.encode(compactBytes)).contentEquals(compactBytes))
    }

    @Test
    fun sparseSnapshotStillPropagatesNewerUnreadReset() {
        val local = Backup(
            listOf(
                BackupManga(
                    source = 1, url = "/series",
                    chapters = listOf(BackupChapter("/chapter", "Chapter", read = true, version = 1)),
                ),
            ),
        )
        val remote = Backup(
            listOf(
                BackupManga(
                    source = 1, url = "/series",
                    chapters = listOf(BackupChapter("/chapter", "Chapter", version = 2)),
                ),
            ),
        )
        val merged = LibraryReadingBackup.merge(local, remote)
        assertFalse(merged.backupManga.single().chapters.single().read)
        assertEquals(2L, merged.backupManga.single().chapters.single().version)
        assertEquals(
            merged.backupManga.single().chapters.map { it.url },
            LibraryReadingBackup.restrict(merged).backupManga.single().chapters.map { it.url },
        )
    }
}
