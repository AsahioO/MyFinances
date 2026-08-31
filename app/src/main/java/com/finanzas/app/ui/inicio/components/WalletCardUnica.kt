package com.finanzas.app.ui.inicio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.finanzas.app.data.local.entity.OrigenMovimiento
import com.finanzas.app.domain.cuenta.SaldoCuenta
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme
import com.finanzas.app.ui.theme.SurfaceCrema
import com.finanzas.app.ui.theme.TextoMontoConCentavos

private val AlturaTarjeta = 184.dp

/**
 * Unica wallet destacada en Inicio (prototipo visual, plan.md#8: a futuro se
 * contempla personalizar cada tarjeta). A diferencia de la referencia visual,
 * sin numero de tarjeta enmascarado: la app no tiene acceso a ese dato real
 * (no mueve dinero, solo lo detecta), asi que inventar uno seria enganoso —
 * se usa el nombre real de la cuenta en su lugar.
 */
@Composable
fun WalletCardUnica(saldoCuenta: SaldoCuenta, modifier: Modifier = Modifier) {
    val colorFondo = if (saldoCuenta.cuenta.origen == OrigenMovimiento.MANUAL) {
        FinanzasTheme.colores.origenManual
    } else {
        FinanzasTheme.colores.origenAutomatico
    }

    // Onda cacheada por tamaño: evita new Path() por cada frame de scroll.
    val ondaColor = remember { Color.White.copy(alpha = 0.10f) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AlturaTarjeta)
            .clip(RoundedCornerShape(Dimens.RadioTarjeta))
            .background(colorFondo)
            .drawWithCache {
                val onda = Path().apply {
                    moveTo(0f, size.height * 0.30f)
                    cubicTo(
                        size.width * 0.28f, size.height * 0.05f,
                        size.width * 0.60f, size.height * 0.46f,
                        size.width, size.height * 0.16f,
                    )
                    lineTo(size.width, 0f)
                    lineTo(0f, 0f)
                    close()
                }
                onDrawBehind { drawPath(path = onda, color = ondaColor) }
            },
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.EspacioL),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = saldoCuenta.cuenta.nombre,
                    style = FinanzasTheme.monto.mediano,
                    color = SurfaceCrema,
                )
                Icon(
                    imageVector = Icons.Filled.Wifi,
                    contentDescription = null,
                    tint = SurfaceCrema.copy(alpha = 0.8f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tu balance",
                        style = FinanzasTheme.monto.pequeno,
                        color = SurfaceCrema.copy(alpha = 0.8f),
                    )
                    TextoMontoConCentavos(
                        centavos = saldoCuenta.saldoCentavos,
                        estiloEntero = FinanzasTheme.monto.mediano.copy(color = SurfaceCrema),
                        estiloCentavos = FinanzasTheme.monto.pequeno.copy(color = SurfaceCrema),
                    )
                }
                // Switch decorativo (antes Switch material deshabilitado = nodos de animacion extra en scroll).
                // Replica visual estática sin estado/animacion -> 0 overhead de recomposicion.
                Box(
                    modifier = Modifier
                        .size(width = 46.dp, height = 28.dp)
                        .background(SurfaceCrema.copy(alpha = 0.35f), RoundedCornerShape(percent = 50))
                        .padding(2.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(SurfaceCrema, CircleShape),
                    )
                }
            }
        }
    }
}
