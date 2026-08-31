# AGENTS.md — AppFinanzas

Android nativo Kotlin + Compose + Room + Hilt. Repo en español (`MovimientoRepository`, `montoCentavos`). `plan.md` es la spec de producto — léelo antes de tocar UI/permisos/parsers.

## Módulo y stack
- Un solo módulo `app` (`settings.gradle.kts:18`). Namespace/appId `com.finanzas.app`, `compileSdk 37 / minSdk 29 / targetSdk 36 / Java 17` (`app/build.gradle.kts:11-17,35`).
- Versiones en `gradle/libs.versions.toml`: AGP 9.3.2, Kotlin 2.3.21, KSP 2.3.11, Room 2.8.4, Hilt 2.60.1, Compose BOM 2026.08.00. Wrapper Gradle 9.5.0. `org.gradle.caching=true` + `configuration-cache=true` (`gradle.properties`).

## Comandos
```bash
./gradlew testDebugUnitTest --console=plain                          # todos los unit tests (JVM, sin emulador)
./gradlew testDebugUnitTest --tests "*MovimientoDaoTest" --console=plain
./gradlew testDebugUnitTest --tests "*NuNotificacionParserTest.parses ingreso*" --console=plain
./gradlew assembleDebug --console=plain                               # APK debug
./gradlew lint                                                         # Android lint
./gradlew connectedAndroidTest                                         # instrumentados (requiere dispositivo)
```
Si el cache oculta fallos, añade `--rerun-tasks`.

## Arquitectura
- `app/src/main/java/com/finanzas/app/{data,domain,ui,di}` — MVVM. `FinanzasApplication` + `MainActivity` + `FinanzasNavHost` son los entrypoints (`AndroidManifest.xml`).
- `data/local` = Room (entidades/DAOs/`AppDatabase.kt`), `data/notificacion` = `FinanzasNotificationListenerService` + `parser/`, `data/repository/` = 3 repos por responsabilidad: `MovimientoRepository` (movimientos + categorías + dedup + bancos), `CuentaRepository` (wallets), `ReportesRepository` (agregados y listados LIMIT para UI). ViewModels nunca tocan DAOs. Los repos se construyen en `di/RepositoryModule.kt` (no `@Inject constructor`). Agregación siempre en SQL (`GROUP BY`), no en memoria; listados de UI con `LIMIT` en la query, nunca `take()` sobre la lista completa.
- `domain/notificacion` = `ParserNotificacionBanco` + `ProcesarNotificacionUseCase` — toda la lógica de parseo vive aquí.
- `di/`: `DatabaseModule` (Room + DAOs), `RepositoryModule` (repo), `NotificacionModule` (multibind `List<ParserNotificacionBanco>`). Banco nuevo = clase `ParserNotificacionBanco` + 1 línea en `NotificacionModule.proveerParsersBancarios`.
- `ui/navigation`: rutas tipadas (`Rutas.kt`, `@Serializable` — respetar `app/proguard-rules.pro`), un grafo anidado por feature (`GrafosFeature.kt: grafoInicio()` etc.), `FinanzasNavHost` solo los conecta. `DestinoPrincipal` matchea por `hasRoute` sobre el grafo, no la pantalla hoja.
- `ui/` organizada por feature: `ui/inicio/components/`, `ui/reportes/components/`, etc. Componentes genéricos (reusables por 2+ features) van en `ui/components/`. `domain/common` no existe: modelos de dominio en `domain/model/` (ej. `RangoFechas`, con clock inyectable por parámetro).

## Base de datos
- `exportSchema=true` (`app/build.gradle.kts:58`), schemas en `app/schemas/` (v1 actual). **Nunca** `fallbackToDestructiveMigration()` — historial financiero real, cada cambio lleva `Migration`.
- `AppDatabase.SEED_CALLBACK` (`AppDatabase.kt:50`) inserta `com.nu.production / Nu / activo=1` con SQL crudo (DAO no disponible en `onCreate`).
- Dinero siempre `Long montoCentavos`, nunca `Double`. FK `Movimiento.categoriaId -> Categoria ON DELETE SET NULL`. Índices en `fechaMovimiento`, `categoriaId`, `estado`.

## Pipeline de notificaciones (no romper)
- `FinanzasNotificationListenerService.onNotificationPosted()` corre en main thread del listener (`FinanzasNotificationListenerService.kt:49`) — solo filtro `packageName in paquetesConocidos` ahí; todo lo demás va a `scope.launch(Dispatchers.IO) { procesarNotificacion(evento) }`. No añadas regex ni Room en el callback.
- `ProcesarNotificacionUseCase` (`ProcesarNotificacionUseCase.kt:18`) respeta `paquetesDeBancosActivos()` (toggle `BancoConfig`), busca parser por `packageName`, parsea y persiste vía `MovimientoRepository.registrarMovimientoAutomatico` (`MovimientoRepository.kt:87`) que hace `db.withTransaction { yaProcesada? null : insert }` — dedup atómica por `sbn.key` en `NotificacionProcesada`.
- Formatos Nu reales (`NuNotificacionParser.kt:76`): `Recibiste $700.00 en tu Cuenta Nu.` y `Compraste en <comercio> con tu tarjeta de Cuenta Nu por $<monto> el <dd/MM/yyyy HH:mm>.` Filtrar por `packageName` ya ignora duplicados vía Gmail.
- Permisos sin API directa para listener: `NotificationManagerCompat.getEnabledListenerPackages()` + `PowerManager.isIgnoringBatteryOptimizations()`; re-verificar en `onResume` (Samsung Device Care revoca solo).

## Theming
- `ui/theme/Theme.kt` construye `ColorScheme` fijo a mano — nunca `dynamicColor`. Violeta = automático, mostaza = manual (`plan.md:194`). Usar `FinanzasTheme.colores`/`FinanzasTheme.monto`, no `MaterialTheme` directo.

## Tests
- Unit tests en `app/src/test` con Robolectric + `Room.inMemoryDatabaseBuilder(...).allowMainThreadQueries()` (`MovimientoDaoTest.kt:32`). No necesitan emulador (`testOptions.isIncludeAndroidResources=true`).
- `app/src/test/resources/robolectric.properties:5` fija `sdk=35` porque el toolchain es Java 17 y Robolectric necesita Java 21 para simular SDK 36. No subir sin migrar toolchain.

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

When the user types `/graphify`, use the installed graphify skill or instructions before doing anything else.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- Dirty graphify-out/ files are expected after hooks or incremental updates; dirty graph files are not a reason to skip graphify. Only skip graphify if the task is about stale or incorrect graph output, or the user explicitly says not to use it.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
