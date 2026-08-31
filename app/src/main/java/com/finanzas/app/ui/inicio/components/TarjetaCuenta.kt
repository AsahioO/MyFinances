package com.finanzas.app.ui.inicio.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finanzas.app.data.local.entity.OrigenMovimiento
import com.finanzas.app.domain.cuenta.SaldoCuenta
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme
import com.finanzas.app.ui.theme.TextoMontoConCentavos

@Composable
fun TarjetaCuenta(
    saldoCuenta: SaldoCuenta,
    modifier: Modifier = Modifier,
) {
    val colorAcento = if (saldoCuenta.cuenta.origen == OrigenMovimiento.MANUAL) {
        FinanzasTheme.colores.origenManual
    } else {
        FinanzasTheme.colores.origenAutomatico
    }

    Card(
        modifier = modifier.width(180.dp),
        shape = RoundedCornerShape(Dimens.RadioTarjeta),
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(Dimens.EspacioM)) {
            Row {
                IndicadorOrigen(color = colorAcento)
                Spacer(modifier = Modifier.width(Dimens.EspacioXS))
                Text(
                    text = saldoCuenta.cuenta.nombre,
                    style = FinanzasTheme.monto.pequeno.copy(fontWeight = FontWeight.Medium),
                )
            }
            Spacer(modifier = Modifier.height(Dimens.EspacioS))
            TextoMontoConCentavos(
                centavos = saldoCuenta.saldoCentavos,
                estiloEntero = FinanzasTheme.monto.mediano,
                estiloCentavos = FinanzasTheme.monto.pequeno,
            )
        }
    }
}

@Composable
private fun IndicadorOrigen(color: Color) {
    Icon(
        imageVector = Icons.Filled.AccountBalanceWallet,
        contentDescription = null,
        tint = color,
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape),
    )
}
