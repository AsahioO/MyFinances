package com.finanzas.app.ui.inicio.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.finanzas.app.ui.common.fondoCategoria
import com.finanzas.app.ui.components.IconoCategoriaCircular
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme

/**
 * El bucle de valor de la app hecho tarjeta: el banco detecta, el usuario
 * confirma. FilledTonalCard M3; violeta porque estos movimientos vienen de
 * una notificacion bancaria (plan.md#8: violeta = detectado automaticamente).
    FilledTonalCard como tal no existe en M3 (las variantes de Card son
 * filled/elevated/outlined): se usa Card con colores de secondaryContainer,
 * que es el rol tonal por defecto.
 */
@Composable
fun TarjetaPendientes(
    cantidad: Int,
    onRevisar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val acento = FinanzasTheme.colores.origenAutomatico
    val titulo = remember(cantidad) {
        if (cantidad == 1) "1 movimiento por revisar" else "$cantidad movimientos por revisar"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            // Un solo nodo para TalkBack: la tarjeta entera es el boton.
            .semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        onClick = onRevisar,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.EspacioM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.EspacioS),
        ) {
            IconoCategoriaCircular(
                icono = Icons.Filled.PendingActions,
                colorFondo = fondoCategoria(acento, intensidad = 0.28f),
                colorIcono = acento,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = "Detectados automaticamente",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = acento,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
