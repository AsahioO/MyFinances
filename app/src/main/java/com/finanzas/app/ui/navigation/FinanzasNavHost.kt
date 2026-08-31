package com.finanzas.app.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

/**
 * Grafo raiz: solo conecta los grafos de cada feature, no declara pantallas.
 * SharedTransitionLayout envuelve todo el NavHost (plan.md#8): es el unico
 * punto donde puede vivir, ya que la transicion Inicio -> Detalle cruza dos
 * grafos anidados distintos (GrafoInicio y GrafoAcciones).
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FinanzasNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    SharedTransitionLayout(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = GrafoInicio,
            modifier = Modifier.fillMaxSize(),
        ) {
            grafoInicio(navController, sharedTransitionScope = this@SharedTransitionLayout)
            grafoMovimientos(navController)
            grafoReportes(navController)
            grafoAjustes()
            grafoAcciones(navController, sharedTransitionScope = this@SharedTransitionLayout)
        }
    }
}
