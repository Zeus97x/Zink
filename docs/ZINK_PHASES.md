# Zink Phase Progress

All changes are local. Do not push, create a release, or build an APK until requested.

## Phases 1 and 2: Migration Batching and Matching

Implemented, pending compilation and device testing:

- Search ten titles at a time, with an explicit Load 10 more button and swipe-to-bottom loading.
- Scrolling/composition alone no longer starts an unrequested batch.
- Strict matching scores even a single returned candidate instead of assuming a perfect match.
- Unfound migrations stay visible for retry or manual matching.
- A batch with no matches can still advance to the next batch.
- Bulk migration removes completed entries while retaining unloaded and unfound titles.
- Prevent overlapping batch search and bulk migration.

## Phase 3: Library Tools

Implemented, pending compilation and device testing:

- Migrate and Delete duplicates are adjacent entries at the top of Settings.
- Migrate selects library titles and opens the existing migration configuration.
- Duplicate candidates appear side by side with source and reading progress.
- Matching ignores case, punctuation, spacing, and Unicode presentation differences.
- Edition words and numbers remain significant; no fuzzy automatic deletion.
- Removal requires confirmation and preserves downloads and reading history.
- The final copy in a duplicate group cannot be removed from this tool.
- Results show ten groups at a time, with Load 10 more and swipe-to-bottom loading.
- Empty, loading, and retryable failure states are included.

## Phase 4: Library Cards

Implemented, pending compilation and device testing:

- Rounded, larger library rows and grid cards.
- Card size in the library's top-right overflow menu.
- Compact, Comfortable, and Large sizes are saved across app restarts.
- Size changes use adaptive grid widths and reset explicit column counts.
- Explicit column counts can still be set in the existing library display settings.
- Shared Browse/search card defaults are unchanged.

## Phase 5: Lightning Loading and Refresh

Implemented in Zink, pending compilation and device testing:

- Shared pulsing bolt for indeterminate loading, with progress accessibility semantics.
- Determinate progress fills the bolt, including downloads and reader pages.
- Compose loading states, View-based reader transitions, and pull-to-refresh use the bolt.
- The home-screen widget uses a static loading bolt, compatible with Glance/RemoteViews.
- Refresh/retry icons and refresh notification assets use lightning icons.
- Fixed page-preview percentage conversion from integer percent to a 0..1 fraction.
- Applying the same convention in the other app repositories remains separate work; ZRetire is excluded.

## Phase 6: More Menu Cleanup

Implemented, pending compilation and device testing:

- Removed the global Incognito toggle and its More screen bindings.
- Reset any saved global Incognito state at application startup to restore reading history.
- Downloaded only, queue, categories, statistics, data/storage, Batch Add, and Settings remain available.
- Existing history and privacy-related backend data are preserved.

## Phase 7: UI Polish

Implemented, pending compilation and device testing:

- Zink branding in the app name and More header.
- Default charcoal surfaces with teal accents and a lavender download accent.
- New installs use the Zink default palette; saved alternate theme choices remain available.
- Rounded More rows and migration comparisons; more space between library grid cards.
- The app's existing explicit AMOLED option is preserved.

## Phase 8: APK Release Workflow

Implemented as a APK workflow, not run:

- Build and unit-test the universal debug APK without adding signing secrets.
- Upload Zink.apk as both a workflow artifact and a GitHub release asset.
- Use Zink as the visible release title, with a unique internal tag for each run.
- No release publication from pull-request validation or ordinary pushes via this new workflow.
- Compilation and actual GitHub execution must be verified on an authorized build.
- Debug signing does not promise installation over APKs signed by other keys.
- The pre-existing release-branch builder also names its release and assets Zink;
  its existing signing/integration-secret requirements are unchanged.

## Static Verification Completed

- Changed-file whitespace check passes.
- Shared string resources parse, have unique names, and identify the app as Zink.
- Manual workflow YAML parses and only has workflow_dispatch as its trigger.
- Universal APK selection was executed against a synthetic split-APK metadata fixture.
- No APK compilation, Gradle unit tests, device tests, push, or workflow run was performed.

## Other App Phase Additions

The corrected request is planning only for ZDo, ZMM, ZBox, and ZCalc. Append
lightning loading/refresh and app-name-only APK release delivery to their existing
queues. No app implementation or build was made in those projects from this chat.
Do not add new signing keys or update-over-install work. ZRetire remains excluded
from the lightning change.

## Verification Before Release

- Compile and run the duplicate title-key unit tests.
- Check exact duplicates, three-copy groups, and titles with Colored/version suffixes.
- Cancel removal, confirm removal, and verify history/downloads remain intact.
- Check empty libraries, missing sources, and database failures.
- Confirm loading another batch requires a click or scrolling to the footer.
- Test all sizes in list and grid modes, portrait and landscape, and larger system fonts.
- Restart the app and verify the selected size persists.
- Check Settings navigation on phones and two-pane layouts.
- Check bolt loading and progress at 0%, 25%, 50%, and 100% with accessibility enabled.
- Test pull-to-refresh, cancellation, download menus, and reader chapter transitions.
- Start with an old saved Incognito preference enabled and verify history recording resumes.

No APK build, push, or release has been performed for these phases.

## Authorized build and push — October 7, 2026

User authorized completing all phases, building, and pushing Zink. The APK workflow now runs on master code pushes as well as manual dispatch, and publishes a release titled Zink only after build and unit tests pass. Local Gradle execution was attempted but stopped before compilation because services.gradle.org is unreachable from this workspace. GitHub Actions provides the pending compilation and unit-test validation; device testing remains pending. Other apps remain planning-only.

## Next build: Google Drive sign-in crash fix

Deferred by the user on October 7, 2026; low priority. Include with the next
authorized build, without triggering a separate push or build now.

- Crash log confirms that tapping Google Drive Sign in throws
  `FileNotFoundException: client_secrets.json` before opening the login browser.
- Prepared locally: include the existing `CLIENT_SECRETS_TEXT` GitHub Actions
  secret as the APK's Google OAuth configuration when available, validating its
  JSON and required client fields without printing credentials.
- Prepared locally: catch sign-in startup failures and display a clear error
  instead of crashing. A missing configuration explicitly reports that Google
  Drive sign-in is unavailable in that build.
- Google Drive sync requires a valid configured `CLIENT_SECRETS_TEXT` secret;
  its availability has not been verified. Do not treat the crash guard as proof
  that Google Drive authentication or sync works.
- XML, workflow YAML, embedded Python syntax, and whitespace checks passed.
  Compilation and device testing remain pending for these changes.
- On the next build, test missing configuration, browser sign-in, and actual
  sync with valid configuration. Follow the model-change pause and the user's
  push-and-link preference; do not monitor the build unless requested.

One follow-up item remains, queued for the next build.
