# Zink development instructions

Repository: Zeus97x/Zink. Preserve project identity and existing features. Read existing project guidance before changes.

## Development recovery log (shared Zeus app and website rule)

- User requirement: maintain root `DEVELOPMENT_LOG.md` as a running engineering and recovery record for every app, and for websites when their source is put in GitHub.
- Read the log and existing handoff/phase notes before work. Inspect the current source and Git history; reconcile stale notes before resuming.
- Update the log in the same commit as each meaningful change, fix, phase, configuration change, asset update or workflow change. Also record incomplete work and blockers before a handoff.
- Record date (America/Toronto), request/problem, what changed and why, how it was implemented, exact affected paths, relevant commit/PR/build references, verification actually performed, remaining issues and the concrete next step.
- Preserve previous entries. Clearly distinguish implemented, planned, unverified and blocked work. Never invent historical implementation details or claim checks passed without evidence.
- Keep project-specific handoff and phase files consistent. Link existing history rather than replacing it.
- Commit source/assets/configuration with the log: this Markdown file explains recovery but cannot replace missing project files. Record missing source honestly.
- Never record passwords, API keys, tokens, signing keys or other secrets. Refer only to configuration names and approved secret storage.
- When creating another Zeus app or website repository, carry over this rule and initialize its own `DEVELOPMENT_LOG.md`.

## Permanent AI development team (2026-10-09)
Read `.github/ai/README.md`, PROJECT_STATUS.md, TASK_BOARD.md and ROADMAP.md before development. The user's current master workflow controls AI responsibilities, approval, usage, phases and PR integration; initialize the same workspace for future repos. Preserve existing project-specific guidance and logs. Current user instructions take precedence over historical rules.
