package com.finanzas.app.ui.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzas.app.data.repository.MovimientoRepository
import com.finanzas.app.domain.cuenta.ObtenerSaldosCuentasUseCase
import com.finanzas.app.domain.cuenta.SaldoCuenta
import com.finanzas.app.domain.reportes.FlujoMes
import com.finanzas.app.domain.reportes.ObtenerFlujoDelMesUseCase
import com.finanzas.app.ui.common.MovimientoUi
import com.finanzas.app.ui.common.aUi
import com.finanzas.app.ui.theme.ColoresSemanticos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val MAX_MOVIMIENTOS_RECIENTES = 5

data class InicioUiState(
    val cargando: Boolean = true,
    val flujoMes: FlujoMes = FlujoMes(),
    val saldosCuentas: List<SaldoCuenta> = emptyList(),
    val movimientosRecientes: List<MovimientoUi> = emptyList(),
    val movimientosPendientes: Int = 0,
)

@HiltViewModel
class InicioViewModel @Inject constructor(
    private val repositorio: MovimientoRepository,
    obtenerFlujoDelMes: ObtenerFlujoDelMesUseCase,
    obtenerSaldosCuentas: ObtenerSaldosCuentasUseCase,
) : ViewModel() {

    // ColoresSemanticos no depende de composicion (data class con defaults):
    // el ViewModel no es @Composable, asi que no puede leer LocalColoresSemanticos.
    private val colores = ColoresSemanticos()

    val estado: StateFlow<InicioUiState> = combine(
        obtenerFlujoDelMes(),
        obtenerSaldosCuentas(),
        repositorio.observarRecientes(MAX_MOVIMIENTOS_RECIENTES),
        repositorio.observarCategorias(),
        repositorio.observarPendientesDeRevision().map { it.size },
    ) { flujo, saldos, movimientos, categorias, pendientes ->
        val categoriaPorId = categorias.associateBy { it.id }
        InicioUiState(
            cargando = false,
            flujoMes = flujo,
            saldosCuentas = saldos,
            movimientosRecientes = movimientos.take(MAX_MOVIMIENTOS_RECIENTES).map {
                it.aUi(it.categoriaId?.let(categoriaPorId::get), colores)
            },
            movimientosPendientes = pendientes,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InicioUiState(),
    )
}
