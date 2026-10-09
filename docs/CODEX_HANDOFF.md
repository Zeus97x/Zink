# Current status — October 9, 2026

The user explicitly approved source publication to Zeus97x/Zink, the isolated sync schema on shared ZPet, and continuing the APK build. The earlier publication/backend approval blockers are resolved; do not ask again. Latest cloud scope is ONLY library membership, chapter read/progress and reading history. Extensions, repositories, categories, tracking, settings, bookmarks and notes stay in local backups. The cloud whitelist also strips these fields from old remote snapshots before merging/restoring.

Migration zink_owner_sync is deployed on sihbduemrgwqdznkxykx. Rollback-only authenticated SQL tests passed initial insert, conflict rejection, revision increments, owner isolation and denied cross-owner writes. RLS is enabled with three owner policies; anonymous SELECT/RPC access is revoked. No test snapshots remain. Existing unrelated ZPet advisor findings were not changed.

Local bookshelf, Updates layout and library/reading-only sync are published in c97c90d2249008959bc0c0618dc0c004f8bc50ae. APK workflow run https://github.com/Zeus97x/Zink/actions/runs/37887215949 failed in source-local: two nullable nameWithoutExtension lowercase calls in LocalCoverManager.kt. Both now use safe calls with an empty fallback; this fix triggers a replacement build. Do not monitor unless asked. Gradle distribution download is unavailable here, so Android compilation, JVM tests and device behavior remain unverified until CI/phone testing. Give the build link after pushing; do not monitor unless asked. Read the latest DEVELOPMENT_LOG.md entry for precise paths and checks.

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
- Do not add signing keys, secrets or update-over-install work to the plans.
- Preserve user work and existing features. Do not add raw crash logs containing
  device identifiers to the public repository; the diagnosis above is sufficient.

See ZINK_PHASES.md for feature details and the next-build checklist.

### Source publication blocked (October 9)

Automatic approval review also rejected uploading the complete prepared source to Zeus97x/Zink because explicit export approval is required. Source is local only; no GitHub tree/commit/ref or APK was created. Ask for one approval covering source upload to Zeus97x/Zink and the prepared private table/function on the shared ZPet Supabase project. Source upload must use [skip ci]; preserve model-change pause before APK build. See DEVELOPMENT_LOG.md for the checkpoint and actual verification.
