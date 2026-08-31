package com.finanzas.app.ui.reportes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finanzas.app.data.local.entity.CategoriaEntity
import com.finanzas.app.domain.reportes.GastoCategoria
import com.finanzas.app.ui.common.colorCategoria
import com.finanzas.app.ui.common.fondoCategoria
import com.finanzas.app.ui.common.iconoCategoria
import com.finanzas.app.ui.components.FondoPantalla
import com.finanzas.app.ui.components.IconoCategoriaCircular
import com.finanzas.app.ui.components.PillPorcentaje
import com.finanzas.app.ui.theme.AppFinanzasTheme
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme
import com.finanzas.app.ui.theme.TextoMontoConCentavos
import kotlin.math.roundToInt

/**
 * "Ver todas" de Top Movers: mismas categorias del mes, sin el take(4) de
 * Reportes. Reusa ObtenerGastoPorCategoriaUseCase, asi que nunca se desincroniza
 * de los montos que ya se ven en el donut/top movers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriasScreen(
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoriasViewModel = hiltViewModel(),
) {
    val gastos by viewModel.gastos.collectAsStateWithLifecycle()
    CategoriasContenido(gastos = gastos, onCerrar = onCerrar, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoriasContenido(
    gastos: List<GastoCategoria>,
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FondoPantalla(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Categorias", style = FinanzasTheme.monto.mediano) },
                    navigationIcon = {
                        IconButton(onClick = onCerrar) {
                            Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
        ) { paddingInterno ->
            if (gastos.isEmpty()) {
                Text(
                    text = "Aun no hay gasto este mes",
                    style = FinanzasTheme.monto.pequeno,
                    color = FinanzasTheme.colores.textoSecundario,
                    modifier = Modifier.padding(paddingInterno).padding(Dimens.EspacioL),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Dimens.EspacioL,
                        end = Dimens.EspacioL,
                        top = paddingInterno.calculateTopPadding() + Dimens.EspacioM,
                        bottom = Dimens.EspacioL,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.EspacioM),
                ) {
                    items(gastos, key = { it.categoria?.id ?: -1L }) { gasto ->
                        FilaCategoriaGasto(gasto)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaCategoriaGasto(gasto: GastoCategoria, modifier: Modifier = Modifier) {
    val color = colorCategoria(gasto.categoria?.color, FinanzasTheme.colores.textoSecundario)
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconoCategoriaCircular(
            icono = iconoCategoria(gasto.categoria?.icono),
            colorFondo = fondoCategoria(color),
            colorIcono = color,
        )
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = Dimens.EspacioS),
        ) {
            Text(text = gasto.categoria?.nombre ?: "Sin categoria", style = FinanzasTheme.monto.pequeno)
            TextoMontoConCentavos(
                centavos = gasto.montoCentavos,
                estiloEntero = FinanzasTheme.monto.pequeno,
                estiloCentavos = FinanzasTheme.monto.pequeno,
            )
        }
        PillPorcentaje(porcentaje = (gasto.proporcion * 100).roundToInt(), colorFondo = color)
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun CategoriasScreenPreview() {
    AppFinanzasTheme {
        val nombres = listOf("Comida", "Transporte", "Ocio", "Servicios", "Salud")
        val iconos = listOf("Restaurant", "DirectionsCar", "SportsEsports", "Bolt", "LocalHospital")
        val coloresHex = listOf("#E07856", "#5B8DEF", "#6FA087", "#F2B134", "#D9556B")
        val montos = listOf(40_000L, 30_000L, 20_000L, 15_000L, 5_000L)
        val total = montos.sum()
        val gastos = nombres.mapIndexed { indice, nombre ->
            GastoCategoria(
                categoria = CategoriaEntity(id = (indice + 1).toLong(), nombre = nombre, icono = iconos[indice], color = coloresHex[indice]),
                montoCentavos = montos[indice],
                proporcion = montos[indice] / total.toFloat(),
            )
        }
        CategoriasContenido(gastos = gastos, onCerrar = {})
    }
}
