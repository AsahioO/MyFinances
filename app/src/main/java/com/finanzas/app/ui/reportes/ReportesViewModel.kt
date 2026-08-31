package com.finanzas.app.ui.reportes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzas.app.data.repository.MovimientoRepository
import com.finanzas.app.domain.model.rangoMesActual
import com.finanzas.app.domain.reportes.GastoCategoria
import com.finanzas.app.domain.reportes.ObtenerGastoPorCategoriaUseCase
import com.finanzas.app.ui.common.MovimientoUi
import com.finanzas.app.ui.common.aUi
import com.finanzas.app.ui.reportes.components.SegmentoDonut
import com.finanzas.app.ui.theme.ColoresSemanticos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val MAX_TOP_MOVERS = 4
private const val MAX_TRANSACCIONES_RECIENTES = 10

data class ReportesUiState(
    val cargando: Boolean = true,
    val totalGastadoCentavos: Long = 0L,
    val segmentosDonut: List<SegmentoDonut> = emptyList(),
    val topMovers: List<GastoCategoria> = emptyList(),
    val transaccionesRecientes: List<MovimientoUi> = emptyList(),
)

/** Paleta ciclica para categorias sin color propio: reusa tokens ya definidos, no agrega nuevos. */
private fun colorParaIndice(indice: Int, colores: ColoresSemanticos) = listOf(
    colores.origenAutomatico,
    colores.origenManual,
    colores.ingreso,
    colores.egreso,
    colores.textoSecundario,
)[indice % 5]

@HiltViewModel
class ReportesViewModel @Inject constructor(
    private val repositorio: MovimientoRepository,
    obtenerGastoPorCategoria: ObtenerGastoPorCategoriaUseCase,
) : ViewModel() {

    // ColoresSemanticos no depende de composicion: el ViewModel no es @Composable.
    private val colores = ColoresSemanticos()
    private val rango = rangoMesActual()

    val estado: StateFlow<ReportesUiState> = combine(
        obtenerGastoPorCategoria(rango),
        repositorio.observarMovimientosEnRango(rango.desde, rango.hasta),
        repositorio.observarCategorias(),
    ) { gastos, movimientos, categorias ->
        val categoriaPorId = categorias.associateBy { it.id }
        ReportesUiState(
            cargando = false,
            totalGastadoCentavos = gastos.sumOf { it.montoCentavos },
            segmentosDonut = gastos.mapIndexed { indice, gasto ->
                SegmentoDonut(
                    color = colorParaIndice(indice, colores),
                    proporcion = gasto.proporcion,
                    etiqueta = gasto.categoria?.nombre ?: "Sin categoria",
                )
            },
            topMovers = gastos.take(MAX_TOP_MOVERS),
            transaccionesRecientes = movimientos.take(MAX_TRANSACCIONES_RECIENTES).map {
                it.aUi(it.categoriaId?.let(categoriaPorId::get), colores)
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReportesUiState(),
    )
}
