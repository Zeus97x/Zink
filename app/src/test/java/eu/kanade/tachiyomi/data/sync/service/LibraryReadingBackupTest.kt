package eu.kanade.tachiyomi.data.sync.service

import eu.kanade.tachiyomi.data.backup.models.Backup
import eu.kanade.tachiyomi.data.backup.models.BackupCategory
import eu.kanade.tachiyomi.data.backup.models.BackupChapter
import eu.kanade.tachiyomi.data.backup.models.BackupHistory
import eu.kanade.tachiyomi.data.backup.models.BackupManga
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
        val local = Backup(listOf(BackupManga(
            source = 1, url = "/series", favorite = true, favoriteModifiedAt = 10,
            chapters = listOf(BackupChapter("/chapter", "Chapter", read = true, lastPageRead = 20, version = 1)),
            history = listOf(BackupHistory("/chapter", 100, 60)),
        )))
        val remote = Backup(listOf(BackupManga(
            source = 1, url = "/series", favorite = false, favoriteModifiedAt = 20,
            chapters = listOf(BackupChapter("/chapter", "Chapter", read = false, version = 2)),
            history = listOf(BackupHistory("/chapter", 90, 70)),
        )))
        val merged = LibraryReadingBackup.merge(local, remote)
        val manga = merged.backupManga.single()
        assertFalse(manga.favorite)
        assertFalse(manga.chapters.single().read)
        assertEquals(0L, manga.chapters.single().lastPageRead)
        assertEquals(BackupHistory("/chapter", 100, 70), manga.history.single())
        assertEquals(manga.history, LibraryReadingBackup.merge(merged, remote).backupManga.single().history)
        assertTrue(local.backupManga.single().chapters.single().read)
    }
}
