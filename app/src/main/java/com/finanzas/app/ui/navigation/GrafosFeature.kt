package com.finanzas.app.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.finanzas.app.ui.agregar.AgregarMovimientoScreen
import com.finanzas.app.ui.ajustes.AjustesScreen
import com.finanzas.app.ui.components.ContextoTransicion
import com.finanzas.app.ui.inicio.InicioScreen
import com.finanzas.app.ui.movimientos.DetalleMovimientoScreen
import com.finanzas.app.ui.movimientos.MovimientosScreen
import com.finanzas.app.ui.reportes.CategoriasScreen
import com.finanzas.app.ui.reportes.ReportesScreen
import com.finanzas.app.ui.theme.Motion

/**
 * Un grafo anidado por feature. Las pantallas secundarias de cada seccion
 * (detalle de movimiento, alta manual, gestion de categorias...) se agregan
 * dentro del grafo que les corresponde, sin tocar los otros.
 */

/**
 * Specs de ruta de la transicion compartida: el sharedBounds lleva el morph de
 * la superficie; aqui solo anima el contenido NO compartido (la lista recede
 * mientras la tarjeta viaja, el destino fadea rapido). Timings en Motion.
 */
private object TransicionesRuta {
    val detalleEntrada: EnterTransition = fadeIn(
        tween(Motion.DetalleFadeMillis, delayMillis = Motion.DetalleFadeDelayMillis),
    )
    val detalleSalida: ExitTransition = fadeOut(tween(Motion.DetalleFadeMillis))

    val recesoSalida: ExitTransition =
        fadeOut(tween(Motion.BoundsMillis, easing = Motion.EaseFlip), targetAlpha = Motion.RecesoAlpha) +
            scaleOut(tween(Motion.BoundsMillis, easing = Motion.EaseFlip), targetScale = Motion.RecesoEscala)

    val recesoEntrada: EnterTransition =
        fadeIn(tween(Motion.DetalleFadeMillis, delayMillis = Motion.DetalleFadeDelayMillis)) +
            scaleIn(tween(Motion.BoundsMillis, easing = Motion.EaseFlip), initialScale = Motion.RecesoEscala)
}

/** Destinos con elemento compartido al salir/entrar de Inicio. */
private fun NavDestination.esDestinoCompartido(): Boolean =
    hasRoute<RutaAgregarManual>() || hasRoute<RutaDetalleMovimiento>()

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.grafoInicio(
    navController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
) {
    navigation<GrafoInicio>(startDestination = RutaInicio) {
        composable<RutaInicio>(
            exitTransition = {
                if (targetState.destination.esDestinoCompartido()) TransicionesRuta.recesoSalida else fadeOut()
            },
            popEnterTransition = {
                if (initialState.destination.esDestinoCompartido()) TransicionesRuta.recesoEntrada else fadeIn()
            },
        ) {
            InicioScreen(
                onAgregar = { navController.navigate(RutaAgregarManual()) },
                onFilaClick = { id -> navController.navigate(RutaDetalleMovimiento(id)) },
                // "Pendientes" y "Ver todas" mandan al historial completo sin
                // filtro por ahora: filtrar por pendientes queda para cuando
                // MovimientosViewModel exponga el estado inicial via nav arg.
                onVerMovimientos = {
                    navController.navigate(GrafoMovimientos) { launchSingleTop = true }
                },
                onGestionarCuentas = {
                    navController.navigate(GrafoAjustes) { launchSingleTop = true }
                },
                contextoTransicion = ContextoTransicion(sharedTransitionScope, this),
            )
        }
    }
}

fun NavGraphBuilder.grafoMovimientos(navController: NavHostController) {
    navigation<GrafoMovimientos>(startDestination = RutaMovimientos) {
        composable<RutaMovimientos> {
            MovimientosScreen(
                onFilaClick = { id -> navController.navigate(RutaDetalleMovimiento(id)) },
            )
        }
    }
}

fun NavGraphBuilder.grafoReportes(navController: NavHostController) {
    navigation<GrafoReportes>(startDestination = RutaReportes) {
        composable<RutaReportes> {
            ReportesScreen(
                // "Ver todas" salta al historial completo (tab Movimientos).
                onVerTodas = {
                    navController.navigate(GrafoMovimientos) { launchSingleTop = true }
                },
                onVerTodasCategorias = { navController.navigate(RutaCategorias) },
            )
        }
        composable<RutaCategorias> {
            CategoriasScreen(onCerrar = { navController.popBackStack() })
        }
    }
}

fun NavGraphBuilder.grafoAjustes() {
    navigation<GrafoAjustes>(startDestination = RutaAjustes) {
        composable<RutaAjustes> { AjustesScreen() }
    }
}

/**
 * Pantallas de accion, alcanzables desde mas de una seccion (ver comentario
 * en Rutas.kt). Sin tab propio: MainActivity oculta el bottom nav mientras el
 * back stack esta dentro de este grafo.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.grafoAcciones(
    navController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
) {
    navigation<GrafoAcciones>(startDestination = RutaAgregarManual()) {
        composable<RutaAgregarManual>(
            enterTransition = { TransicionesRuta.detalleEntrada },
            exitTransition = { TransicionesRuta.detalleSalida },
        ) {
            AgregarMovimientoScreen(
                onGuardado = { navController.popBackStack() },
                onCancelar = { navController.popBackStack() },
                contextoTransicion = ContextoTransicion(sharedTransitionScope, this),
            )
        }
        composable<RutaDetalleMovimiento>(
            enterTransition = { TransicionesRuta.detalleEntrada },
            exitTransition = { TransicionesRuta.detalleSalida },
        ) {
            DetalleMovimientoScreen(
                onCerrar = { navController.popBackStack() },
                contextoTransicion = ContextoTransicion(sharedTransitionScope, this),
            )
        }
    }
}
