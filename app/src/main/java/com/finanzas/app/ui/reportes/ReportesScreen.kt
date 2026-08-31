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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finanzas.app.data.local.entity.CategoriaEntity
import com.finanzas.app.data.local.entity.TipoMovimiento
import com.finanzas.app.domain.reportes.GastoCategoria
import com.finanzas.app.ui.common.MovimientoUi
import com.finanzas.app.ui.common.colorCategoria
import com.finanzas.app.ui.common.iconoCategoria
import com.finanzas.app.ui.components.EncabezadoSeccion
import com.finanzas.app.ui.components.FilaMovimiento
import com.finanzas.app.ui.components.FondoPantalla
import com.finanzas.app.ui.reportes.components.DonutChartGastos
import com.finanzas.app.ui.reportes.components.SegmentoDonut
import com.finanzas.app.ui.reportes.components.TarjetaTopMover
import com.finanzas.app.ui.theme.AppFinanzasTheme
import com.finanzas.app.ui.theme.ColoresSemanticos
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme

/**
 * Reportes es una vista previa, no el historial: el donut, los top movers y
 * hasta MAX_TRANSACCIONES_PREVIEW movimientos recientes. El historial completo
 * vive en Movimientos (onVerTodas).
 */
private const val MAX_TRANSACCIONES_PREVIEW = 8

@Composable
fun ReportesScreen(
    onVerTodas: () -> Unit,
    onVerTodasCategorias: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportesViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    ReportesContenido(
        estado = estado,
        onVerTodas = onVerTodas,
        onVerTodasCategorias = onVerTodasCategorias,
        modifier = modifier,
    )
}

@Composable
private fun ReportesContenido(
    estado: ReportesUiState,
    onVerTodas: () -> Unit,
    onVerTodasCategorias: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Cap de la vista previa en la UI, sin mutar el estado: el LazyColumn
    // raiz no crece con el historial. La fuente ya llega limitada por SQL,
    // este es el corte fino de 8 para Reportes.
    val transaccionesPreview by remember(estado.transaccionesRecientes) {
        derivedStateOf { estado.transaccionesRecientes.take(MAX_TRANSACCIONES_PREVIEW) }
    }

    FondoPantalla(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Dimens.EspacioL),
            verticalArrangement = Arrangement.spacedBy(Dimens.EspacioL),
        ) {
            item {
                DonutChartGastos(
                    segmentos = estado.segmentosDonut,
                    totalCentavos = estado.totalGastadoCentavos,
                    modifier = Modifier.testTag(CLAVE_DONUT),
                )
            }

            if (estado.topMovers.isNotEmpty()) {
                item {
                    EncabezadoSeccion(
                        titulo = "Top movers",
                        textoAccion = "Ver todas",
                        onAccion = onVerTodasCategorias,
                    )
                }
                item {
                    TopMovers(topMovers = estado.topMovers)
                }
            }

            item {
                EncabezadoSeccion(
                    titulo = "Transacciones",
                    textoAccion = "Ver todas".takeIf { transaccionesPreview.isNotEmpty() },
                    onAccion = onVerTodas.takeIf { transaccionesPreview.isNotEmpty() },
                )
            }

            if (transaccionesPreview.isEmpty()) {
                item {
                    Text(
                        text = "Aun no hay movimientos",
                        style = FinanzasTheme.monto.pequeno,
                        color = FinanzasTheme.colores.textoSecundario,
                    )
                }
            }

            items(transaccionesPreview, key = { it.id }) { movimiento ->
                FilaMovimiento(
                    movimiento = movimiento,
                    modifier = Modifier.testTag("transaction_${movimiento.id}"),
                )
            }
        }
    }
}

@Composable
private fun TopMovers(
    topMovers: List<GastoCategoria>,
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
                color = colorCategoria(gasto.categoria?.color, FinanzasTheme.colores.textoSecundario),
                icono = iconoCategoria(gasto.categoria?.icono),
                montosOrdenados = gasto.montosOrdenados,
                indice = indice,
                modifier = Modifier.testTag("topmover_${gasto.categoria?.id ?: -1L}"),
            )
        }
    }
}

private const val CLAVE_DONUT = "donut_gastos"

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun ReportesConMuchasTransaccionesPreview() {
    AppFinanzasTheme {
        ReportesContenido(
            estado = estadoMockConMuchasTransacciones(),
            onVerTodas = {},
            onVerTodasCategorias = {},
        )
    }
}

/**
 * Cubre a proposito ambos casos del sparkline condicional (punto 12): Comida y
 * Transporte tienen 3+ montos (con sparkline), Entretenimiento y Servicios
 * tienen 1-2 (sin sparkline).
 */
private fun estadoMockConMuchasTransacciones(): ReportesUiState {
    val nombres = listOf("Comida", "Transporte", "Entretenimiento", "Servicios")
    val iconos = listOf("Restaurant", "DirectionsCar", "SportsEsports", "Bolt")
    val coloresHex = listOf("#E07856", "#5B8DEF", "#6FA087", "#F2B134")
    val montosPorCategoria = listOf(
        listOf(6_000L, 12_000L, 5_000L, 9_000L, 8_000L),
        listOf(9_000L, 6_000L, 8_000L, 7_000L),
        listOf(11_000L, 9_000L),
        listOf(10_000L),
    )
    val montos = montosPorCategoria.map { it.sum() }
    val total = montos.sum()
    val gastos = nombres.mapIndexed { indice, nombre ->
        GastoCategoria(
            categoria = CategoriaEntity(id = (indice + 1).toLong(), nombre = nombre, icono = iconos[indice], color = coloresHex[indice]),
            montoCentavos = montos[indice],
            proporcion = montos[indice] / total.toFloat(),
            montosOrdenados = montosPorCategoria[indice],
        )
    }
    val colores = ColoresSemanticos()
    return ReportesUiState(
        cargando = false,
        totalGastadoCentavos = total,
        segmentosDonut = gastos.map { gasto ->
            SegmentoDonut(
                color = colorCategoria(gasto.categoria?.color, colores.textoSecundario),
                proporcion = gasto.proporcion,
                etiqueta = gasto.categoria?.nombre ?: "Sin categoria",
            )
        },
        topMovers = gastos,
        transaccionesRecientes = (1..24).map { i ->
            val indiceCategoria = (i - 1) % nombres.size
            MovimientoUi(
                id = i.toLong(),
                comercioOrigen = "Comercio ${(i % 7) + 1}",
                categoriaNombre = nombres[indiceCategoria],
                montoCentavos = 5_000L + i * 173L,
                tipo = TipoMovimiento.EGRESO,
                colorOrigen = colores.origenAutomatico,
                fechaMovimiento = 0L,
                pendienteRevision = i % 5 == 0,
                categoriaIcono = iconos[indiceCategoria],
                categoriaColorHex = coloresHex[indiceCategoria],
            )
        },
    )
}
