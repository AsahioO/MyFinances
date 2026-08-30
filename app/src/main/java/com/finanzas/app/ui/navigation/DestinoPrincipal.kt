package com.finanzas.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.ui.graphics.vector.ImageVector
import com.finanzas.app.R

/**
 * Las cuatro secciones del bottom navigation. Cada una apunta al grafo de su
 * feature, no a una pantalla suelta: asi la pila interna de cada seccion se
 * conserva al cambiar de pestana.
 */
enum class DestinoPrincipal(
    @param:StringRes val etiqueta: Int,
    val iconoActivo: ImageVector,
    val iconoInactivo: ImageVector,
    val grafo: Any,
) {
    INICIO(
        etiqueta = R.string.nav_inicio,
        iconoActivo = Icons.Filled.Home,
        iconoInactivo = Icons.Outlined.Home,
        grafo = GrafoInicio,
    ),
    MOVIMIENTOS(
        etiqueta = R.string.nav_movimientos,
        iconoActivo = Icons.Filled.SwapVert,
        iconoInactivo = Icons.Outlined.SwapVert,
        grafo = GrafoMovimientos,
    ),
    REPORTES(
        etiqueta = R.string.nav_reportes,
        iconoActivo = Icons.Filled.PieChart,
        iconoInactivo = Icons.Outlined.PieChart,
        grafo = GrafoReportes,
    ),
    AJUSTES(
        etiqueta = R.string.nav_ajustes,
        iconoActivo = Icons.Filled.Settings,
        iconoInactivo = Icons.Outlined.Settings,
        grafo = GrafoAjustes,
    ),
}
