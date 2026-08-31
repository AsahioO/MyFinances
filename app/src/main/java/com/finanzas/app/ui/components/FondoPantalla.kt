package com.finanzas.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import com.finanzas.app.ui.theme.SurfaceCrema
import com.finanzas.app.ui.theme.SurfaceLavanda

/**
 * Fondo contenedor raiz de Inicio y Reportes. Las tarjetas (WalletCardUnica,
 * TarjetaTopMover) llevan relleno solido y sombra para despegarse de este
 * fondo, en vez de depender del degradado para separar planos.
 *
 * [conDegradado] activa el lavanda -> crema de plan.md#8 (la referencia
 * visual tipo wallet app). Por defecto queda plano: Reportes/Categorias
 * ya usan tarjetas con su propio color de acento, y competir con un fondo en
 * degradado ahi les resta jerarquia en vez de sumarla.
 */
@Composable
fun FondoPantalla(
    modifier: Modifier = Modifier,
    conDegradado: Boolean = false,
    content: @Composable () -> Unit,
) {
    if (conDegradado) {
        val brush = remember { Brush.verticalGradient(listOf(SurfaceLavanda, SurfaceCrema)) }
        Box(
            modifier = modifier
                .fillMaxSize()
                .drawWithCache {
                    onDrawBehind { drawRect(brush) }
                },
        ) {
            content()
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(SurfaceLavanda),
        ) {
            content()
        }
    }
}
