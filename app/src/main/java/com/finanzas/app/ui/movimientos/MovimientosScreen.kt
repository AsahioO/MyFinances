package com.finanzas.app.ui.movimientos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finanzas.app.data.local.entity.OrigenMovimiento
import com.finanzas.app.data.local.entity.TipoMovimiento
import com.finanzas.app.ui.components.FilaMovimiento
import com.finanzas.app.ui.components.FondoPantalla
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme

/**
 * Historial completo con filtros (fecha, tipo, banco, pendientes) y
 * paginacion LIMIT/OFFSET en SQL (plan.md §3 y §7). Cada fila lleva al
 * detalle ([onFilaClick]).
 */
@Composable
fun MovimientosScreen(
    onFilaClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MovimientosViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    FondoPantalla(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            FiltrosHistorial(estado, viewModel)

            when {
                estado.cargando && estado.movimientos.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                estado.movimientos.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Sin movimientos con estos filtros",
                            style = FinanzasTheme.monto.mediano,
                            color = FinanzasTheme.colores.textoSecundario,
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = Dimens.EspacioL, vertical = Dimens.EspacioM),
                        verticalArrangement = Arrangement.spacedBy(Dimens.EspacioXS),
                    ) {
                        items(estado.movimientos, key = { it.id }) { movimiento ->
                            FilaMovimiento(
                                movimiento = movimiento,
                                onClick = { onFilaClick(movimiento.id) },
                            )
                        }
                        if (estado.hayMas) {
                            item {
                                TextButton(
                                    onClick = viewModel::cargarMas,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("Cargar mas movimientos")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltrosHistorial(estado: MovimientosUiState, viewModel: MovimientosViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.EspacioL),
        verticalArrangement = Arrangement.spacedBy(Dimens.EspacioXS),
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.EspacioXS)) {
            items(RangoHistorico.entries) { rango ->
                FilterChip(
                    selected = estado.rango == rango,
                    onClick = { viewModel.cambiarRango(rango) },
                    label = { Text(if (rango == RangoHistorico.ESTE_MES) "Este mes" else "Todo") },
                )
            }
            // Tipo: tocar el activo lo deselecciona (vuelve a "Todos")
            items(TipoMovimiento.entries) { tipo ->
                FilterChip(
                    selected = estado.filtros.tipo == tipo,
                    onClick = {
                        viewModel.cambiarTipo(if (estado.filtros.tipo == tipo) null else tipo)
                    },
                    label = { Text(if (tipo == TipoMovimiento.EGRESO) "Egresos" else "Ingresos") },
                )
            }
            // Origen
            items(OrigenMovimiento.entries) { origen ->
                FilterChip(
                    selected = estado.filtros.origen == origen,
                    onClick = {
                        viewModel.cambiarOrigen(if (estado.filtros.origen == origen) null else origen)
                    },
                    label = { Text(if (origen == OrigenMovimiento.MANUAL) "Manual" else origen.name) },
                )
            }
            item {
                FilterChip(
                    selected = estado.filtros.soloPendientes,
                    onClick = { viewModel.cambiarSoloPendientes(!estado.filtros.soloPendientes) },
                    label = { Text("Pendientes") },
                )
            }
        }
    }
}
