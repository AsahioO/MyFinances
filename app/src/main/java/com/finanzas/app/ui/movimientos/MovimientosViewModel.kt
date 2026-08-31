package com.finanzas.app.ui.movimientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzas.app.data.local.entity.OrigenMovimiento
import com.finanzas.app.data.local.entity.TipoMovimiento
import com.finanzas.app.data.repository.MovimientoRepository
import com.finanzas.app.data.repository.ReportesRepository
import com.finanzas.app.domain.model.rangoMesActual
import com.finanzas.app.ui.common.MovimientoUi
import com.finanzas.app.ui.common.aUi
import com.finanzas.app.ui.theme.ColoresSemanticos
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Filtros del historial. Campos null = sin restriccion; la query los ignora en SQL. */
data class FiltrosMovimientos(
    val desde: Long? = null,
    val hasta: Long? = null,
    val tipo: TipoMovimiento? = null,
    val origen: OrigenMovimiento? = null,
    val soloPendientes: Boolean = false,
)

/** Rangos de fecha predefinidos del filtro (plan.md §3: filtros de fecha). */
enum class RangoHistorico { TODO, ESTE_MES }

private const val TAMANO_PAGINA = 30

/**
 * Pagina 0 (consulta nueva) reemplaza la lista; las siguientes (cargarMas o
 * re-emision de Room por invalidacion) acumulan sin duplicar por id.
 */
internal fun acumularPagina(actual: List<MovimientoUi>, pagina: List<MovimientoUi>, offset: Int): List<MovimientoUi> =
    if (offset == 0) pagina else (actual + pagina).distinctBy { it.id }

data class MovimientosUiState(
    val movimientos: List<MovimientoUi> = emptyList(),
    val hayMas: Boolean = false,
    val cargando: Boolean = true,
    val filtros: FiltrosMovimientos = FiltrosMovimientos(),
    val rango: RangoHistorico = RangoHistorico.TODO,
)

/**
 * Primer pantalla con filtros + paginacion de la app. La consulta activa vive
 * en un MutableStateFlow: cada cambio de filtro la re-emite desde el offset 0
 * (reemplaza la lista), y [cargarMas] re-emite con offset mayor (acumula).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MovimientosViewModel @Inject constructor(
    private val reportes: ReportesRepository,
    movimientos: MovimientoRepository,
) : ViewModel() {

    private data class Consulta(val filtros: FiltrosMovimientos, val offset: Int)

    // ColoresSemanticos no depende de composicion (data class con defaults):
    // el ViewModel no es @Composable, asi que no puede leer LocalColoresSemanticos.
    private val colores = ColoresSemanticos()

    private val consulta = MutableStateFlow(Consulta(FiltrosMovimientos(), 0))

    private val _estado = MutableStateFlow(MovimientosUiState())
    val estado: StateFlow<MovimientosUiState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            combine(consulta, movimientos.observarCategorias()) { consulta, categorias ->
                Pair(consulta, categorias.associateBy { it.id })
            }.flatMapLatest { (consulta, categoriaPorId) ->
                val filtros = consulta.filtros
                reportes.observarMovimientosFiltrados(
                    desde = filtros.desde,
                    hasta = filtros.hasta,
                    tipo = filtros.tipo,
                    origen = filtros.origen,
                    soloPendientes = filtros.soloPendientes,
                    limite = TAMANO_PAGINA,
                    offset = consulta.offset,
                ).map { pagina -> Pair(consulta.offset, pagina.map { it.aUi(it.categoriaId?.let(categoriaPorId::get), colores) }) }
            }.collect { (offset, pagina) ->
                _estado.update { actual ->
                    // ponytail: una invalidacion de Room colapsa la lista a la
                    // pagina 1 en vez de conservar las cargadas; aceptable para
                    // una lista personal, se arregla con cursor-pagination si molesta.
                    val unidos = acumularPagina(actual.movimientos, pagina, offset)
                    actual.copy(
                        movimientos = unidos,
                        hayMas = pagina.size == TAMANO_PAGINA,
                        cargando = false,
                    )
                }
            }
        }
    }

    private fun aplicarFiltros(transformar: (FiltrosMovimientos) -> FiltrosMovimientos, rango: RangoHistorico? = null) {
        val nuevos = transformar(_estado.value.filtros)
        _estado.update { it.copy(filtros = nuevos, rango = rango ?: it.rango, cargando = true) }
        consulta.value = Consulta(nuevos, 0)
    }

    fun cambiarTipo(tipo: TipoMovimiento?) =
        aplicarFiltros(transformar = { filtros -> filtros.copy(tipo = tipo) })

    fun cambiarOrigen(origen: OrigenMovimiento?) =
        aplicarFiltros(transformar = { filtros -> filtros.copy(origen = origen) })

    fun cambiarSoloPendientes(valor: Boolean) =
        aplicarFiltros(transformar = { filtros -> filtros.copy(soloPendientes = valor) })

    fun cambiarRango(rango: RangoHistorico) {
        val rangoMes = rangoMesActual()
        val (desde, hasta) = when (rango) {
            RangoHistorico.TODO -> Pair(null, null)
            RangoHistorico.ESTE_MES -> Pair(rangoMes.desde, rangoMes.hasta)
        }
        aplicarFiltros(transformar = { filtros -> filtros.copy(desde = desde, hasta = hasta) }, rango = rango)
    }

    fun cargarMas() {
        val actual = _estado.value
        if (!actual.hayMas || actual.cargando) return
        consulta.value = Consulta(actual.filtros, actual.movimientos.size)
    }
}
