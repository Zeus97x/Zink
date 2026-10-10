# ZInk approved bolt + inkwell logo — Claude build handoff

Status: artwork and runtime wiring implemented; signed CI build passed 2026-10-10 (Build Zink APK run 38015885987, release zink-38015885987-1, versionCode 1013, same signer as 1012); device verification pending.
Branch: design/zink-bolt-inkwell. Base: master at 5fa043e1e5a92c0d9c84d42d6f715df6054c774b.
User selected concept A: amber lightning striking an ivory inkwell, controlled splash, charcoal background. Use it for app launcher, startup/splash, settings and existing More/About branding. The comparison board was a design reference; production artwork is the editable SVG/vector rendition, with additional safe padding.

## Source and placement

- artwork/zink-icon.svg: canonical charcoal icon; artwork/zink-mark.svg: transparent mark; artwork/zink-icon-preview.png: review render.
- scripts/artwork/generate_zink_icons.py: reproducible SVG, Android vectors and density raster generator (cairosvg + Pillow).
- app/src/main/res/drawable/ic_zink_logo.xml: shared full-colour foreground used by main/debug adaptive launchers, ic_tachi_splash.xml, and LogoHeader.
- app/src/main/res/drawable/ic_zink_monochrome_launcher.xml: themed launcher silhouette.
- app/src/main/res/drawable/ic_tachi.xml: white notification silhouette.
- main/debug drawable/ic_launcher_foreground.xml: replace remaining legacy foreground definitions.
- main/debug mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher{,_round}.png: legacy launcher and bitmap notification branding.
- main drawable-{density}/splash_icon.webp and main/ic_launcher-web.png: regenerate remaining raster branding.
- LogoHeader.kt: charcoal rounded badge keeps ivory visible in light themes; already used in More and About.
- SettingsMainScreen.kt: shared header above settings rows; two-pane selected-row scroll offset updated from two utility rows to three header/utility rows.

## Build this PR

1. Read AGENTS.md, DEVELOPMENT_LOG.md and docs/CODEX_HANDOFF.md. Check open PRs/latest master; do not overwrite unrelated work.
2. Check out design/zink-bolt-inkwell. Review icon resources and changed Kotlin, fix any compilation/lint failure on this same branch.
3. Use existing GitHub Actions Build Zink APK workflow with workflow_dispatch on THIS branch. Example: `gh workflow run build-apk.yml --repo Zeus97x/Zink --ref design/zink-bolt-inkwell`. It compiles, runs JVM tests, uses the four existing signing secrets and publishes the APK artifact and a GitHub release as configured. Do not expose secret values or replace the signing key. Do not use a local temporary debug-key APK for the update test.
4. Follow the branch run; fix failures and rerun as needed. Provide run, artifact/release and APK links. Report actual checks only. Preserve application ID eu.kanade.tachiyomi.sy.debug and workflow versionCode scheme; do not merge the PR.
5. Update development log and this task status with real build results.

## Device acceptance

- Update over current signed install without uninstalling; retain settings/library.
- Launcher square, circular and themed masks show the complete bolt/inkwell.
- Cold start splash, More, About and Settings display the approved mark.
- Light/dark themes keep branding visible; verify two-pane settings selection/scroll alignment.
- Trigger sync/backup notification; large icon matches and small icon is a white silhouette.

Local verification: generated PNG/WebP outputs decoded and dimensions checked; XML parsed; SVG preview reviewed; whitespace checks passed. Android compilation pending Claude/CI. Two-device cloud sync confirmation remains pending separately.

## Build result (2026-10-10)

- Fixed import order in LogoHeader.kt and SettingsMainScreen.kt (commit b19168e).
- Build Zink APK run https://github.com/Zeus97x/Zink/actions/runs/38015885987 succeeded (assembleDebug + JVM tests). Release: https://github.com/Zeus97x/Zink/releases/tag/zink-38015885987-1. Signer cert SHA-256 matches previous release; application ID unchanged; versionCode 1013.
- PR CI spotlessCheck still red only on unrelated master files (see DEVELOPMENT_LOG.md). Device acceptance above still pending.
