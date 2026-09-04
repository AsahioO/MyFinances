package com.finanzas.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

/**
 * Tokens de timing/easing de la transicion compartida (validados en
 * my-notes-app): morfosis de superficie card<->detalle con 420 ms totales.
 * Nada de timings inline en composables: todo vive aca.
 */
object Motion {
    /** EaseFlip: curva estandar de entrada (arranque lento, llegada suave). */
    val EaseFlip: Easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

    const val BoundsMillis = 420

    const val ContenidoSalidaMillis = 90
    const val ContenidoEntradaMillis = 180
    const val ContenidoEntradaDelayMillis = 60

    const val SuperficieColorDelayMillis = 50
    const val SuperficieColorMillis = 200

    const val DetalleFadeMillis = 120
    const val DetalleFadeDelayMillis = 40

    const val RecesoEscala = 0.94f
    const val RecesoAlpha = 0.4f

    /** Medio ciclo del pulso del esqueleto de carga (va en RepeatMode.Reverse). */
    const val PulsoEsqueletoMillis = 900

    /** Relleno de la barra de proporcion del hero: lento, es un dato, no un gesto. */
    const val BarraProporcionMillis = 600
}

/*
 * Nota: MaterialTheme.motionScheme (el sistema de motion de M3 Expressive)
 * seria el lugar natural para los timings de componente, pero en material3
 * 1.4.0 —la version que resuelve el BOM de este proyecto— tanto MotionScheme
 * como ExperimentalMaterial3ExpressiveApi son internal. Hasta que entre
 * material3-expressive como dependencia, todos los timings salen de aca.
 */

