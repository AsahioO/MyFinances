package com.finanzas.app.ui.components

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.OverlayClip
import androidx.compose.animation.SharedTransitionScope.SharedContentState
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.Motion

/**
 * Par sharedTransitionScope+animatedContentScope que viaja junto (nunca uno
 * sin el otro) desde FinanzasNavHost hasta FilaMovimiento/CabeceraMovimiento.
 * Null fuera de un SharedTransitionLayout (previews, o listas que hoy no
 * participan de la transicion como Reportes/Movimientos).
 */
data class ContextoTransicion(
    val sharedTransitionScope: SharedTransitionScope,
    val animatedContentScope: AnimatedContentScope,
)

/** Icono de categoria: spring en vez de tween para sentirse "vivo" al viajar entre pantallas. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.compartirIcono(clave: String, contexto: ContextoTransicion?): Modifier {
    contexto ?: return this
    return with(contexto.sharedTransitionScope) {
        this@compartirIcono.sharedElement(
            sharedContentState = rememberSharedContentState(key = clave),
            animatedVisibilityScope = contexto.animatedContentScope,
            boundsTransform = { _, _ -> spring(dampingRatio = 0.8f, stiffness = 400f) },
        )
    }
}

/** El tween(350) original: se preserva para toda llamada que no pase un boundsTransform explicito. */
private val BoundsTransformLimitePorDefecto: BoundsTransform =
    BoundsTransform { _, _ -> tween(350, easing = FastOutSlowInEasing) }

/**
 * Texto/monto/contenedor que puede cambiar de tamano entre fila y detalle
 * (ej. monto pequeno -> grande): sharedBounds escala el contenido en vez de
 * solo interpolar posicion, que es lo que necesita sharedElement.
 *
 * [boundsTransform] y [clip] son opcionales: sin pasarlos, el comportamiento
 * es identico al de antes (tween lineal, sin clip propio -> ParentClip).
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.compartirLimite(
    clave: String,
    contexto: ContextoTransicion?,
    boundsTransform: BoundsTransform = BoundsTransformLimitePorDefecto,
    clip: OverlayClip? = null,
): Modifier {
    contexto ?: return this
    return with(contexto.sharedTransitionScope) {
        if (clip != null) {
            this@compartirLimite.sharedBounds(
                sharedContentState = rememberSharedContentState(key = clave),
                animatedVisibilityScope = contexto.animatedContentScope,
                boundsTransform = boundsTransform,
                clipInOverlayDuringTransition = clip,
                // resizeMode por defecto ya es scaleToBounds(FillWidth, Center):
                // exactamente lo que queremos para el monto (pequeno -> grande).
            )
        } else {
            this@compartirLimite.sharedBounds(
                sharedContentState = rememberSharedContentState(key = clave),
                animatedVisibilityScope = contexto.animatedContentScope,
                boundsTransform = boundsTransform,
                // resizeMode por defecto ya es scaleToBounds(FillWidth, Center):
                // exactamente lo que queremos para el monto (pequeno -> grande).
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// "agregar-bounds": morfosis de forma (FAB circular -> tarjeta)
// ─────────────────────────────────────────────────────────────────────────────

/** Lado del FAB de Inicio (FloatingActionButton circular 56dp). Debe coincidir con el FAB. */
private const val OrigenLadoDp = 56f
private const val CrecimientoMorfosisDp = 96f // ventana de crecimiento (lado menor) hasta comprometerse al radio final

/**
 * Interpola el clip de circulo (radio = mitad del lado) a RoundedCornerShape(Dimens.RadioTarjeta)
 * a medida que crecen los bounds animados. El coerceIn final (no solo en el radio deseado, sino
 * tambien acotado a la mitad del lado menor de los bounds de ESTE frame) es lo que evita un ovalo
 * aplastado si width y height del Rect no crecen exactamente en lockstep durante el spring.
 */
private fun rutaMorfosisCirculoATarjeta(bounds: Rect, density: Density): Path {
    val origenPx = with(density) { OrigenLadoDp.dp.toPx() }
    val crecimientoPx = with(density) { CrecimientoMorfosisDp.dp.toPx() }
    val radioTarjetaPx = with(density) { Dimens.RadioTarjeta.toPx() }

    val menorLadoPx = minOf(bounds.width, bounds.height)
    val fraccion = ((menorLadoPx - origenPx) / crecimientoPx).coerceIn(0f, 1f)
    val radioDeseadoPx = origenPx / 2f + (radioTarjetaPx - origenPx / 2f) * fraccion
    val radioFinalPx = radioDeseadoPx.coerceIn(0f, menorLadoPx / 2f)

    return Path().apply {
        addRoundRect(RoundRect(rect = bounds, cornerRadius = CornerRadius(radioFinalPx)))
    }
}

/** Clip compartido por los participantes de "agregar-bounds" (FAB de Inicio, CardHeroMonto). */
val ClipAgregarBounds: OverlayClip = object : OverlayClip {
    override fun getClipPath(
        sharedContentState: SharedContentState,
        bounds: Rect,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Path = rutaMorfosisCirculoATarjeta(bounds, density)
}

/**
 * Tween + EaseFlip (validado en produccion): el spring es para transforms
 * direccionales (picker que se abre y se cierra), no para entradas estandar.
 */
val BoundsTransformAgregarBounds: BoundsTransform =
    BoundsTransform { _, _ -> tween(Motion.BoundsMillis, easing = Motion.EaseFlip) }

/**
 * Color de la superficie morfeada, animado por la Transition del
 * AnimatedVisibilityScope: en reposo (Visible) el color propio; durante la
 * transicion el del lado contrario. Asi el color "viaja" con los bounds en vez
 * de cortarse: el lado saliente conserva el suyo el primer cuarto y luego
 * morfea al del peer (delay 50 + morph 200).
 */
@Composable
fun colorSuperficie(
    contexto: ContextoTransicion?,
    colorReposo: Color,
    colorContraparte: Color,
): Color {
    contexto ?: return colorReposo
    return with(contexto.animatedContentScope) {
        transition.animateColor(
            transitionSpec = {
                tween(
                    delayMillis = Motion.SuperficieColorDelayMillis,
                    durationMillis = Motion.SuperficieColorMillis,
                    easing = Motion.EaseFlip,
                )
            },
            label = "color_superficie",
        ) { estado -> if (estado == EnterExitState.Visible) colorReposo else colorContraparte }.value
    }
}

/**
 * Crossfade del contenido dentro de la superficie morfeada: entra con delay
 * (el contenido nuevo aparece cuando la superficie ya esta llegando, sin
 * quedar comprimido en los bounds pequenos del arranque) y sale rapido.
 * Fast path inactivo: null fuera de una transicion -> sin animacion.
 */
@Composable
fun Modifier.compartirContenido(contexto: ContextoTransicion?): Modifier {
    contexto ?: return this
    return with(contexto.animatedContentScope) {
        this@compartirContenido.animateEnterExit(
            enter = fadeIn(
                tween(Motion.ContenidoEntradaMillis, delayMillis = Motion.ContenidoEntradaDelayMillis),
            ),
            exit = fadeOut(tween(Motion.ContenidoSalidaMillis)),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Scrub de retroceso predictivo en vivo (AgregarMovimientoScreen + DetalleMovimientoScreen)
// ─────────────────────────────────────────────────────────────────────────────

/** Curva fast-out/extra-slow-in para no mapear 1:1 el progreso lineal del gesto. */
val EasingRetrocesoPredictivo: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
val EncogimientoMaximoRetroceso = 0.06f // escala minima ~0.94 al progreso maximo del gesto
val DesvanecimientoMaximoRetroceso = 0.25f // alpha minimo ~0.75 al progreso maximo del gesto
