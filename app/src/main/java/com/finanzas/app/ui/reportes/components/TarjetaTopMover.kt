package com.finanzas.app.ui.reportes.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.finanzas.app.ui.common.fondoCategoria
import com.finanzas.app.ui.components.IconoCategoriaCircular
import com.finanzas.app.ui.components.PillPorcentaje
import com.finanzas.app.ui.components.SparklineCategoria
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme
import com.finanzas.app.ui.theme.TextoMontoConCentavos
import kotlin.math.roundToInt

/** Cuenta minima de transacciones para que el sparkline tenga sentido (una tendencia real, no 1-2 puntos). */
private const val MINIMO_TRANSACCIONES_SPARKLINE = 3

/**
 * Intensidad de la mezcla hacia blanco del fondo, por posicion en el ranking
 * (topMovers ya llega ordenado de mayor a menor gasto): la primera tarjeta
 * lleva el color mas cargado para reforzar que es la de mayor peso.
 */
private val INTENSIDADES_FONDO_POR_RANGO = listOf(0.22f, 0.15f, 0.12f, 0.10f)
private const val INTENSIDAD_FONDO_DEFECTO = 0.10f

private fun intensidadFondoPorRango(indice: Int): Float =
    INTENSIDADES_FONDO_POR_RANGO.getOrElse(indice) { INTENSIDAD_FONDO_DEFECTO }

@Composable
fun TarjetaTopMover(
    etiqueta: String,
    montoCentavos: Long,
    proporcion: Float,
    color: Color,
    icono: ImageVector,
    montosOrdenados: List<Long>,
    indice: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .width(140.dp)
            // Altura compacta pero con margen para el sparkline condicional.
            .heightIn(min = 132.dp, max = 172.dp),
        shape = RoundedCornerShape(Dimens.RadioTarjeta),
        colors = CardDefaults.cardColors(containerColor = fondoCategoria(color, intensidadFondoPorRango(indice))),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(Dimens.EspacioM)) {
            IconoCategoriaCircular(
                icono = icono,
                colorFondo = Color.White.copy(alpha = 0.6f),
                colorIcono = color,
                tamano = 26.dp,
                modifier = Modifier.align(Alignment.TopEnd),
            )
            Column(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
            ) {
                Text(text = etiqueta, style = FinanzasTheme.monto.pequeno, maxLines = 1)
                Spacer(modifier = Modifier.height(Dimens.EspacioXXS))
                TextoMontoConCentavos(
                    centavos = montoCentavos,
                    estiloEntero = FinanzasTheme.monto.mediano,
                    estiloCentavos = FinanzasTheme.monto.pequeno,
                )
                if (montosOrdenados.size >= MINIMO_TRANSACCIONES_SPARKLINE) {
                    Spacer(modifier = Modifier.height(Dimens.EspacioXS))
                    SparklineCategoria(
                        valores = montosOrdenados,
                        color = color,
                        modifier = Modifier.fillMaxWidth().height(20.dp),
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.EspacioXS))
                PillPorcentaje(porcentaje = (proporcion * 100).roundToInt(), colorFondo = color)
            }
        }
    }
}
