# Zink — Development and recovery log

Repository: [Zeus97x/Zink](https://github.com/Zeus97x/Zink)  
Default branch at initialization: `master`

This is the running technical record for ChatGPT/Codex to recover context, understand how changes were made and resume work. Keep entries in chronological order and update with every meaningful change. See [AGENTS.md](AGENTS.md) for maintenance rules. The same standard applies to future Zeus websites in GitHub.

## Recovery starting point

- Inspect current Git source and history before acting on historical notes.
- Baseline before this log: [c5ce8fda346cf3ad98fbe2d228ff024ce7c1c3ed](https://github.com/Zeus97x/Zink/commit/c5ce8fda346cf3ad98fbe2d228ff024ce7c1c3ed). Recover committed files by checking out that commit or a later verified checkpoint; do not overwrite newer source with an old ZIP.
- Source directories and project configuration are present. Buildability and runtime behavior were not tested in this documentation task.
- Earlier work is documented in the references below and Git history. This initialization does not claim a complete reconstruction of past changes; backfill only from verified source, diffs and existing handoffs, with provenance.

### Existing history and guidance

- [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)
- [CONTRIBUTING.md](CONTRIBUTING.md)
- [README.md](README.md)

## 2026-10-08 — Initialize shared recovery record

- Request: keep a running Markdown record for all apps and future GitHub websites so development can resume if chat context or files are lost.
- Changed: added `DEVELOPMENT_LOG.md`; created `AGENTS.md`.
- How: documented the current Git checkpoint, repository contents and existing history references; required future work to record implementation details and verification alongside source changes. Existing project instructions and history are preserved.
- Affected files: `DEVELOPMENT_LOG.md`, `AGENTS.md`.
- Verification: read the default-branch root contents, existing root agent instructions and baseline Git commit. Documentation only; no application tests or build were run. Confirm the committed documents by reading them back after publication.
- Remaining: prior implementation details have not been backfilled; use the existing history and commits as evidence. Record subsequent work here with exact affected paths.
- Next step: read this record together with the relevant handoff/phase files, inspect current source, and proceed with the user's next authorized task.

## Entry template for future work

Copy this template for a new dated entry; replace every placeholder with facts.

### YYYY-MM-DD — Change or phase name

- Status: implemented / in progress / blocked / planned.
- Request/problem and reason:
- Changes:
- Implementation (how and important decisions):
- Affected files and configuration names (no secret values):
- References: baseline/source commit, PR, build/release, related notes as applicable. A commit cannot contain its own final SHA; use the baseline or add the resulting reference in a later entry.
- Verification: commands/checks actually run and their results; state what was not tested.
- Remaining issues/blockers:
- Recovery/resume: exact next step and any compatibility or migration details needed to continue safely.

## 2026-10-09 — Local bookshelf, cloud sync preparation and Updates layout

- Status: implemented in source; compilation/device verification pending; cloud schema activation blocked.
- Request: separate local bookshelf with a root-folder picker, series subfolders and posters; use Supabase for personal Android backup/sync; deliver an APK after these phases. Additional screenshot showed repeated Google Drive `client_secrets.json` failures and overlapping Updates rows during a large refresh.
- Local bookshelf: reuse source ID 0 and existing reader/title models; hide the old Browse entry; add Library overflow and Data/storage links; select an independent SAF root instead of app storage/local. Carry an existing local folder URI forward once without moving/deleting files. New URI/migration settings are app-state-only and cannot enter backups. Numbered local chapters sort numerically with natural-name ties; poster aliases cover/poster/folder are accepted in that order, with earliest recognized chapter fallback.
- Cloud client: add ZApps email/password login on the existing shared-account project's public client endpoint, rotated access/refresh sessions stored together as app state; never save passwords. A dedicated HTTP client avoids the debug header logger. Add Supabase service 3, reuse existing WorkManager intervals and reading/app triggers, turn off retired Google Drive service 2 and remove its UI/workflow credential packaging. Preserve SyncYomi and local backup import/export.
- Backup integrity: gzip + protobuf + base64 payload, 8 MiB compressed and 64 MiB decoded bounds; reject malformed remote snapshots; retry revision conflicts by pulling and merging again. Existing merge now preserves extension repositories. Restore repositories before titles. Never restore device-specific app-state keys or sync connection settings from an imported backup. First Supabase sync no longer skips applying a merged remote library simply because local data exists.
- Backend: prepared `docs/backend/zink_sync.sql` for shared project `sihbduemrgwqdznkxykx` (ZPet, contains zapp_accounts): one latest snapshot per owner, RLS SELECT/INSERT/UPDATE ownership checks and SECURITY INVOKER compare-and-swap function. No secret/service key or saved credentials are committed. The publishable client key is intentionally public and requires RLS on the proposed table.
- BLOCKED: automatic approval review rejected deploying `zink_owner_sync` because the user had not explicitly approved this precise production schema change and target shared project. No schema was deployed; do not retry indirectly or claim cloud sync works. Ask for approval to add this isolated table/function to ZPet after source is reviewable.
- Updates UI: remove placement animation on rows/date headers; use intrinsic row height with a 64 dp minimum and fixed 48 dp covers; clamp bookmark icon size. This addresses overlapping rows during bulk refresh and larger fonts; device reproduction still required. The screenshot's sync failure is a Google Drive configuration issue, not a missing-source error.
- CANCELLED: highest-known-chapter cutoff/initial-fetch baseline was briefly considered then removed at the user's request because fan-coloured chapter 1045 can coexist with main releases only through 765. Online SyncChaptersWithSource is byte-identical to baseline. Do not resurrect the cutoff, hide old chapters, or mark chapters read. The cause of every title appearing updated is not conclusively reproduced; missing sources, restored chapter data and source URL changes require device/database evidence if it persists.
- Verification: `git diff --check` passed; all base resource XML and APK workflow YAML parse; resource names are unique; added MR.strings references resolve; changed Kotlin imports have no duplicates; confirmed original online chapter matching remains unchanged. Added CloudBackupCodec JVM tests for binary roundtrip, corrupt data and expanded-size rejection, but they have NOT run.
- Attempted `./gradlew :app:testDebugUnitTest --no-daemon --console=plain`: failed before configuration/compilation because services.gradle.org is unreachable while downloading Gradle 9.6.1. No Android code compiled, unit tests passed, device tests ran, or APK was created in this pass.
- Recovery: source checkpoint uses [skip ci] to preserve the user's model-change pause before an APK. After exact backend approval, deploy the prepared SQL, verify owner isolation/CAS with rollback-only test data and advisors, then complete the requested model check and trigger the APK workflow. Publish/link Zink.apk using the existing workflow; do not monitor the build unless asked. Check folder permissions/restarts, cover aliases, archives/chapter order, imported TachiyomiSY backup, both-device sync and updates under a bulk refresh on-device.
- Follow-up phase after backup/sync: restore missing extension APKs from their restored repositories, with one user-triggered Install action per extension and Android's installer prompt; retain the existing overflow option. This installer feature is planned, not implemented by this pass.
- Baseline: cc0296946cd604e913d8fd9d04e837bdfdc3162b. Affected files:
  - `.github/workflows/build-apk.yml`
  - `DEVELOPMENT_LOG.md`
  - `app/src/main/java/eu/kanade/domain/source/interactor/GetEnabledSources.kt`
  - `app/src/main/java/eu/kanade/domain/sync/SyncPreferences.kt`
  - `app/src/main/java/eu/kanade/presentation/library/components/LibraryToolbar.kt`
  - `app/src/main/java/eu/kanade/presentation/more/settings/screen/SettingsDataScreen.kt`
  - `app/src/main/java/eu/kanade/presentation/more/settings/screen/ZAppsAccountPreferences.kt`
  - `app/src/main/java/eu/kanade/presentation/updates/UpdatesUiItem.kt`
  - `app/src/main/java/eu/kanade/tachiyomi/App.kt`
  - `app/src/main/java/eu/kanade/tachiyomi/data/backup/create/creators/PreferenceBackupCreator.kt`
  - `app/src/main/java/eu/kanade/tachiyomi/data/backup/restore/BackupRestorer.kt`
  - `app/src/main/java/eu/kanade/tachiyomi/data/backup/restore/restorers/PreferenceRestorer.kt`
  - `app/src/main/java/eu/kanade/tachiyomi/data/sync/SyncDataJob.kt`
  - `app/src/main/java/eu/kanade/tachiyomi/data/sync/SyncManager.kt`
  - `app/src/main/java/eu/kanade/tachiyomi/data/sync/service/CloudBackupCodec.kt`
  - `app/src/main/java/eu/kanade/tachiyomi/data/sync/service/SupabaseSyncService.kt`
  - `app/src/main/java/eu/kanade/tachiyomi/data/sync/service/SyncService.kt`
  - `app/src/main/java/eu/kanade/tachiyomi/data/sync/service/ZAppsAccount.kt`
  - `app/src/main/java/eu/kanade/tachiyomi/ui/browse/source/browse/BrowseSourceScreen.kt`
  - `app/src/main/java/eu/kanade/tachiyomi/ui/library/LibraryScreenModel.kt`
  - `app/src/main/java/eu/kanade/tachiyomi/ui/library/LibraryTab.kt`
  - `app/src/test/java/eu/kanade/tachiyomi/data/sync/service/CloudBackupCodecTest.kt`
  - `docs/CODEX_HANDOFF.md`
  - `docs/ZINK_PHASES.md`
  - `docs/backend/zink_sync.sql`
  - `domain/src/main/java/tachiyomi/domain/storage/service/StorageManager.kt`
  - `domain/src/main/java/tachiyomi/domain/storage/service/StoragePreferences.kt`
  - `i18n/src/commonMain/moko-resources/base/strings.xml`
  - `source-local/src/androidMain/kotlin/tachiyomi/source/local/LocalSource.kt`
  - `source-local/src/androidMain/kotlin/tachiyomi/source/local/image/LocalCoverManager.kt`

### Publication checkpoint blocker

- GitHub create_tree was rejected by automatic approval review: uploading the complete modified source to Zeus97x/Zink requires explicit approval for exporting that prepared payload to this destination. No blobs/tree/commit/ref were created remotely by this attempted operation.
- Direct Git push dry-run failed because no GitHub credential was available; this was a dry-run, not a publication attempt. Do not bypass either rejection via another upload mechanism.
- Preserve this pass as a local Git checkpoint while awaiting one explicit approval covering (a) source upload to Zeus97x/Zink, and (b) the prepared private zink_sync table/RLS/compare-and-swap function on shared ZPet Supabase project sihbduemrgwqdznkxykx. Use [skip ci] for source upload; the APK model-change pause still applies.
- All source described above is local only. The remote master remains cc0296946cd604e913d8fd9d04e837bdfdc3162b. APK compilation and end-to-end sync remain pending. Three remaining steps: publication approval, backend activation/verification, and APK build/test delivery.

## 2026-10-09 — Approved backend activation and library/reading-only sync

- Supersedes the earlier approval blockers and broad cloud-sync preparation above. User explicitly said “approve and continue”, then narrowed cloud sync to library and reading history/progress. Source push and APK workflow are authorized; no further model/permission pause is needed for this pass.
- Deployed docs/backend/zink_sync.sql as zink_owner_sync on shared ZPet project sihbduemrgwqdznkxykx. Rollback-only tests passed CAS initial revision 1, duplicate insert conflict 0, successful update 2, stale revision 0, owner read and rejected owner reassignment/cross-owner access. Follow-up confirmed zero snapshots after rollback, RLS enabled, three owner policies and no anonymous table/RPC access. Advisors returned no ZInk-specific findings; pre-existing unrelated ZPet RLS-without-policy informational findings and leaked-password-protection warning were left unchanged.
- Cloud whitelist builds fresh library/chapter records before upload and strips non-reading data from remote snapshots: no categories, extension stores/repos, source/app/private settings, saved searches, tracking, custom info, bookmarks or notes. Keep required source/title/chapter identity metadata and merged-title references. Merge chapters by version/time and progress, preserving explicit newer unread; union history by chapter URL with max timestamp/duration to avoid repeated-sync double counting.
- Cloud-only restoration preserves existing local title/chapter configuration and only updates library membership and reading state. Added SQL queries include favorites, removal timestamps and history-only titles, and preserve the incoming membership timestamp after the local favorite trigger. Local backup restore continues to support its full existing options.
- Added JVM tests for whitelist isolation, input preservation, newer unread/removal conflict resolution and idempotent history duration. Tests are prepared but have NOT run because Gradle distribution download is unavailable locally; CI is the compilation/test gate.
- Source checks: whitespace, base XML resource names/references, workflow YAML and unchanged online chapter matcher. SQL query/trigger checks executed in temporary SQLite data. Phone checks remain necessary for SAF folders, covers/archives, two-device login/sync and Updates rendering; the bulk all-chapters update cause is not conclusively reproduced.
- Publication: prepare one source commit on master without skip-ci to invoke .github/workflows/build-apk.yml. Workflow assembles debug APK, runs unit tests and publishes Zink.apk if successful. Respect user preference to give build link without monitoring.
- Paths changed in this narrowing: app/src/main/java/eu/kanade/domain/sync/SyncPreferences.kt; app/src/main/java/eu/kanade/presentation/more/settings/screen/SettingsDataScreen.kt; app/src/main/java/eu/kanade/tachiyomi/data/backup/restore/BackupRestoreJob.kt; app/src/main/java/eu/kanade/tachiyomi/data/backup/restore/BackupRestorer.kt; app/src/main/java/eu/kanade/tachiyomi/data/backup/restore/restorers/MangaRestorer.kt; app/src/main/java/eu/kanade/tachiyomi/data/sync/SyncManager.kt; app/src/main/java/eu/kanade/tachiyomi/data/sync/service/SupabaseSyncService.kt; app/src/main/java/eu/kanade/tachiyomi/data/sync/service/LibraryReadingBackup.kt; app/src/test/java/eu/kanade/tachiyomi/data/sync/service/LibraryReadingBackupTest.kt; data/src/main/sqldelight/tachiyomi/data/mangas.sq; i18n/src/commonMain/moko-resources/base/strings.xml; docs/CODEX_HANDOFF.md; docs/ZINK_PHASES.md; DEVELOPMENT_LOG.md. Earlier entry lists all local bookshelf/client/layout paths included in this publication.

### Publication and build launch

- Published source commit c97c90d2249008959bc0c0618dc0c004f8bc50ae to Zeus97x/Zink master after approval. Uploaded tree f5c4f198229190b0c4e451e5f947eeddcf2fbc7d exactly matches the staged local tree, including existing executable file modes; remote ref readback confirmed the commit.
- Confirmed one launch of APK workflow https://github.com/Zeus97x/Zink/actions/runs/37887215949 (in_progress). No ongoing monitoring; build/tests and Zink.apk release outcome remain pending. No APK success claim.
- Documentation-only follow-up records publication and launch in DEVELOPMENT_LOG.md and docs/CODEX_HANDOFF.md; uses skip-ci and does not trigger another build.

## 2026-10-09 — Fix nullable local cover names after failed APK build

- Request: repair failed APK run https://github.com/Zeus97x/Zink/actions/runs/37887215949 and start a fresh build.
- Failure evidence: job 113679805674 stopped at :source-local:compileAndroidMain. Kotlin reported nullable String receiver errors in LocalCoverManager.kt at lines 25 and 26. APK preparation, upload and release were skipped; unit tests had not run.
- Fix: safely lowercase nullable nameWithoutExtension with Locale.ROOT and use an empty fallback. Unnamed files fail the cover-name filter; named cover/poster/folder files retain the requested priority. No sync, chapter matching or backup behavior changes.
- Verification: inspected all compiler error lines in the failed job log; both reported receiver errors are addressed. git diff --check passed; targeted source check confirms both lookups use safe calls. Full Android compilation remains delegated to the replacement CI build because local Gradle distribution bootstrap is unavailable. Do not claim APK success until CI completes.
- Exact paths: source-local/src/androidMain/kotlin/tachiyomi/source/local/image/LocalCoverManager.kt; DEVELOPMENT_LOG.md; docs/CODEX_HANDOFF.md; docs/ZINK_PHASES.md.
- Next: publish this fix on master without skip-ci and give the replacement APK build link; do not monitor unless asked. Device checks for local folder covers and library/reading sync remain pending.

## 2026-10-09 — Fix repository restore coroutine scope after second failed APK

- Request: fix replacement APK run https://github.com/Zeus97x/Zink/actions/runs/37888040855. Job 113682376595 passed the earlier source-local compilation point, then failed :app:compileDebugKotlin at BackupRestorer.kt:125. The only compiler error reported was calling CoroutineScope.restoreExtensionStores without a CoroutineScope receiver. Unit tests/release remained skipped.
- Fix: move repository restoration inside the existing coroutineScope and join its child Job before launching remaining restore tasks. This supplies the required receiver and enforces repositories-before-titles, including cancellation through the parent scope. Cloud sync still excludes repositories and is unaffected; full local backups retain repository restoration.
- Verification: reviewed every compiler error in job 113682376595; inspected the helper's CoroutineScope receiver and Job return. git diff --check passed and the corrected call is inside coroutineScope with join before other task launches. Full compilation/JVM tests remain pending replacement CI, with local Gradle bootstrap unavailable.
- Paths: app/src/main/java/eu/kanade/tachiyomi/data/backup/restore/BackupRestorer.kt; DEVELOPMENT_LOG.md; docs/CODEX_HANDOFF.md; docs/ZINK_PHASES.md.
- Next: publish fix on master to trigger the APK workflow, provide build link and do not monitor unless asked. Phone validation remains pending.
