package com.finanzas.app.ui.reportes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finanzas.app.domain.reportes.GastoCategoria
import com.finanzas.app.ui.components.FilaMovimiento
import com.finanzas.app.ui.components.FondoPantalla
import com.finanzas.app.ui.reportes.components.DonutChartGastos
import com.finanzas.app.ui.reportes.components.TarjetaTopMover
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme

@Composable
fun ReportesScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportesViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    FondoPantalla(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Dimens.EspacioL),
            verticalArrangement = Arrangement.spacedBy(Dimens.EspacioL),
        ) {
            item {
                Text(
                    text = "Gasto de este mes",
                    style = FinanzasTheme.monto.pequeno,
                    color = FinanzasTheme.colores.textoSecundario,
                )
                DonutChartGastos(
                    segmentos = estado.segmentosDonut,
                    totalCentavos = estado.totalGastadoCentavos,
                )
            }

            if (estado.topMovers.isNotEmpty()) {
                item {
                    Text(text = "Top movers", style = FinanzasTheme.monto.mediano)
                }
                item {
                    TopMovers(
                        topMovers = estado.topMovers,
                        // Mismo orden que topMovers: ambos vienen de la misma lista
                        // de gastos por categoria, asi que el indice coincide.
                        colores = estado.segmentosDonut.map { it.color },
                    )
                }
            }

            item {
                Text(text = "Transacciones", style = FinanzasTheme.monto.mediano)
            }

            if (estado.transaccionesRecientes.isEmpty()) {
                item {
                    Text(
                        text = "Aun no hay movimientos",
                        style = FinanzasTheme.monto.pequeno,
                        color = FinanzasTheme.colores.textoSecundario,
                    )
                }
            }

            items(estado.transaccionesRecientes, key = { it.id }) { movimiento ->
                FilaMovimiento(movimiento = movimiento)
            }
        }
    }
}

@Composable
private fun TopMovers(
    topMovers: List<GastoCategoria>,
    colores: List<Color>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.EspacioS),
    ) {
        items(topMovers.size, key = { topMovers[it].categoria?.id ?: -1L }) { indice ->
            val gasto = topMovers[indice]
            TarjetaTopMover(
                etiqueta = gasto.categoria?.nombre ?: "Sin categoria",
                montoCentavos = gasto.montoCentavos,
                proporcion = gasto.proporcion,
                color = colores.getOrElse(indice) { FinanzasTheme.colores.textoSecundario },
            )
        }
    }
}
