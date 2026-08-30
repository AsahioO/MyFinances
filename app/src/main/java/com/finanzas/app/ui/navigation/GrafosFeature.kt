package com.finanzas.app.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.finanzas.app.ui.ajustes.AjustesScreen
import com.finanzas.app.ui.inicio.InicioScreen
import com.finanzas.app.ui.movimientos.MovimientosScreen
import com.finanzas.app.ui.reportes.ReportesScreen

/**
 * Un grafo anidado por feature. Las pantallas secundarias de cada seccion
 * (detalle de movimiento, alta manual, gestion de categorias...) se agregan
 * dentro del grafo que les corresponde, sin tocar los otros.
 */

fun NavGraphBuilder.grafoInicio() {
    navigation<GrafoInicio>(startDestination = RutaInicio) {
        composable<RutaInicio> { InicioScreen() }
    }
}

fun NavGraphBuilder.grafoMovimientos() {
    navigation<GrafoMovimientos>(startDestination = RutaMovimientos) {
        composable<RutaMovimientos> { MovimientosScreen() }
    }
}

fun NavGraphBuilder.grafoReportes() {
    navigation<GrafoReportes>(startDestination = RutaReportes) {
        composable<RutaReportes> { ReportesScreen() }
    }
}

fun NavGraphBuilder.grafoAjustes() {
    navigation<GrafoAjustes>(startDestination = RutaAjustes) {
        composable<RutaAjustes> { AjustesScreen() }
    }
}
