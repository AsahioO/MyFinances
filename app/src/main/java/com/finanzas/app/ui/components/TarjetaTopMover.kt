package com.finanzas.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme
import com.finanzas.app.ui.theme.TextoMontoConCentavos
import kotlin.math.roundToInt

@Composable
fun TarjetaTopMover(
    etiqueta: String,
    montoCentavos: Long,
    proporcion: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.width(140.dp),
        shape = RoundedCornerShape(Dimens.RadioTarjeta),
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(Dimens.EspacioM)) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, CircleShape),
            )
            Spacer(modifier = Modifier.height(Dimens.EspacioXS))
            Text(text = etiqueta, style = FinanzasTheme.monto.pequeno)
            Spacer(modifier = Modifier.height(Dimens.EspacioXXS))
            TextoMontoConCentavos(
                centavos = montoCentavos,
                estiloEntero = FinanzasTheme.monto.mediano,
                estiloCentavos = FinanzasTheme.monto.pequeno,
            )
            Text(
                text = "${(proporcion * 100).roundToInt()}%",
                style = FinanzasTheme.monto.pequeno,
                color = FinanzasTheme.colores.textoSecundario,
            )
        }
    }
}
