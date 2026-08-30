package com.finanzas.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

/**
 * Grafo raiz: solo conecta los grafos de cada feature, no declara pantallas.
 */
@Composable
fun FinanzasNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = GrafoInicio,
        modifier = modifier,
    ) {
        grafoInicio()
        grafoMovimientos()
        grafoReportes()
        grafoAjustes()
    }
}
