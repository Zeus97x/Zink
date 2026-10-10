# Current status — October 9, 2026

The user explicitly approved source publication to Zeus97x/Zink, the isolated sync schema on shared ZPet, and continuing the APK build. The earlier publication/backend approval blockers are resolved; do not ask again. Latest cloud scope is ONLY library membership, chapter read/progress and reading history. Extensions, repositories, categories, tracking, settings, bookmarks and notes stay in local backups. The cloud whitelist also strips these fields from old remote snapshots before merging/restoring.

Migration zink_owner_sync is deployed on sihbduemrgwqdznkxykx. Rollback-only authenticated SQL tests passed initial insert, conflict rejection, revision increments, owner isolation and denied cross-owner writes. RLS is enabled with three owner policies; anonymous SELECT/RPC access is revoked. No test snapshots remain. Existing unrelated ZPet advisor findings were not changed.

Local bookshelf, Updates layout and library/reading-only sync were published in c97c90d2249008959bc0c0618dc0c004f8bc50ae. Initial build 37887215949 failed nullable cover filenames; fixed in 606399b64379a5efd8cdc2c14f1cc12fa19d021c. Replacement build 37888040855 got past source-local, then failed app compilation because repository restore lacked its CoroutineScope receiver. Latest fix moves that call inside coroutineScope and joins the repository Job before launching remaining restore tasks. Replacement APK build is authorized; do not monitor unless asked. Local Gradle bootstrap is unavailable. JVM tests, APK release and phone behavior remain pending. Read the latest DEVELOPMENT_LOG.md entry for exact failure evidence and paths.

Latest local-backup change: retain only the newest successful automatic backup. UUID filenames avoid same-minute overwrites; old app backups in the auto destination are removed after writing/validation, and cleanup failures preserve the new file. Source checks passed; compile/SAF device tests remain pending next build. User reports sync working. Extension backups currently save repositories and source identity, not installed APKs/reinstallation; the one-at-a-time Install feature remains planned. Its next-phase UI is a missing-extension list with Install for confirmed repo matches and Find match for manual candidate selection. Save local installed-package inventory (including unused extensions), restore repositories first, and keep the three-dot entry. See the latest ZINK_PHASES.md section; this UI is not implemented.

The user cancelled chapter-number cutoffs because fan-colour releases can exceed the main release range. Online chapter matching is unchanged. Do not hide chapters or mark them read. Missing extension installation remains a separate follow-up.

---

# Zink: Codex handoff

Repository: https://github.com/Zeus97x/Zink
Working branch: master

## Current implementation

The repository contains the Android project, Gradle wrapper, resources, tests,
artwork and GitHub Actions workflows. The original eight phases are implemented:
migration in batches of ten with Load more and scroll loading; stricter matching
and Unfound results; duplicate cleanup beside Migrate in Settings; adjustable
rounded library cards; lightning loading indicators; removal of the global
Incognito option; Zink styling; and APK delivery through GitHub releases.

The latest branding uses a clear teal Z with a lavender accent. Launcher assets
in both main and debug match the splash and in-app logo. The debug overlay and
More screen dividers were removed. Device feedback is still the source of truth
for UI and runtime behavior.

Some original verification descriptions in ZINK_PHASES.md predate successful
GitHub builds. Treat those as historical records, not the current build status.
The latest completed branding work was committed as 5386ac7e0ea8e72fea2235f9512cd71ea6e7084d.

## One follow-up for the next authorized build

Google Drive Sign in crashed because client_secrets.json was absent from the
APK. The crash occurred in GoogleDriveService.generateAuthorizationUrl before
the browser opened. Included source changes catch startup errors and explain
missing configuration instead of crashing. The APK workflow now reads the
existing CLIENT_SECRETS_TEXT repository secret when available, validates its
JSON and client fields, and packages it as an asset. Without that secret, the
workflow warns and sign-in remains unavailable. Secret availability and actual
Google Drive login/sync have not been verified. Do not claim sync is fixed just
because the crash is handled. Do not commit OAuth configuration or credentials.

The user deferred this low-priority fix to the next build. XML, YAML, embedded
Python syntax and whitespace checks passed; compilation and device testing for
the follow-up remain pending.

## Working preferences

- This repository is for Zink. Shared changes for ZDo, ZMM, ZBox and ZCalc are
  planning only here; ZRetire is excluded from the lightning loading change.
- Work through requested phases and report the remaining count at completion.
- Before an APK build, remind the user to **change model** and pause until they
  confirm the change or say continue.
- When substantial coding or building is requested in Work chat, remind the
  user to switch to the Zink Codex environment. If no Zink environment is
  available, tell them to create one; do not assume its existence.
- Push and give the GitHub build link after an authorized build. Do not monitor
  the build unless requested; the user reports success or failure.
- Do not trigger builds or publish releases for a source-only synchronization.
  The handoff commit uses [skip ci] to avoid push-triggered Actions.
- Releases use the visible name Zink and the APK filename Zink.apk. The current
  master workflow builds a debug-signed universal APK using JDK 17 and
  ./gradlew :app:assembleDebug :app:testDebugUnitTest --no-daemon --stacktrace.
- Superseded October 9: the user now explicitly requests permanent signing and update-over-install setup. See the signing preparation section below.
- Preserve user work and existing features. Do not add raw crash logs containing
  device identifiers to the public repository; the diagnosis above is sufficient.

See ZINK_PHASES.md for feature details and the next-build checklist.

### Source publication blocked (October 9)

Automatic approval review also rejected uploading the complete prepared source to Zeus97x/Zink because explicit export approval is required. Source is local only; no GitHub tree/commit/ref or APK was created. Ask for one approval covering source upload to Zeus97x/Zink and the prepared private table/function on the shared ZPet Supabase project. Source upload must use [skip ci]; preserve model-change pause before APK build. See DEVELOPMENT_LOG.md for the checkpoint and actual verification.

## Permanent signing preparation — October 9, 2026

User requested preparing everything so they only need to enter GitHub secret values.
The master APK workflow now requires SIGNING_KEYSTORE_BASE64, SIGNING_STORE_PASSWORD, SIGNING_KEY_ALIAS and SIGNING_KEY_PASSWORD. Restore the permanent key into RUNNER_TEMP, configure the existing debug variant through ZINK_SIGNING_KEYSTORE, and assign versionCode = 1000 + GITHUB_RUN_NUMBER. Preserve applicationId eu.kanade.tachiyomi.sy.debug. Missing/invalid secrets fail before the build; cleanup runs even after failure. Local builds without these env vars retain their previous behavior and are not suitable for distributing updates to the permanently signed APK.

A private signing setup ZIP with the keystore and individual copy-paste value files was generated outside the repository for the user. Never regenerate it just because a new session starts; reuse the same key. No secret values belong in source or logs. User must add the four secrets in GitHub settings. This preparation does not trigger an APK build. Preserve the model-change pause before the next requested build. Existing installations likely require one backed-up reinstall because previous runner debug keys were not preserved. Phone update compatibility and Android compilation remain unverified.

The in-app updater remains disabled for debug and still points upstream; GitHub APK updates are the first delivery method. OTA integration is deferred.

### First permanently signed build authorization

October 9: user confirms entering four signing secrets and authorizes publication/build. Publish prepared configuration without skip-ci to trigger one APK run, then provide its link. Do not monitor unless asked. Secret contents are not readable through the connector; the build validates them. Earlier source publication denial has been resolved by explicit approval.

## Sync size fix — prepared October 9

Cloud whitelist now omits untouched unread catalogue chapters while retaining read/progress, versioned/timestamped unread resets and history references. Descriptions and chapter scanlator/fetch/upload dates stay local. Existing snapshots remain compatible. Local backups unchanged. Added regression tests are pending CI because local Gradle distribution download failed. SQL trigger checks and whitespace passed. Source-only publication uses skip-ci; next authorized APK is intended to test update over the first permanent-key install plus sync with the user's actual library. Existing 64 MiB decoded/8 MiB compressed bounds remain; a large changed-state library may still need batched sync.

## October 9 logo redesign PR

User approved bolt + inkwell concept A. Artwork/runtime resources and Settings header are implemented on design/zink-bolt-inkwell; Claude build and device verification pending. See docs/ai/tasks/zink-logo-refresh/HANDOFF.md for exact placement/build steps. Do not merge automatically or replace permanent signing key. Current APK update-over-install was reported successful; cross-device sync still awaits user test.
