package com.finanzas.app.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.finanzas.app.data.local.entity.TipoMovimiento
import com.finanzas.app.ui.common.MovimientoUi
import com.finanzas.app.ui.common.colorCategoria
import com.finanzas.app.ui.common.fondoCategoria
import com.finanzas.app.ui.common.iconoCategoria
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme
import com.finanzas.app.ui.theme.TextoMontoConCentavos
import com.finanzas.app.ui.theme.formatearFechaCorta

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FilaMovimiento(
    movimiento: MovimientoUi,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contextoTransicion: ContextoTransicion? = null,
) {
    // Memoizar trabajo pesado por fila: parseo de color/fecha/icono solo si cambian esos campos,
    // no en cada recomposicion del padre (LazyColumn scrolleando).
    val colores = FinanzasTheme.colores
    val colorCategoria = remember(movimiento.categoriaColorHex, colores.textoSecundario) {
        colorCategoria(movimiento.categoriaColorHex, colores.textoSecundario)
    }
    val fondoCategoria = remember(colorCategoria) { fondoCategoria(colorCategoria) }
    val iconoCategoria = remember(movimiento.categoriaIcono) { iconoCategoria(movimiento.categoriaIcono) }
    val fechaCorta = remember(movimiento.fechaMovimiento) { formatearFechaCorta(movimiento.fechaMovimiento) }
    val subtitulo = remember(movimiento.categoriaNombre, fechaCorta) {
        "${movimiento.categoriaNombre ?: "Sin categoria"} · $fechaCorta"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .compartirLimite("movimiento-${movimiento.id}-bounds", contextoTransicion)
            .clickable(enabled = onClick != null, onClick = { onClick?.invoke() })
            .padding(vertical = Dimens.EspacioXS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconoCategoriaCircular(
            icono = iconoCategoria,
            colorFondo = fondoCategoria,
            colorIcono = colorCategoria,
            modifier = Modifier.compartirIcono("movimiento-${movimiento.id}-icono", contextoTransicion),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Dimens.EspacioS),
        ) {
            Text(
                text = movimiento.comercioOrigen,
                style = FinanzasTheme.monto.pequeno,
                modifier = Modifier.compartirLimite("movimiento-${movimiento.id}-comercio", contextoTransicion),
            )
            Text(
                text = subtitulo,
                style = FinanzasTheme.monto.pequeno,
                color = colores.textoSecundario,
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
                modifier = Modifier.compartirLimite("movimiento-${movimiento.id}-monto", contextoTransicion),
            )
        }
    }
}
