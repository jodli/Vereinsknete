# CLAUDE.md

## Project Overview

YogaKnete (package: `de.yogaknete.app`) — Single-user Android app for yoga instructors to track classes and generate invoices. German-only UI. Offline-first with local Room database. Project lives under `android/`.

## Build & Test

All commands from the `android/` directory. **JDK 11–17 required** (24+ breaks test runner).

```bash
gradle assembleDebug            # build
gradle testDebugUnitTest        # unit tests
gradle lintDebug                # lint
gradle installDebug             # install on device
./scripts/seed-db.sh            # seed test data
```

## Architecture & Guidelines

MVVM + Clean Architecture, Hilt DI, Jetpack Compose, Room DB. Detailed patterns, conventions and guidelines in `android/docs/`:

- `architecture-guidelines.md` — layers, state management, data flow, DI, navigation, testing
- `design-guidelines.md` — colors, components, icons, spacing, typography
- `ux-guidelines.md` — dialog types, action grouping, destructive actions

## CI/CD

- **CI** (`.github/workflows/ci.yml`): lint → unit tests → debug APK on push/PR
- **CD** (`.github/workflows/cd.yml`): signed release APK + AAB on tag push (`v*`)
