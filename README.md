# AppFinanzas

App nativa Android para registro personal de gastos. Automatiza el dinero electrónico leyendo notificaciones del sistema y deja solo el efectivo como registro manual. Elimina la fricción de "se me olvida entrar y termino abandonando el hábito" (`plan.md`).

Bancos: **Nu** activo (`com.nu.production`) y **Santander** futuro como plug-in sin tocar el resto del código.

## Características

- **Detección automática** vía `NotificationListenerService` + parsers por banco (`data/notificacion/parser/`).
- **Registro manual** para efectivo (formulario dedicado).
- **Flujo del mes** (ingresos vs egresos) como protagonista, no balance real — es 100% preciso aunque se pierda una notificación (`plan.md:187`).
- **4 pantallas principales** (bottom nav): Inicio, Movimientos, Reportes, Ajustes + secundarias: Detalle, Agregar manual, Onboarding, Gestión bancos/categorías.
- **Theming fijo** violeta (automático) / mostaza (manual) vía `FinanzasTheme`, sin `dynamicColor`. Tipografías: Space Grotesk / IBM Plex Mono / Inter (`plan.md:194`).
- **Nativo**: Widget Glance, App Shortcut "Agregar gasto en efectivo", predictive back (Android 14+), notificación resumen semanal, `SharedTransitionLayout` entre lista y detalle.

## Stack

Un solo módulo `app` — `com.finanzas.app` — `compileSdk 37 / minSdk 29 / targetSdk 36 / Java 17` (`app/build.gradle.kts:11-17`).

| Herramienta | Versión |
|---|---|
| AGP | 9.3.2 |
| Kotlin | 2.3.21 |
| KSP | 2.3.11 |
| Room | 2.8.4 |
| Hilt | 2.60.1 |
| Compose BOM | 2026.08.00 |
| Navigation Compose | 2.10.0 |
| Gradle Wrapper | 9.5.0 |

`org.gradle.caching=true` + `configuration-cache=true` (`gradle.properties`).

## Arquitectura

MVVM por capas `data / domain / ui` (`AGENTS.md`):

- **data/local** — Room (entidades, DAOs, `AppDatabase.kt`), **data/notificacion** — `FinanzasNotificationListenerService` + parsers, **data/repository** — 3 repos: `MovimientoRepository` (movimientos+categorías+dedup+bancos), `CuentaRepository`, `ReportesRepository`. ViewModels nunca tocan DAOs.
- **domain/notificacion** — `ParserNotificacionBanco` + `ProcesarNotificacionUseCase` (toda la lógica de parseo aquí).
- **di/** — `DatabaseModule` (Room+DAOs), `RepositoryModule` (repos sin `@Inject constructor`), `NotificacionModule` (multibind `List<ParserNotificacionBanco>` → banco nuevo = 1 clase + 1 línea).
- **ui/navigation** — rutas tipadas `@Serializable` (`Rutas.kt`, respetar `proguard-rules.pro`), un grafo anidado por feature (`GrafosFeature.kt`), `FinanzasNavHost` solo los conecta. `ui/` por feature, genéricos en `ui/components/`, modelos en `domain/model/`.
- Agregación siempre en SQL (`GROUP BY`), listados con `LIMIT` en query, nunca `take()` en memoria.

## Base de datos

`exportSchema=true` (`app/build.gradle.kts:58`), schemas en `app/schemas/` (v1). Nunca `fallbackToDestructiveMigration()`.

| Tabla | Clave |
|---|---|
| **Movimiento** | `id`, `montoCentavos: Long` (nunca `Double`), `tipo` INGRESO/EGRESO, `origen` NU/SANTANDER/MANUAL, `comercioOrigen`, `categoriaId FK SET NULL`, `fechaMovimiento`, `fechaRegistro`, `estado` PENDIENTE_REVISION/CONFIRMADO, `notas` |
| **Categoria** | `id`, `nombre`, `icono` (Material), `color` hex |
| **NotificacionProcesada** | `key` PK (`sbn.key`), `packageName`, `fechaProcesado`, `movimientoId` |
| **BancoConfig** | `packageName` PK, `nombreDisplay`, `activo` |

Índices en `fechaMovimiento`, `categoriaId`, `estado`. Seed en `AppDatabase.SEED_CALLBACK:50` inserta `com.nu.production / Nu / activo=1` con SQL crudo.

## Pipeline de notificaciones

No romper (`FinanzasNotificationListenerService.kt:49`, `ProcesarNotificacionUseCase.kt:18`, `MovimientoRepository.kt:87`):

1. `onNotificationPosted()` corre en main thread del listener — solo filtro `packageName in paquetesConocidos`, resto a `scope.launch(Dispatchers.IO) { procesarNotificacion(evento) }`. Sin regex ni Room ahí.
2. `ProcesarNotificacionUseCase` respeta `paquetesDeBancosActivos()` (toggle `BancoConfig`), busca parser por `packageName`, parsea y persiste vía `registrarMovimientoAutomatico` con `db.withTransaction { yaProcesada? null : insert }` — dedup atómica por `sbn.key`.
3. Filtro por `packageName` ya ignora duplicados de Gmail (`com.google.android.gm`).
4. Formatos Nu reales (`NuNotificacionParser.kt:76`): `Recibiste $700.00 en tu Cuenta Nu.` y `Compraste en <comercio> con tu tarjeta de Cuenta Nu por $<monto> el <dd/MM/yyyy HH:mm>.` — parseo por keywords fijas.
5. Extensible: Santander = `packageName` + parser + fila `BancoConfig`, sin tocar Room/UI/parser Nu.

## Permisos y onboarding

1. **Acceso a notificaciones** — sin diálogo, intent a `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`, verificación vía `NotificationManagerCompat.getEnabledListenerPackages()`.
2. **Batería** — `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` + pantalla extra Samsung Device Care (API estándar no cubre Samsung), verificación `PowerManager.isIgnoringBatteryOptimizations()`.
3. **`POST_NOTIFICATIONS`** — runtime dialog (Android 13+) para resumen semanal.

Orden: Bienvenida → notificaciones → batería (+ Samsung) → `POST_NOTIFICATIONS` → Inicio. Re-verificación en `onResume` (Samsung puede revocar solo). No bloqueante: sin permisos el registro manual sigue funcionando, banner descartable invita a activar.

## Inicio rápido

Requisitos: Android Studio Ladybug+, JDK 17.

```bash
git clone <repo> && cd appfinanzas

# APK debug
./gradlew assembleDebug --console=plain

# Tests unitarios (JVM, sin emulador — Robolectric sdk=35 en robolectric.properties:5)
./gradlew testDebugUnitTest --console=plain
./gradlew testDebugUnitTest --tests "*MovimientoDaoTest" --console=plain
./gradlew testDebugUnitTest --tests "*NuNotificacionParserTest.parses ingreso*" --console=plain

# Si el cache oculta fallos
./gradlew testDebugUnitTest --rerun-tasks --console=plain

# Lint / instrumentados
./gradlew lint
./gradlew connectedAndroidTest  # requiere dispositivo/emulador
```

## Estructura

```
app/src/main/java/com/finanzas/app/
├── data/{local,notificacion/parser,repository}
├── domain/{model,notificacion}
├── di/{DatabaseModule,RepositoryModule,NotificacionModule}
└── ui/{inicio,movimientos,reportes,ajustes,navigation,components,theme}
FinanzasApplication + MainActivity + FinanzasNavHost (AndroidManifest.xml)
app/schemas/  # Room schemas versionados
```

## Roadmap

`plan.md:9` — 1. Base Gradle/paquetes → 2. Room+migraciones → 3. Listener+parser Nu → 4. Onboarding/permisos → 5. Pantallas Compose → 6. Shared transition → 7. Widget+Shortcut → 8. Resumen semanal.

---
Proyecto personal de David. Ver `plan.md` como spec de producto y `AGENTS.md` como guía de arquitectura/comandos.
