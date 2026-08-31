package com.finanzas.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.finanzas.app.data.local.entity.CategoriaEntity
import com.finanzas.app.ui.common.colorCategoria
import com.finanzas.app.ui.common.iconoCategoria
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme

/**
 * Grid de categorias seleccionables (3 por fila), compartido por el formulario
 * de alta manual y el detalle de movimiento. Tocar la seleccionada la
 * deselecciona (volver a "Sin categorizar").
 */
@Composable
fun GridCategorias(
    categorias: List<CategoriaEntity>,
    seleccionada: Long?,
    onChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.EspacioXS),
    ) {
        categorias.chunked(3).forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.EspacioXS),
            ) {
                fila.forEach { categoria ->
                    ChipCategoria(
                        categoria = categoria,
                        seleccionada = categoria.id == seleccionada,
                        onClick = { onChange(categoria.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(3 - fila.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ChipCategoria(
    categoria: CategoriaEntity,
    seleccionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorBase = colorCategoria(categoria.color, FinanzasTheme.colores.textoSecundario)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.RadioChip))
            .background(if (seleccionada) colorBase.copy(alpha = 0.18f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = Dimens.EspacioS),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(imageVector = iconoCategoria(categoria.icono), contentDescription = null, tint = colorBase)
        Spacer(modifier = Modifier.height(Dimens.EspacioXXS))
        Text(
            text = categoria.nombre,
            style = FinanzasTheme.monto.pequeno,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}
