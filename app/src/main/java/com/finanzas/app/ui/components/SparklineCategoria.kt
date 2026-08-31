package com.finanzas.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

private val GROSOR_TRAZO = 2.dp

/**
 * Mini linea de tendencia sin ejes ni labels. Quien llama decide si tiene
 * sentido mostrarla (p. ej. solo con 3+ transacciones); con menos de 2 puntos
 * no hay tendencia que trazar, asi que no dibuja nada.
 */
@Composable
fun SparklineCategoria(
    valores: List<Long>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (valores.size < 2) return
    Canvas(modifier = modifier) {
        val minimo = valores.min().toFloat()
        val maximo = valores.max().toFloat()
        val rango = (maximo - minimo).coerceAtLeast(1f)
        val pasoX = size.width / (valores.size - 1)
        val trazo = Path().apply {
            valores.forEachIndexed { indice, valor ->
                val x = indice * pasoX
                val y = size.height - ((valor - minimo) / rango) * size.height
                if (indice == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(
            path = trazo,
            color = color,
            style = Stroke(width = GROSOR_TRAZO.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}
