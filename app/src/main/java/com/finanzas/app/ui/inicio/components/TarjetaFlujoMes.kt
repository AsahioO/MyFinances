package com.finanzas.app.ui.inicio.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.finanzas.app.domain.reportes.FlujoMes
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme
import com.finanzas.app.ui.theme.Motion
import com.finanzas.app.ui.theme.SurfaceLavanda
import com.finanzas.app.ui.theme.TextoMontoConCentavos
import com.finanzas.app.ui.theme.formatearMontoPlano
import java.util.Locale
import kotlin.math.roundToInt

private val AlturaBarra = 10.dp
private val SeparacionBarra = 4.dp
private val LOCALE_ES_MX_HERO = Locale.forLanguageTag("es-MX")

/**
 * Numero protagonista de Inicio (plan.md#8): el flujo del mes, no un "balance
 * real" — es lo unico 100% preciso porque depende solo de lo que la app ya
 * capturo. ElevatedCard M3 sobre el degradado para despegarse del fondo.
 *
 * El mes vive en el label ("Flujo de septiembre") y no en una TopAppBar: en
 * Inicio no hay acciones que justifiquen una barra colapsable y su area
 * expandida dejaba ~120dp vacios sobre el titulo.
 */
@Composable
fun TarjetaFlujoMes(flujoMes: FlujoMes, modifier: Modifier = Modifier, mes: String = "") {
    val ingresos = flujoMes.ingresosCentavos
    val egresos = flujoMes.egresosCentavos
    val neto = flujoMes.netoCentavos

    // Cachear formateos y proporcion: solo recalcular si el flujo cambia (no en cada frame de scroll).
    val ingresosTexto = remember(ingresos) { formatearMontoPlano(ingresos) }
    val egresosTexto = remember(egresos) { formatearMontoPlano(egresos) }
    // Sin movimientos no hay proporcion que mostrar: un 50/50 se leeria como
    // "ingresaste tanto como gastaste", que es una afirmacion, no un vacio.
    val sinMovimiento = ingresos == 0L && egresos == 0L
    val fraccionIngresos = remember(ingresos, egresos) {
        val total = ingresos + egresos
        if (total > 0L) ingresos.toFloat() / total else 1f
    }
    // Del dinero que entro, cuanto quedo. Sin ingresos no hay base sobre la que
    // expresar el neto como porcentaje, y el chip se omite.
    val porcentajeRetenido = remember(neto, ingresos) {
        if (ingresos > 0L) (neto.toFloat() / ingresos * 100).roundToInt() else null
    }
    val descripcionBarra = remember(ingresosTexto, egresosTexto) {
        "Ingresos $ingresosTexto contra egresos $egresosTexto"
    }
    // Sin mes (previews viejas, callers futuros) se conserva el label generico.
    val etiquetaFlujo = remember(mes) {
        if (mes.isBlank()) "Flujo del mes" else "Flujo de ${mes.lowercase(LOCALE_ES_MX_HERO)}"
    }

    val fraccionAnimada by animateFloatAsState(
        targetValue = fraccionIngresos,
        animationSpec = tween(Motion.BarraProporcionMillis, easing = Motion.EaseFlip),
        label = "fraccion_flujo",
    )

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(modifier = Modifier.padding(Dimens.EspacioL)) {
            Text(
                text = etiquetaFlujo,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Dimens.EspacioXS))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextoMontoConCentavos(
                    centavos = neto,
                    estiloEntero = FinanzasTheme.monto.grande,
                    estiloCentavos = FinanzasTheme.monto.mediano,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (porcentajeRetenido != null) {
                    Spacer(modifier = Modifier.size(Dimens.EspacioS))
                    ChipTendencia(positivo = neto >= 0, porcentaje = porcentajeRetenido)
                }
            }

            Spacer(modifier = Modifier.height(Dimens.EspacioM))
            BarraProporcionFlujo(
                fraccionIngresos = fraccionAnimada,
                vacia = sinMovimiento,
                modifier = Modifier.semantics { contentDescription = descripcionBarra },
            )

            Spacer(modifier = Modifier.height(Dimens.EspacioM))
            Row(modifier = Modifier.fillMaxWidth()) {
                LeyendaFlujo(
                    etiqueta = "Ingresos",
                    monto = ingresosTexto,
                    color = FinanzasTheme.colores.ingreso,
                    modifier = Modifier.weight(1f),
                )
                LeyendaFlujo(
                    etiqueta = "Egresos",
                    monto = egresosTexto,
                    color = FinanzasTheme.colores.egreso,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Dos segmentos proporcionales en vez de una barra de progreso: aqui no hay un
 * "avance hacia una meta", sino dos cantidades que se reparten el ancho, que
 * es justo lo que el mes significa. Se dibuja con weights (no Canvas) para no
 * pagar una fase de draw propia en cada frame del scroll.
 */
@Composable
private fun BarraProporcionFlujo(
    fraccionIngresos: Float,
    vacia: Boolean,
    modifier: Modifier = Modifier,
) {
    if (vacia) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(AlturaBarra)
                .clip(RoundedCornerShape(percent = 50))
                .background(SurfaceLavanda),
        )
        return
    }
    val pesoIngresos = fraccionIngresos.coerceIn(0.02f, 0.98f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(AlturaBarra),
        horizontalArrangement = Arrangement.spacedBy(SeparacionBarra),
    ) {
        Box(
            modifier = Modifier
                .weight(pesoIngresos)
                .fillMaxHeight()
                .clip(RoundedCornerShape(percent = 50))
                .background(FinanzasTheme.colores.ingreso),
        )
        Box(
            modifier = Modifier
                .weight(1f - pesoIngresos)
                .fillMaxHeight()
                .clip(RoundedCornerShape(percent = 50))
                .background(FinanzasTheme.colores.egreso),
        )
    }
}

/** Punto de color + etiqueta + monto: le pone nombre a cada mitad de la barra. */
@Composable
private fun LeyendaFlujo(
    etiqueta: String,
    monto: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.EspacioXXS),
        ) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = monto,
            style = FinanzasTheme.monto.pequeno,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Tendencia del neto del mes como AssistChip M3: borde e icono coloreados
 * segun el signo (menta/coral), sin relleno solido para no pelear con el
 * neto protagonista.
 */
@Composable
private fun ChipTendencia(positivo: Boolean, porcentaje: Int, modifier: Modifier = Modifier) {
    val color = if (positivo) FinanzasTheme.colores.ingreso else FinanzasTheme.colores.egreso
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(text = "${if (positivo) "+" else ""}$porcentaje%") },
        leadingIcon = {
            Icon(
                imageVector = if (positivo) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = null,
            )
        },
        modifier = modifier.semantics {
            contentDescription = if (positivo) {
                "Retuviste $porcentaje por ciento de tus ingresos"
            } else {
                "Gastaste ${-porcentaje} por ciento mas de lo que ingresaste"
            }
        },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = Color.Transparent,
            disabledLabelColor = color,
            disabledLeadingIconContentColor = color,
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = false,
            borderColor = color.copy(alpha = 0.4f),
        ),
    )
}
