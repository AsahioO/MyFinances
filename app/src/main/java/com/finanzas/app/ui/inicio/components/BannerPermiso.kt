package com.finanzas.app.ui.inicio.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.finanzas.app.ui.common.fondoCategoria
import com.finanzas.app.ui.components.IconoCategoriaCircular
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme

/**
 * Banner no bloqueante de plan.md#5: si el acceso a notificaciones no esta
 * concedido, la app funciona igual (todo manual) pero lo avisa aqui.
 * OutlinedCard M3 con acento coral: violeta y mostaza significan origen del
 * dinero (plan.md#8), y esto no es dinero — es una capacidad caida.
 *
 * El descarte es solo de sesion: el banner debe reaparecer mientras el permiso
 * siga sin concederse.
 */
@Composable
fun BannerPermiso(
    onActivar: () -> Unit,
    onDescartar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val acento = FinanzasTheme.colores.egreso
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, acento.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Dimens.EspacioM, top = Dimens.EspacioM, bottom = Dimens.EspacioXS),
            horizontalArrangement = Arrangement.spacedBy(Dimens.EspacioS),
        ) {
            IconoCategoriaCircular(
                icono = Icons.Filled.NotificationsOff,
                colorFondo = fondoCategoria(acento, intensidad = 0.28f),
                colorIcono = acento,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "La deteccion automatica esta desactivada",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Activala para registrar tus movimientos sin escribirlos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.padding(top = Dimens.EspacioXXS),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onActivar) {
                        Text(text = "Activar", color = acento)
                    }
                }
            }
            IconButton(onClick = onDescartar) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Descartar aviso",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
