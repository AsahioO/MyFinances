# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A native Android personal finance app (Kotlin + Jetpack Compose + Room + Hilt). It auto-detects electronic money movements by reading system notifications from banking apps (starting with Nu, `com.nu.production`), removing the friction of manual entry. Cash is still logged by hand. See `plan.md` for the full product/design spec (screens, onboarding/permissions flow, color semantics, roadmap) — read it before working on UI, permissions, or notification parsing, since a lot of the "why" lives there and not in code comments.

All code, identifiers, and comments in this repo are in Spanish (e.g. `MovimientoRepository`, `ProcesarNotificacionUseCase`, `montoCentavos`). Match this convention for new code.

## Commands

```bash
./gradlew build                                    # full build
./gradlew testDebugUnitTest                         # run all JVM unit tests (Robolectric)
./gradlew testDebugUnitTest --tests "*MovimientoDaoTest"        # single test class
./gradlew testDebugUnitTest --tests "*NuNotificacionParserTest.parses ingreso*"  # single test
./gradlew lint                                      # Android lint
./gradlew connectedAndroidTest                       # instrumented tests (needs device/emulator)
```

Unit tests live under `app/src/test` and run on the JVM via Robolectric (no emulator needed) — this is where Room DAO tests and parser tests live. `app/src/test/resources/robolectric.properties` pins `sdk=35` deliberately (Robolectric needs Java 21 to simulate SDK 36; the toolchain here is Java 17) — don't bump it without checking the Java toolchain first.

## Architecture

MVVM with three layers, each with a specific reason to exist:

- **`data/`** — Room entities/DAOs/database, `MovimientoRepository`, and the `NotificationListenerService` + per-bank parsers.
- **`domain/`** — use cases (e.g. `ProcesarNotificacionUseCase`). This layer exists (unlike in simpler apps) because notification parsing/classification is real business logic that shouldn't be mixed into Room or Compose.
- **`ui/`** — Compose screens + their ViewModels, exposing `StateFlow`. Each screen owns its own ViewModel; screens only ever talk to each other through the shared repository, never directly.

**Repository pattern:** a single `MovimientoRepository` wraps all 4 DAOs (`MovimientoDao`, `CategoriaDao`, `NotificacionProcesadaDao`, `BancoConfigDao`) plus `AppDatabase` for transactions. It's constructed manually in `di/RepositoryModule.kt` (not `@Inject constructor`) so there's exactly one binding definition. ViewModels never talk to Room directly.

**DI (Hilt):** modules live in `di/` — `DatabaseModule` (Room + DAOs), `RepositoryModule` (the repository), `NotificacionModule` (multibinds the list of `ParserNotificacionBanco`). Adding a new bank parser means adding one line to `NotificacionModule`'s list — nothing else changes.

**Money is always `Long` centavos**, never `Double`/`Float` — e.g. `montoCentavos = 21900L` for $219.00. This is enforced convention, not a type wrapper, so preserve it in any new code touching amounts.

### Notification pipeline (the core of the app)

1. `FinanzasNotificationListenerService.onNotificationPosted()` runs on the listener process's main thread — it does the absolute minimum there (filter by `sbn.packageName` against known parser packages, extract title/text/key/timestamp into `NotificacionCruda`) and immediately launches a coroutine on `Dispatchers.IO` to hand off to `ProcesarNotificacionUseCase`. Never add parsing, regex, or Room writes directly in `onNotificationPosted()` — that risks an ANR or the system killing the service.
2. `ProcesarNotificacionUseCase` checks the notification's package is in the active-banks list (`BancoConfig`), finds the matching `ParserNotificacionBanco` by package name, parses, and persists.
3. Each bank implements `ParserNotificacionBanco` (`packageName`, `origen`, `parsear(...)`) as its own class — e.g. `NuNotificacionParser`. Adding a new bank (e.g. Santander) is: a new parser class implementing this interface + one line in `NotificacionModule` + a new `BancoConfig` row. No changes to the service, existing parsers, or Room.
4. Deduplication: every notification's `sbn.key` is checked/recorded in `NotificacionProcesada` inside the same Room transaction as the movement insert (`MovimientoRepository.registrarMovimientoAutomatico`, via `db.withTransaction`), so a re-posted/updated notification never creates a duplicate movement. This also naturally ignores banks' notifications mirrored through other apps (e.g. Gmail), since those packages are never in the whitelist.

### Navigation

Typed routes only — no string routes anywhere (`ui/navigation/Rutas.kt` uses `@Serializable data object`s). One nested `NavGraph` per feature (`grafoInicio()`, `grafoMovimientos()`, etc. in `GrafosFeature.kt`); `FinanzasNavHost` only wires the four feature graphs together and never declares screens itself. `DestinoPrincipal` (bottom nav) matches against the top-level graph route via `hierarchy`/`hasRoute`, not the leaf screen, so a feature's tab stays highlighted while on a secondary screen inside it. Adding a 5th bottom-nav section means: a new `Grafo*`/`Ruta*` pair, a new `grafoX()` builder, a new `DestinoPrincipal` entry — existing features untouched.

### Room schema

`exportSchema = true`; JSON schemas are versioned in `app/schemas/`. **Never use `fallbackToDestructiveMigration()`** — this app accumulates real financial history, so every schema change needs a real `Migration`. `AppDatabase.SEED_CALLBACK` seeds `banco_config` with Nu active via raw SQL in `onCreate` (a DAO isn't safely available yet at that point in the open lifecycle).

### Theming

`ui/theme/Theme.kt` builds a fixed `ColorScheme` by hand from fixed tokens — no `dynamicColor`/Material You, ever. Color carries semantic meaning (violet = bank-detected/automatic, mustard = manual/cash entry), so deriving it from the wallpaper would break that meaning. Access custom tokens via `FinanzasTheme.colores` / `FinanzasTheme.monto` (amount-specific text styles have no Material 3 equivalent), not `MaterialTheme` directly, when they exist.
