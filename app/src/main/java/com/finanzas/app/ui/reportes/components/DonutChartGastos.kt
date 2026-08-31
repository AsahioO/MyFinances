package com.finanzas.app.ui.reportes.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme
import com.finanzas.app.ui.theme.TextoMontoConCentavos
import com.finanzas.app.ui.theme.formatearMontoPlano
import kotlin.math.roundToInt

/** Un segmento ya resuelto: color y proporcion (0f..1f) calculados por quien llama. */
data class SegmentoDonut(
    val color: Color,
    val proporcion: Float,
    val etiqueta: String,
)

private const val ANGULO_INICIAL = -90f
private const val ANGULO_TOTAL = 360f
private const val ESPACIO_ENTRE_SEGMENTOS_GRADOS = 3f

/** Diametro del anillo; el box lo centra en el ancho de la pantalla. */
private val DIAMETRO_DONUT = 232.dp

/** Grosor del trazo, rango 20-24dp. */
private val GROSOR_DONUT = 22.dp

/**
 * Donut dibujado a mano con Canvas: pocos segmentos estaticos, sin
 * interaccion. No justifica una libreria de charts externa. El anillo es de
 * tamaño fijo (no crece con el ancho de pantalla) y el total va al centro,
 * con la etiqueta del periodo arriba.
 */
@Composable
fun DonutChartGastos(
    segmentos: List<SegmentoDonut>,
    totalCentavos: Long,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .size(DIAMETRO_DONUT)
                .semantics {
                    contentDescription = descripcionAccesible(segmentos, totalCentavos)
                },
        ) {
            val grosor = GROSOR_DONUT.toPx()
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.EspacioXXS),
        ) {
            Text(
                text = "Este mes",
                style = FinanzasTheme.monto.pequeno,
                color = FinanzasTheme.colores.textoSecundario,
            )
            TextoMontoConCentavos(
                centavos = totalCentavos,
                estiloEntero = FinanzasTheme.monto.grande,
                estiloCentavos = FinanzasTheme.monto.mediano,
            )
        }
    }
}

/**
 * El donut no se puede leer solo por TalkBack: la descripcion repite el total
 * y el desglose por categoria con su porcentaje.
 */
private fun descripcionAccesible(segmentos: List<SegmentoDonut>, totalCentavos: Long): String {
    val desglose = segmentos.joinToString { segmento ->
        "${segmento.etiqueta} ${(segmento.proporcion * 100).roundToInt()}%"
    }
    return "Gasto de este mes: ${formatearMontoPlano(totalCentavos)}. Por categoria: $desglose"
}
