package com.finanzas.app.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme

/**
 * Icono + etiqueta + accion. Sin logica de negocio: el callback lo decide quien lo usa.
 *
 * [contexto] no se usa hoy dentro del componente (AnimatedContentScope.animateEnterExit no
 * resuelve en la version de Compose que pinea este proyecto) -- se mantiene en la firma como
 * punto de enganche futuro para animar la salida de la etiqueta; por ahora simplemente
 * desaparece sin animacion propia cuando el chip deja composicion.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AccesoRapidoChip(
    icono: ImageVector,
    etiqueta: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    modifierCirculo: Modifier = Modifier,
    colores: IconButtonColors = IconButtonDefaults.iconButtonColors(
        containerColor = FinanzasTheme.colores.textoSecundario.copy(alpha = 0.1f),
    ),
    contador: Int? = null,
    @Suppress("UNUSED_PARAMETER") contexto: ContextoTransicion? = null,
) {
    Column(
        modifier = modifier.padding(Dimens.EspacioXXS),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BadgedBox(
            badge = {
                if (contador != null && contador > 0) {
                    Badge { Text("$contador") }
                }
            },
        ) {
            IconButton(
                onClick = onClick,
                modifier = modifierCirculo
                    .size(48.dp)
                    .clip(CircleShape),
                colors = colores,
            ) {
                Icon(imageVector = icono, contentDescription = etiqueta)
            }
        }
        Text(text = etiqueta, style = FinanzasTheme.monto.pequeno)
    }
}
