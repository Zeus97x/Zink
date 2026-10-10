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

## 2026-10-09 — Keep only the latest successful local automatic backup

- Request: user reports sync working, asks whether backups reinstall extensions and requests deleting old local automatic backups when a new one is made. Clarified: backupExtensionStores saves repositories, backupSources saves IDs/names for title sources; extension APK binaries and installed-package inventory/reinstall are NOT implemented. One-at-a-time Install from restored repos remains planned. Do not advise uninstalling an extension to test this unfinished feature.
- Changed automatic retention from four backups/pruning before writing to keeping one validated replacement/pruning afterward. Collect only matching app backup files in the configured automatic destination; give each automatic backup a UUID suffix so repeated attempts in the same minute cannot overwrite an existing good file. Filename matcher accepts legacy minute-only and new UUID names, escapes the app ID and extension delimiter. Other files/directories and backup exports elsewhere are excluded. Legacy same-named manual exports placed in the automatic folder cannot be distinguished from automatic files and share that folder's retention policy.
- After gzip output is closed and BackupFileValidator succeeds, update the last-auto timestamp and delete preceding candidates excluding the new URI. Deletion errors are logged best-effort and do not discard the new valid backup. Failed writing/validation removes only the new attempted file and leaves previous backups intact. Provider deletion failures may leave extra old files. Manual backup creation remains unchanged.
- Verification: git diff --check passed; source ordering check puts deletion after validation; filename checks passed for legacy/UUID names and rejected another app ID, incorrect extension delimiter and unrelated files. These are source checks, not Android execution. Local Gradle bootstrap remains unavailable; actual SAF deletion/failure behavior and compilation await the next build/device test.
- Paths: app/src/main/java/eu/kanade/tachiyomi/data/backup/create/BackupCreator.kt; DEVELOPMENT_LOG.md; docs/CODEX_HANDOFF.md; docs/ZINK_PHASES.md. Publish with skip-ci for the next build; this request did not explicitly request another APK. Cloud sync remains library/progress/history only.

## 2026-10-09 — Specify extension recovery list and Find match

- Request: user wants a list where each missing extension can be installed or manually matched before installation. Planning-only change; no APK requested.
- Added next-phase acceptance criteria for local installed-extension inventory, repo-first matching, legacy source-based backup recovery, right-side Install, searchable Find match/change-match, per-extension Android prompts and retry/status handling. Keep access via existing three-dot entry and show recovery after local restore. Include unused installed extensions in inventory; cloud sync excludes it.
- Paths: docs/ZINK_PHASES.md; docs/CODEX_HANDOFF.md; DEVELOPMENT_LOG.md.
- Verification: inspected current phase/handoff and backup limitations; documentation clearly marks the feature planned, not implemented. git diff --check passed. No runtime changes/tests/build. Next step is implementing this next phase when requested; do not suggest uninstall testing beforehand.

## 2026-10-09 — Prepare permanent signing and install-over updates

- Request: investigate repeated uninstalls, then prepare all signing setup so the user only enters secret values. This explicitly supersedes earlier instructions excluding signing work.
- Evidence: master build-apk.yml uses assembleDebug and does not restore/persist its runner debug keystore. Latest successful run 37888416977 uploads only the APK artifact. No keystore found in source. This makes changing runner debug signatures the likely reinstall cause; installed APK certificates were not compared. In-app updater is disabled in debug and points at jobobby04 upstream repos.
- Generated one RSA 3072-bit JKS outside Git; verified password and alias with keytool. Private setup includes key backup and four copy-paste secret value files. Do not log values or regenerate this key for later builds.
- Workflow now validates four secret names, strictly decodes the keystore in RUNNER_TEMP, validates its store password/alias, injects signing vars into Gradle and removes the key with always() cleanup. Missing secrets fail clearly before building. Gradle configures the existing debug signing configuration from environment and sets distributed versionCode to 1000 + GITHUB_RUN_NUMBER, preserving eu.kanade.tachiyomi.sy.debug. Local defaults remain as before. No temporary-key fallback in the published workflow.
- Paths: .github/workflows/build-apk.yml; app/build.gradle.kts; .gitignore; docs/CODEX_HANDOFF.md; docs/ZINK_PHASES.md; DEVELOPMENT_LOG.md.
- Verification: YAML and embedded Python parse; actual generated keystore decode round-trip; version-code assignment; all four missing-secret errors; malformed Base64 error; step ordering and always cleanup; keytool alias/store-password check; git diff --check. Android compilation and phone install/update remain pending.
- Publication uses [skip ci]; no APK/build requested for this preparation. GitHub connector cannot add/read Actions secrets. User adds SIGNING_KEYSTORE_BASE64, SIGNING_STORE_PASSWORD, SIGNING_KEY_ALIAS, SIGNING_KEY_PASSWORD, then requests next build. Likely one backed-up reinstall required for the new key. OTA not implemented.

## 2026-10-09 — Authorize publication and first permanently signed build

- User reports adding all four GitHub Actions secrets and explicitly authorizes uploading the prepared changes and building the APK. Earlier auto-review publication blocker is resolved by this explicit authorization.
- Re-ran signing preparation checks and git diff --check successfully. Publish the six prepared files with a normal commit to trigger master APK workflow once. No signing credentials are included in the source payload.
- Build/Gradle compilation and phone update behavior remain pending; do not monitor the run unless asked. Give the user the run link after confirming launch. First permanently signed installation likely requires a backed-up reinstall.

## 2026-10-09 — Normalize pasted signing Base64

- Failed run 37941766706, job 113857664336, stopped before Gradle at Restore permanent signing key: SIGNING_KEYSTORE_BASE64 is invalid Base64. All four secret variables were nonempty; no APK built. Actual secret value is unavailable, so whitespace is a possible cause, not confirmed.
- Normalize whitespace in the Base64 value before strict decoding; retain invalid-character checks and clarify the corrective copy/paste instruction. No changes to the permanent key or password requirements.
- Verification: restore script checks passed including round-trip and missing/invalid values, plus whitespace-wrapped actual key round-trip. Publish workflow and log fix to trigger one replacement build. If still invalid, user must replace this one secret with the complete text file contents; connector cannot edit secrets.

## 2026-10-09 — Replace missing FlexibleAdapter artifact with pinned source

- Run 37942611702, job 113860587940: signing restore succeeded, then compileDebugAidl failed resolving FlexibleAdapter c8013533. Both JitPack host URLs return 404. JitPack build API reports Error/No build artifacts; upstream log shows missing nu.studer:java-ordered-properties:1.0.1 required by old grabver publishing plugin. This is a dependency availability failure, not a signing-secret failure.
- Vendor flexible-adapter/src/main from exact original arkon/FlexibleAdapter commit c80135339bcff5f7f8c2c2380329dfc155b26232 with Apache license/provenance. Preserve Java/resources; use modern namespace manifest and Zink Android library convention instead of upstream publishing plugins. App depends on local project; remove obsolete catalog artifact.
- Paths: third-party/flexible-adapter/**; settings.gradle.kts; gradle/libs.versions.toml; app/build.gradle.kts; DEVELOPMENT_LOG.md.
- Verification: Java/resource bytes compared with pinned upstream; XML parsed; local project dependency and manifest namespace checked; git diff --check. Full Gradle compilation pending replacement CI. Publish and trigger one replacement APK build; no ongoing monitoring unless asked.

## 2026-10-09 — Compact library/reading cloud snapshots

- Request: user reports old APK shows codec size rejection, will install permanently signed APK first, then test update-over-install using the next sync-fix build. User now requests starting the fix. No APK build requested in this step.
- Exact notification originates in local CloudBackupCodec encode bounds (64 MiB decoded or 8 MiB compressed), not the separate HTTP 413 message. Actual payload size/data unavailable, so the violated bound is unknown. LibraryReadingBackup currently copied every catalogue chapter.
- Restrict cloud snapshots to chapters that are read, have progress, have a change version/timestamp, or appear in history. Preserve changed unread/reset rows as tombstones, membership/removal state, merged references and history. Strip descriptions, scanlator and fetch/upload dates from cloud copies. Newly synced title metadata is marked uninitialized for source refresh; existing local metadata remains protected by the reading-only restorer. No local backup change, backend migration, signing-key change or codec-limit increase. Old protobuf snapshots remain accepted and restricted before merge. A library containing enough changed chapters can still exceed the existing bounds; batching remains a future fallback if device test still fails.
- Paths: app/src/main/java/eu/kanade/tachiyomi/data/sync/service/LibraryReadingBackup.kt; app/src/test/java/eu/kanade/tachiyomi/data/sync/service/LibraryReadingBackupTest.kt; DEVELOPMENT_LOG.md; docs/CODEX_HANDOFF.md; docs/ZINK_PHASES.md.
- Tests added for untouched unread omission, history-only chapter retention, version/timestamp unread reset preservation, original input preservation, library membership with 10,000 unread chapters, protobuf size reduction/codec round-trip and newer reset merge. Existing whitelist/idempotency tests retained.
- Actual verification: executed chapter table and triggers from chapters.sq in SQLite: initial version/timestamp zero; read then unread reset increments version to 2 and records timestamp. git diff --check passed. Targeted Gradle JVM test attempt failed at Gradle distribution download with Network is unreachable, before compilation/test execution. Tests are added but NOT run/passed.
- Publish source with skip-ci. Next: authorized APK build runs tests, then phone sync and update-over-install verification. Do not claim confirmed size resolution without the user's library test.

- Publication blocker: automatic approval review rejected the five-file GitHub tree write for the sync fix, requiring explicit export approval for this payload to Zeus97x/Zink. No remote tree/ref update or APK build from this fix. Local changes remain prepared and committed; ask approval before retrying.

- User explicitly approved uploading the prepared sync source/tests/documentation to Zeus97x/Zink at 10:50 Toronto time. Prior publication blocker resolved. Source-only commit retains skip-ci; no new APK build in this step.

## 2026-10-09 — Build compact sync APK for install-over test

- User explicitly requests building the published sync fix and testing the permanent signing setup. Base master is 65bbd48d3bd28d382294dd6543358a2324256031. Workflow comment-only change triggers one new master build; signing configuration/key and increasing run-number versionCode remain unchanged.
- GitHub CI will compile the APK and run JVM tests, including compact snapshot regression cases. Results are pending. User will install this APK over the first permanently signed APK and confirm retained library/settings and actual cloud sync. No claim of successful update compatibility before the phone test.

## 2026-10-09 — Approved bolt + inkwell logo PR

- Request: redesign tacky logo, user selected A (lightning into inkwell); apply to launcher, loading/splash and settings branding. Prepare separate PR and Claude build command.
- Implemented editable amber/ivory mark on charcoal with safe padding, Android colour/themed/notification vectors, both main/debug legacy launcher rasters and splash/web rasters. More/About reuse shared LogoHeader with charcoal badge for light themes; Settings adds the shared header and adjusts two-pane row scroll offset. Adaptive launcher and splash references already point to the shared vector.
- Paths: artwork/zink-{icon.svg,mark.svg,icon-preview.png}; scripts/artwork/generate_zink_icons.py; app/src/{main,debug}/res/mipmap-*/ic_launcher*.png; main drawable-*/splash_icon.webp; main/ic_launcher-web.png; main drawable/{ic_zink_logo,ic_zink_monochrome_launcher,ic_tachi,ic_launcher_foreground}.xml; debug drawable/ic_launcher_foreground.xml; presentation/more/{LogoHeader.kt,settings/screen/SettingsMainScreen.kt}; docs/ai/tasks/zink-logo-refresh/HANDOFF.md; docs/{CODEX_HANDOFF,ZINK_PHASES}.md; DEVELOPMENT_LOG.md.
- Verification: vector XML parse, raster decoding/dimensions and rendered SVG visual review; git diff --check. No Android build executed; compilation and device appearance/update checks delegated to Claude. Existing signing workflow and secrets preserved. Source publication is on design/zink-bolt-inkwell, not master; no merge or build requested of ChatGPT.
- Next: Claude builds this branch using existing permanently signed Actions workflow and checks approved placement. User confirms two-device sync separately; update-over-install reported successful on current APK.

## 2026-10-10 — Claude build of logo PR #2 (design/zink-bolt-inkwell)

- Status: in progress — branch review done, signed APK build dispatched; results recorded in a follow-up entry.
- Request: review the bolt + inkwell integration, run the existing Build Zink APK workflow on this branch with the current signing secrets, fix failures on the same branch, report the signed APK/release. Do not merge PR #2. Preserve application ID eu.kanade.tachiyomi.sy.debug and the permanent signing key.
- Review: all launcher/splash/notification vectors share the same 108-viewport paths with 0.9 safe-area group; monochrome and notification variants use white silhouettes. Settings two-pane offset `+3` matches the new logo item plus the two existing utility rows. Found two import-order problems that `spotlessCheck` (PR CI) would reject.
- Fix: moved `RoundedCornerShape` import below `layout.*` in `LogoHeader.kt`; moved `eu.kanade.presentation.more.LogoHeader` import into the `eu.kanade` block in `SettingsMainScreen.kt`. No behaviour change.
- Paths: app/src/main/java/eu/kanade/presentation/more/LogoHeader.kt; app/src/main/java/eu/kanade/presentation/more/settings/screen/SettingsMainScreen.kt; DEVELOPMENT_LOG.md.
- Verification: source review only in this commit; local Gradle unavailable in this environment. Workflow and signing configuration unchanged.

## 2026-10-10 — Logo PR #2 signed APK build result

- Status: build implemented and verified in CI; device acceptance pending (user).
- Build: Build Zink APK workflow_dispatch on design/zink-bolt-inkwell at b19168e3483e466236546469fc9077e8243c0c8e — run [38015885987](https://github.com/Zeus97x/Zink/actions/runs/38015885987) (run #13), conclusion success in 3m01s. `:app:assembleDebug :app:testDebugUnitTest` passed; signing key restored from the existing four secrets (names only: SIGNING_KEYSTORE_BASE64, SIGNING_STORE_PASSWORD, SIGNING_KEY_ALIAS, SIGNING_KEY_PASSWORD); ZINK_VERSION_CODE=1013.
- Outputs: artifact `Zink` id 11656236755; release [zink-38015885987-1](https://github.com/Zeus97x/Zink/releases/tag/zink-38015885987-1), asset Zink.apk 112,954,687 bytes. The release is a normal (non-draft) GitHub release created by the existing workflow from a non-master commit.
- Update-compatibility check (performed offline on downloaded APKs): APK Signature Scheme v2 signer certificate SHA-256 `7dcdeaf250980cf17e6cdbd8b65c4b68eb18fcf757268ae0c69aa598b4dbc611` is identical for this APK and the previous master release zink-37949162101-1 (run #12, versionCode 1012). Manifest contains application ID eu.kanade.tachiyomi.sy.debug. VersionCode increases 1012 → 1013. No signing, workflow or application ID change.
- PR CI (`CI` / build_check.yml, run 38015887176): still fails at `:app:spotlessKotlinCheck`, but only on files that come from master and are not touched by this PR (LibraryToolsScreen.kt, ZAppsAccountPreferences.kt, UpdatesUiItem.kt, BackupCreator.kt, SyncManager.kt, SupabaseSyncService.kt, ZAppsAccount.kt, LibraryTab.kt, LibraryReadingBackupTest.kt). The logo files no longer appear after the import-order fix. Not fixed here to keep PR #2 scoped; fix separately with `./gradlew spotlessApply` on master (local Gradle unavailable in this session: Maven Central HTTP 429).
- Not verified: launcher masks, splash, More/About/Settings appearance, light/dark, two-pane scroll, notification icons and update-over-install on device. PR #2 not merged.
- Next: user installs release APK over current signed app and runs the HANDOFF device acceptance list; separately run spotlessApply on master to green PR CI.
