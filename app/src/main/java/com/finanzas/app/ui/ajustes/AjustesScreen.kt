package com.finanzas.app.ui.ajustes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finanzas.app.data.local.entity.CategoriaEntity
import com.finanzas.app.ui.common.colorCategoria
import com.finanzas.app.ui.common.iconoCategoria
import com.finanzas.app.ui.components.FondoPantalla
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme

/**
 * Hub de Ajustes (plan.md §3): contenedor de lista, cada opcion nueva es una
 * seccion mas. Gestion de bancos conectados (toggle) y listado de categorias.
 */
@Composable
fun AjustesScreen(
    modifier: Modifier = Modifier,
    viewModel: AjustesViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    FondoPantalla(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.EspacioL, vertical = Dimens.EspacioM),
            verticalArrangement = Arrangement.spacedBy(Dimens.EspacioL),
        ) {
            Text(text = "Ajustes", style = FinanzasTheme.monto.mediano)

            Text(
                text = "Bancos conectados",
                style = FinanzasTheme.monto.pequeno,
                color = FinanzasTheme.colores.textoSecundario,
            )
            estado.bancos.forEach { banco ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = banco.nombreDisplay, style = FinanzasTheme.monto.pequeno, modifier = Modifier.weight(1f))
                    Switch(
                        checked = banco.activo,
                        onCheckedChange = { activo -> viewModel.cambiarActivoBanco(banco, activo) },
                    )
                }
            }

            Text(
                text = "Categorias",
                style = FinanzasTheme.monto.pequeno,
                color = FinanzasTheme.colores.textoSecundario,
            )
            if (estado.categorias.isEmpty()) {
                Text(
                    text = "Sin categorias",
                    style = FinanzasTheme.monto.pequeno,
                    color = FinanzasTheme.colores.textoSecundario,
                )
            } else {
                estado.categorias.forEach { categoria ->
                    FilaCategoria(categoria)
                }
            }
        }
    }
}

@Composable
private fun FilaCategoria(categoria: CategoriaEntity, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.EspacioXS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = iconoCategoria(categoria.icono),
            contentDescription = null,
            tint = colorCategoria(categoria.color, FinanzasTheme.colores.textoSecundario),
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.height(Dimens.EspacioXS))
        Text(text = categoria.nombre, style = FinanzasTheme.monto.pequeno, modifier = Modifier.weight(1f))
    }
}
