package com.finanzas.app.ui.reportes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzas.app.data.repository.MovimientoRepository
import com.finanzas.app.data.repository.ReportesRepository
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
import kotlinx.coroutines.flow.distinctUntilChanged
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

@HiltViewModel
class ReportesViewModel @Inject constructor(
    private val repositorio: MovimientoRepository,
    private val reportes: ReportesRepository,
    obtenerGastoPorCategoria: ObtenerGastoPorCategoriaUseCase,
) : ViewModel() {

    // ColoresSemanticos no depende de composicion: el ViewModel no es @Composable.
    private val colores = ColoresSemanticos()
    // ponytail: rango fijo por instancia del VM; si la app cruza fin de mes
    // con vida, se recalculara al recrear el VM (cambio de tab). Si eso
    // molestara, inyectar Clock en vez de recalcular en cada emision.
    private val rango = rangoMesActual()

    val estado: StateFlow<ReportesUiState> = combine(
        obtenerGastoPorCategoria(rango),
        reportes.observarEnRangoLimit(rango.desde, rango.hasta, MAX_TRANSACCIONES_RECIENTES),
        repositorio.observarCategorias(),
    ) { gastos, movimientos, categorias ->
        val categoriaPorId = categorias.associateBy { it.id }
        ReportesUiState(
            cargando = false,
            totalGastadoCentavos = gastos.sumOf { it.montoCentavos },
            segmentosDonut = gastos.map { gasto ->
                SegmentoDonut(
                    color = ReportesPaleta.colorPara(gasto.categoria?.id),
                    proporcion = gasto.proporcion,
                    etiqueta = gasto.categoria?.nombre ?: "Sin categoria",
                )
            },
            topMovers = gastos.take(MAX_TOP_MOVERS),
            transaccionesRecientes = movimientos.map {
                it.aUi(it.categoriaId?.let(categoriaPorId::get), colores)
            },
        )
    }.distinctUntilChanged().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReportesUiState(),
    )
}
