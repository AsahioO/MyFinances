package com.finanzas.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.finanzas.app.data.local.entity.TipoMovimiento
import com.finanzas.app.ui.common.MovimientoUi
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme
import com.finanzas.app.ui.theme.TextoMontoConCentavos

@Composable
fun FilaMovimiento(
    movimiento: MovimientoUi,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null, onClick = { onClick?.invoke() })
            .padding(vertical = Dimens.EspacioXS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(movimiento.colorOrigen.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Receipt,
                contentDescription = null,
                tint = movimiento.colorOrigen,
                modifier = Modifier.size(20.dp),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Dimens.EspacioS),
        ) {
            Text(text = movimiento.comercioOrigen, style = FinanzasTheme.monto.pequeno)
            Text(
                text = movimiento.categoriaNombre ?: "Sin categoria",
                style = FinanzasTheme.monto.pequeno,
                color = FinanzasTheme.colores.textoSecundario,
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.EspacioXXS)) {
            if (movimiento.pendienteRevision) {
                Icon(
                    imageVector = Icons.Filled.Circle,
                    contentDescription = "Pendiente de revision",
                    tint = FinanzasTheme.colores.pendienteRevision,
                    modifier = Modifier.size(8.dp),
                )
            }
            val signo = if (movimiento.tipo == TipoMovimiento.EGRESO) -1L else 1L
            val color = if (movimiento.tipo == TipoMovimiento.EGRESO) {
                FinanzasTheme.colores.egreso
            } else {
                FinanzasTheme.colores.ingreso
            }
            TextoMontoConCentavos(
                centavos = signo * movimiento.montoCentavos,
                estiloEntero = FinanzasTheme.monto.pequeno.copy(color = color),
                estiloCentavos = FinanzasTheme.monto.pequeno.copy(color = color),
            )
        }
    }
}
