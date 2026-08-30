# Plan de desarrollo — App de Finanzas Personales

Proyecto personal de David. App nativa Android que detecta automáticamente movimientos de dinero electrónico (pagos, depósitos, transferencias) leyendo las notificaciones del sistema, para eliminar la fricción de registrar manualmente. El dinero en efectivo se sigue registrando a mano.

Bancos objetivo: **Nu** (activo desde el día uno, package `com.nu.production`) y **Santander** (futuro, diseñado para agregarse como plug-in sin tocar el resto del código).

---

## 1. Objetivo y motivación

En otras apps de finanzas, a David se le olvida entrar a registrar movimientos o le da flojera, y termina abandonando el hábito. El objetivo de esta app es automatizar por completo el flujo de dinero electrónico (vía lectura de notificaciones) y dejar el registro manual solo para efectivo, que es la única entrada que realmente requiere acción del usuario.

---

## 2. Arquitectura general

**Patrón:** MVVM con separación en capas.

- **data** — entidades de Room, DAOs, la base de datos, el repositorio que abstrae el origen de los datos, y el `NotificationListenerService` con sus parsers por banco.
- **domain** — casos de uso como "procesar notificación entrante" o "calcular balance del mes". Se incluye esta capa (a diferencia de apps más simples) porque el parseo y clasificación de notificaciones es lógica de negocio real que no debe mezclarse ni con Room ni con Compose.
- **ui** — pantallas en Compose + sus ViewModels, que exponen `StateFlow` hacia la UI.

**Patrón repositorio:** un solo `MovimientoRepository` envuelve los 4 DAOs. Los ViewModels nunca hablan con Room directamente, solo con el repositorio. Esto permite que cada pantalla nueva se conecte sin tocar las demás.

**Stack:** Kotlin + Jetpack Compose + Room, sin librerías de terceros para lo esencial.

---

## 3. Pantallas

### Navegación principal (bottom navigation)

| # | Pantalla | Contenido |
|---|----------|-----------|
| 1 | Inicio | Balance/flujo del mes, últimos movimientos, accesos rápidos |
| 2 | Movimientos | Historial completo con filtros (fecha, tipo, banco, pendientes) |
| 3 | Reportes | Gasto por categoría, comparativa mes contra mes |
| 4 | Ajustes | Hub de configuración |

### Pantallas secundarias (se apilan encima)

| # | Pantalla | Contenido |
|---|----------|-----------|
| 5 | Detalle de movimiento | Ver/editar monto, categoría, notas; marcar como revisado |
| 6 | Agregar movimiento manual | Formulario para efectivo |
| 7 | Onboarding / permisos | Solo la primera vez: pedir permisos |
| 8 | Gestión de bancos conectados | Dentro de Ajustes; activar/desactivar Nu, Santander |
| 9 | Gestión de categorías | Dentro de Ajustes; crear/editar categorías |

Las acciones puntuales (agregar, detalle) no van en el bottom nav — solo las 4 secciones que se revisan constantemente.

### Navegación escalable a futuro

1. **Un grafo de navegación por feature**, no uno solo gigante. Cada sección tiene su propio grafo anidado; el grafo principal solo los conecta. Agregar una quinta sección (ej. "Presupuestos") es un grafo nuevo, sin tocar los demás.
2. **Rutas tipadas** (clases con sus parámetros), no strings sueltos — evita errores de navegación cuando el proyecto crezca.
3. **Ajustes como contenedor de lista**, no pantalla plana — agregar una opción nueva (ej. "Exportar datos") es una fila más, no un rediseño.
4. **Cada pantalla es dueña de su propio ViewModel** — se comunican solo a través del repositorio compartido, así ninguna pantalla nueva rompe una existente.

---

## 4. Esquema de base de datos (Room)

### Tablas

**Movimiento** (tabla central)
- `id`: Long, autogenerado
- `montoCentavos`: Long — dinero se guarda en centavos como entero, nunca en `Double` (evita errores de redondeo de punto flotante)
- `tipo`: enum `INGRESO` / `EGRESO`
- `origen`: enum `NU` / `SANTANDER` / `MANUAL`
- `comercioOrigen`: String opcional — comercio o persona extraído del parseo
- `categoriaId`: Long opcional, referencia a Categoria
- `fechaMovimiento`: Long (epoch) — fecha real del movimiento
- `fechaRegistro`: Long — cuándo se insertó, útil para debug de duplicados/retrasos
- `estado`: enum `PENDIENTE_REVISION` / `CONFIRMADO` — automático entra pendiente, manual entra confirmado
- `notas`: String opcional

**Categoria**
- `id`: Long, autogenerado
- `nombre`: String
- `icono`: String (nombre de ícono Material)
- `color`: String opcional (hex, para reportes)

**NotificacionProcesada** (evita duplicados)
- `key`: String, PK — el `sbn.key` único de cada notificación
- `packageName`: String
- `fechaProcesado`: Long
- `movimientoId`: Long opcional — trazabilidad

**BancoConfig** (toggle de bancos activos en Ajustes)
- `packageName`: String, PK — ej. `com.nu.production`
- `nombreDisplay`: String — ej. "Nu"
- `activo`: Boolean

### Relaciones

- Movimiento → Categoria: muchos a uno, `ON DELETE SET NULL` (no `CASCADE`) — borrar una categoría no borra el historial, solo lo deja sin categorizar.
- Movimiento → NotificacionProcesada: uno a uno opcional, solo para automáticos.

### Índices

- `fechaMovimiento` — se ordena y filtra por fecha constantemente
- `categoriaId` — se filtra por categoría en Reportes
- `estado` — la vista de "pendientes de revisar" lo necesita

### Migraciones

`exportSchema = true` desde el día uno. Evitar `fallbackToDestructiveMigration()` aunque sea más cómodo en desarrollo temprano — la app acumula historial financiero real, así que se escriben migraciones reales desde el primer cambio de esquema.

---

## 5. Flujo de permisos y onboarding

### Los tres permisos, y por qué son distintos

1. **Acceso a notificaciones** (`NotificationListenerService`) — permiso especial, no hay diálogo del sistema. Se manda al usuario a `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS` para activarlo manualmente. No existe callback de "concedido/denegado".
2. **Exclusión de optimización de batería** — existe la API estándar (`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`), pero **Samsung tiene su propio sistema paralelo** (Device Care / apps durmientes) que esa API no cubre. Se necesita una pantalla adicional con instrucciones manuales para Samsung.
3. **`POST_NOTIFICATIONS`** — runtime permission normal con diálogo nativo, obligatorio desde Android 13+, necesario porque la app manda su propia notificación de resumen semanal.

### Orden del onboarding

1. Bienvenida — explica en 2-3 líneas qué hace la app y por qué necesita leer notificaciones (da contexto antes de pedir un permiso que suena invasivo)
2. Solicitar acceso a notificaciones (intent + verificar al regresar)
3. Solicitar exclusión de batería (intent + pantalla extra si es Samsung)
4. Solicitar `POST_NOTIFICATIONS` (diálogo nativo directo)
5. Fin → pantalla de Inicio

### Verificación de permisos (se pasa por alto fácilmente)

No hay API directa de "sí/no" para el listener de notificaciones — se consulta `NotificationManagerCompat.getEnabledListenerPackages()` y se revisa si el paquete propio está en la lista. Para batería sí existe consulta directa: `PowerManager.isIgnoringBatteryOptimizations()`.

Se revisa en dos momentos:
- Al volver de cada pantalla de permisos durante el onboarding
- **Cada vez que se abre la app** (`onResume` de la actividad principal) — Samsung puede revocar el acceso solo, o el usuario puede desactivarlo por error meses después

### No bloqueante

El registro manual no depende de estos permisos, así que el onboarding no obliga a aceptarlos. Si el usuario los salta o los niega, pasa a Inicio de todos modos, con un banner descartable ("La detección automática está desactivada — actívala aquí") que reaparece mientras el permiso siga sin concederse.

### Sin selección de bancos en el onboarding

Como hoy solo se usa Nu, no tiene sentido meter un paso de selección — sería fricción de más para una decisión de un solo elemento. `BancoConfig` para Nu se activa automático la primera vez; Santander se agrega después desde "Gestión de bancos conectados" en Ajustes.

---

## 6. Parser de notificaciones

### Filtro por app de origen

El `NotificationListenerService` recibe todas las notificaciones del sistema. Se filtra por `packageName` contra una lista blanca (`com.nu.production` para Nu). Esto también resuelve el problema de duplicados por Gmail: cuando Nu manda el mismo aviso por correo, la notificación de Gmail (`com.google.android.gm`) simplemente se ignora sin lógica extra.

### Formatos reales confirmados de Nu (capturados de notificaciones reales)

**Ingreso (transferencia recibida):**
- Título: `¡Recibiste una transferencia!`
- Texto: `Recibiste $700.00 en tu Cuenta Nu.`

**Egreso (compra):**
- Título: `Compra aprobada por $219.00`
- Texto: `Compraste en PAYPAL *NVIDIA CORP con tu tarjeta de Cuenta Nu por $219.00 el 29/08/2026 13:35.`

El segundo patrón ya trae comercio, monto y fecha/hora en el mismo texto, separados por palabras clave fijas ("Compraste en", "con tu tarjeta de Cuenta Nu por", "el") — el parseo corta el texto en esos puntos fijos, sin necesidad de inventar delimitadores.

### Deduplicación

Cada notificación trae un `key` único (`sbn.key`), que se guarda en `NotificacionProcesada` antes de insertar un movimiento — así se evita registrar el mismo evento dos veces si el sistema actualiza la misma notificación.

### Diseño extensible

Agregar Santander en el futuro implica: agregar su `packageName` a la lista blanca, escribir sus reglas de parseo específicas, y una fila nueva en `BancoConfig` — sin tocar Room, la UI, ni el parser de Nu.

---

## 7. Rendimiento y optimización

Este es el punto que distingue a esta app de un CRUD normal, porque `onNotificationPosted()` corre en el hilo principal del proceso del listener.

- **No hacer trabajo pesado en el listener.** Solo se capturan los datos crudos (texto, key, timestamp) en `onNotificationPosted()`, y se lanza una coroutine en `Dispatchers.IO` para parsear y guardar. Hacer parseo con regex o escritura a Room directamente ahí puede causar ANR o que el sistema mate el servicio.
- **Room reactivo.** Las queries que alimentan la UI usan `Flow` (sin polling).
- **Paginación.** Si el historial crece mucho, usar Paging 3 en vez de cargar todos los movimientos en memoria.
- **Compose eficiente.** Claves estables (`key = movimiento.id`) en `LazyColumn` para evitar recomposiciones innecesarias.
- **Batería y persistencia del servicio.** Un `NotificationListenerService` no se ve muy afectado por Doze porque el sistema lo despierta al llegar una notificación. El riesgo real es que fabricantes como Samsung maten agresivamente servicios en background — de ahí la necesidad de guiar al usuario a excluir la app manualmente (ver sección de permisos).

---

## 8. UX / UI y diseño visual

### Decisión clave: qué mostrar como número protagonista

La app no mueve dinero, solo lo detecta — a diferencia de una wallet/banca real conectada a cuentas. Por eso:

- **El "flujo del mes" (ingresos vs. egresos) es el protagonista**, no un "balance real" — es 100% preciso porque depende solo de lo que la app ya capturó, sin riesgo de desincronizarse si se pierde una notificación (batería, Samsung matando el servicio).
- El balance real queda como dato secundario opcional, para cuando el usuario quiera capturar un saldo inicial manualmente.

### Paleta con significado semántico

El color no es solo decorativo, comunica el origen del dinero:
- **Violeta cálido** → detectado automáticamente (Nu / bancos)
- **Mostaza** → registrado manualmente (efectivo)
- Fondo cálido con gradiente lavanda → crema → durazno, inspirado en una referencia visual tipo wallet app, pero adaptado (sin números de tarjeta reales, ya que la app no tiene acceso a esos datos).

La paleta es **fija**, definida por el diseño de la app — no se usan colores dinámicos derivados del wallpaper del sistema.

### Tarjeta de banco (prototipo)

El diseño actual de la tarjeta de banco (degradado morado) es solo un **prototipo visual** para esta etapa, no un diseño final. A futuro se contempla permitir personalizar la apariencia de cada tarjeta (color, estilo).

### Tipografía

- **Space Grotesk** — headers y títulos de sección
- **IBM Plex Mono** — montos y números, refuerza la sensación de "libro de contabilidad" (datos, no decoración)
- **Inter** — cuerpo de texto y UI general
- Montos grandes con centavos en tamaño reducido (ej. "$23,580.**59**")

### Aprovechando que es nativo

- **Widget de pantalla de inicio (Glance)** — balance/flujo visible sin abrir la app, ataca directo el problema original de "se me olvida entrar"
- **App Shortcut** (mantener presionado el ícono) — acceso directo a "Agregar gasto en efectivo"
- **Predictive back gesture** (Android 14+) — transición de "regresar" nativa del sistema
- **Notificación propia de resumen semanal** — refuerza el hábito sin que el usuario tenga que entrar
- **Transición de elementos compartidos**, fluida y bien optimizada, entre la lista de movimientos y el detalle (equivalente nativo: `SharedTransitionLayout` de Compose) — el ícono, título y monto de la fila "viajan" visualmente hacia la pantalla de detalle en vez de solo aparecer. Prioridad en la implementación: mantener la recomposición al mínimo durante la animación y evitar cálculos de layout pesados en cada frame, para que el movimiento se sienta fluido y bien renderizado en cualquier gama de dispositivo.

---

## 9. Resumen de próximos pasos

1. Estructura base del proyecto (Gradle, paquetes `data` / `domain` / `ui`)
2. Esquema de Room + migraciones iniciales
3. `NotificationListenerService` + parser de Nu
4. Flujo de onboarding y verificación de permisos
5. Pantallas principales (Inicio, Movimientos, Reportes, Ajustes) en Compose
6. Transición de elementos compartidos en el detalle de movimiento
7. Widget de pantalla de inicio y App Shortcut
8. Notificación de resumen semanal