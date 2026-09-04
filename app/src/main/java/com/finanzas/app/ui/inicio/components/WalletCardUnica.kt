package com.finanzas.app.ui.inicio.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.finanzas.app.data.local.entity.OrigenMovimiento
import com.finanzas.app.domain.cuenta.SaldoCuenta
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme
import com.finanzas.app.ui.theme.SurfaceCrema
import com.finanzas.app.ui.theme.TextoMontoConCentavos

private val AlturaTarjeta = 140.dp

/**
 * Unica wallet destacada en Inicio (prototipo visual, plan.md#8: a futuro se
 * contempla personalizar cada tarjeta). ElevatedCard M3 con el color del
 * origen (violeta = automatico, mostaza = manual): sin numero enmascarado
 * porque la app no tiene acceso a ese dato real, solo nombre y saldo.
 */
@Composable
fun WalletCardUnica(
    saldoCuenta: SaldoCuenta,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val colorFondo = if (saldoCuenta.cuenta.origen == OrigenMovimiento.MANUAL) {
        FinanzasTheme.colores.origenManual
    } else {
        FinanzasTheme.colores.origenAutomatico
    }

    // Onda cacheada por tamaño: evita new Path() por cada frame de scroll.
    val ondaColor = remember { Color.White.copy(alpha = 0.10f) }
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .height(AlturaTarjeta)
            .semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = colorFondo),
        onClick = onClick ?: {},
        enabled = onClick != null,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.EspacioS),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountBalanceWallet,
                        contentDescription = null,
                        tint = SurfaceCrema.copy(alpha = 0.9f),
                    )
                    Text(
                        text = saldoCuenta.cuenta.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        color = SurfaceCrema,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
                Column {
                    Text(
                        text = "Saldo",
                        style = MaterialTheme.typography.labelMedium,
                        color = SurfaceCrema.copy(alpha = 0.8f),
                    )
                    TextoMontoConCentavos(
                        centavos = saldoCuenta.saldoCentavos,
                        estiloEntero = FinanzasTheme.monto.mediano.copy(color = SurfaceCrema),
                        estiloCentavos = FinanzasTheme.monto.pequeno.copy(color = SurfaceCrema),
                    )
                }
            }
        }
    }
}
