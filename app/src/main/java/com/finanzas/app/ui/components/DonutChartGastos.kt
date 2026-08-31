package com.finanzas.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme
import com.finanzas.app.ui.theme.TextoMontoConCentavos

/** Un segmento ya resuelto: color y proporcion (0f..1f) calculados por quien llama. */
data class SegmentoDonut(
    val color: Color,
    val proporcion: Float,
    val etiqueta: String,
)

private const val ANGULO_INICIAL = -90f
private const val ANGULO_TOTAL = 360f
private const val ESPACIO_ENTRE_SEGMENTOS_GRADOS = 3f

/**
 * Donut dibujado a mano con Canvas: pocos segmentos estaticos, sin
 * interaccion. No justifica una libreria de charts externa.
 */
@Composable
fun DonutChartGastos(
    segmentos: List<SegmentoDonut>,
    totalCentavos: Long,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(Dimens.EspacioM),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            val grosor = size.minDimension * 0.16f
            val diametro = size.minDimension - grosor
            val esquinaSuperiorIzq = Offset(
                x = (size.width - diametro) / 2f,
                y = (size.height - diametro) / 2f,
            )
            var anguloActual = ANGULO_INICIAL
            segmentos.forEach { segmento ->
                val barrido = segmento.proporcion * ANGULO_TOTAL
                val barridoConHueco = (barrido - ESPACIO_ENTRE_SEGMENTOS_GRADOS).coerceAtLeast(1f)
                drawArc(
                    color = segmento.color,
                    startAngle = anguloActual,
                    sweepAngle = barridoConHueco,
                    useCenter = false,
                    topLeft = esquinaSuperiorIzq,
                    size = Size(diametro, diametro),
                    style = Stroke(width = grosor, cap = StrokeCap.Round),
                )
                anguloActual += barrido
            }
        }
        TextoMontoConCentavos(
            centavos = totalCentavos,
            estiloEntero = FinanzasTheme.monto.grande,
            estiloCentavos = FinanzasTheme.monto.mediano,
        )
    }
}
