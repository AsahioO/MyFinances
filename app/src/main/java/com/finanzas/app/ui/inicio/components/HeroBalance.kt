package com.finanzas.app.ui.inicio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanzas.app.domain.reportes.FlujoMes
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme
import com.finanzas.app.ui.theme.TextoMontoConCentavos
import com.finanzas.app.ui.theme.formatearMontoPlano
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Numero protagonista de Inicio (plan.md#8): el flujo del mes, no un
 * "balance real" — es lo unico 100% preciso porque depende solo de lo que la
 * app ya capturo.
 */
@Composable
fun HeroBalance(flujoMes: FlujoMes, modifier: Modifier = Modifier) {
    // Cachear formateos y proporcion: solo recalcular si flujo cambia (no en cada recomposicion por scroll).
    val subtitulo = remember(flujoMes.ingresosCentavos, flujoMes.egresosCentavos) {
        "${formatearMontoPlano(flujoMes.ingresosCentavos)} · ${formatearMontoPlano(flujoMes.egresosCentavos)}"
    }
    val proporcion = remember(flujoMes.netoCentavos, flujoMes.egresosCentavos) {
        if (flujoMes.egresosCentavos > 0) {
            (abs(flujoMes.netoCentavos).toFloat() / flujoMes.egresosCentavos * 100).roundToInt()
        } else null
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Tu balance disponible",
            style = FinanzasTheme.monto.pequeno,
            color = FinanzasTheme.colores.textoSecundario,
        )
        TextoMontoConCentavos(
            centavos = flujoMes.netoCentavos,
            estiloEntero = FinanzasTheme.monto.grande,
            estiloCentavos = FinanzasTheme.monto.mediano,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.EspacioS),
            modifier = Modifier.padding(top = Dimens.EspacioXXS),
        ) {
            Text(
                text = subtitulo,
                style = FinanzasTheme.monto.pequeno,
                color = FinanzasTheme.colores.textoSecundario,
            )
            // Sin egresos no hay base para expresar el neto como proporcion.
            if (proporcion != null) {
                PillTendencia(positivo = flujoMes.netoCentavos >= 0, porcentaje = proporcion)
            }
        }
    }
}

/**
 * A diferencia de PillPorcentaje (sin semantica de tendencia, color decidido
 * por quien llama), esta si comunica tendencia a proposito: verde/coral +
 * signo, solo para el neto del mes en el hero.
 */
@Composable
private fun PillTendencia(positivo: Boolean, porcentaje: Int, modifier: Modifier = Modifier) {
    val color = if (positivo) FinanzasTheme.colores.ingreso else FinanzasTheme.colores.egreso
    Text(
        text = "${if (positivo) "+" else "-"}$porcentaje%",
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = FinanzasTheme.monto.pequeno.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
        color = Color.White,
    )
}
