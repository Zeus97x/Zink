package tachiyomi.domain.storage.service

import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.core.common.storage.FolderProvider

class StoragePreferences(
    folderProvider: FolderProvider,
    preferenceStore: PreferenceStore,
) {

    // Device-specific SAF permissions must never travel in backups.
    val localBookshelfDirectory: Preference<String> = preferenceStore.getString(
        Preference.appStateKey("local_bookshelf_dir"),
        "",
    )
    val localBookshelfMigrated: Preference<Boolean> = preferenceStore.getBoolean(
        Preference.appStateKey("local_bookshelf_migrated"),
        false,
    )

    val baseStorageDirectory: Preference<String> = preferenceStore.getString(
        Preference.appStateKey("storage_dir"),
        folderProvider.path(),
    )
}
