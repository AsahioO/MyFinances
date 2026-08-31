package com.finanzas.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// --- Tokens fijos de la paleta ---
// La paleta es fija por diseño: no se deriva del wallpaper (nada de Material You).

val Ink = Color(0xFF2B1F33)
val SurfaceCrema = Color(0xFFFFFEFB)
val SurfaceLavanda = Color(0xFFF6EDE6)
val Violeta = Color(0xFF7A5490)
val Mostaza = Color(0xFFD9A441)
val Menta = Color(0xFF6FA087)
val Coral = Color(0xFFD9776B)
val TextoPrincipal = Color(0xFF2B1F33)
val TextoSecundario = Color(0xFF8A7D91)

/**
 * Colores cuyo significado NO es decorativo: comunican el origen del dinero y
 * el signo del movimiento. Material 3 no tiene un rol para esto, asi que viajan
 * aparte del ColorScheme.
 */
@Immutable
data class ColoresSemanticos(
    /** Detectado automaticamente leyendo notificaciones (Nu, Santander). */
    val origenAutomatico: Color = Violeta,
    /** Capturado a mano por el usuario (efectivo). */
    val origenManual: Color = Mostaza,
    /** Dinero que entra. */
    val ingreso: Color = Menta,
    /** Dinero que sale. */
    val egreso: Color = Coral,
    /** Movimiento automatico todavia sin revisar. */
    val pendienteRevision: Color = Mostaza,
    val textoPrincipal: Color = TextoPrincipal,
    val textoSecundario: Color = TextoSecundario,
)

val LocalColoresSemanticos = staticCompositionLocalOf { ColoresSemanticos() }