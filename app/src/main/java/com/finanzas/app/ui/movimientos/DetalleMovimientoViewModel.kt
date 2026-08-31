package com.finanzas.app.ui.movimientos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.finanzas.app.data.local.entity.CategoriaEntity
import com.finanzas.app.data.local.entity.EstadoMovimiento
import com.finanzas.app.data.local.entity.MovimientoEntity
import com.finanzas.app.data.repository.MovimientoRepository
import com.finanzas.app.ui.navigation.RutaDetalleMovimiento
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Detalle de movimiento (plan.md pantalla 5): editar categoria y notas, y
 * marcar como revisado lo que entro pendiente por notificacion. El monto no
 * se edita (viene correcto de la notificacion o se escribio a mano en el
 * formulario); si hace falta, se agrega despues.
 */
data class DetalleMovimientoUiState(
    val movimiento: MovimientoEntity? = null,
    val categorias: List<CategoriaEntity> = emptyList(),
    val categoriaId: Long? = null,
    val notas: String = "",
    val cargando: Boolean = true,
    val guardado: Boolean = false,
) {
    val hayEdiciones: Boolean get() = movimiento != null &&
        (categoriaId != movimiento.categoriaId || notas != movimiento.notas.orEmpty())

    val pendienteRevision: Boolean get() = movimiento?.estado == EstadoMovimiento.PENDIENTE_REVISION
}

@HiltViewModel
class DetalleMovimientoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val movimientos: MovimientoRepository,
) : ViewModel() {

    private val movimientoId: Long =
        savedStateHandle.toRoute<RutaDetalleMovimiento>().movimientoId

    private val _estado = MutableStateFlow(DetalleMovimientoUiState())
    val estado: StateFlow<DetalleMovimientoUiState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                movimientos.observarMovimiento(movimientoId),
                movimientos.observarCategorias(),
            ) { movimiento, categorias -> Pair(movimiento, categorias) }
                .collect { (movimiento, categorias) ->
                    _estado.update { actual ->
                        if (actual.movimiento == null && movimiento != null) {
                            // Carga inicial: copia editable sincronizada con la base.
                            actual.copy(
                                movimiento = movimiento,
                                categorias = categorias,
                                categoriaId = movimiento.categoriaId,
                                notas = movimiento.notas.orEmpty(),
                                cargando = false,
                            )
                        } else {
                            actual.copy(movimiento = movimiento, categorias = categorias)
                        }
                    }
                }
        }
    }

    /** Toca la misma categoria para volver a "Sin categorizar". */
    fun onCategoriaChange(id: Long) =
        _estado.update { it.copy(categoriaId = if (it.categoriaId == id) null else id) }

    fun onNotasChange(texto: String) = _estado.update { it.copy(notas = texto) }

    /** Persiste categoria/notas y, si [marcarRevisado], confirma el movimiento pendiente. */
    fun guardar(marcarRevisado: Boolean = false) {
        val actual = _estado.value
        val movimiento = actual.movimiento ?: return
        val nuevoEstado = if (marcarRevisado) EstadoMovimiento.CONFIRMADO else movimiento.estado
        viewModelScope.launch {
            movimientos.actualizarMovimiento(
                movimiento.copy(
                    estado = nuevoEstado,
                    categoriaId = actual.categoriaId,
                    notas = actual.notas.trim().ifBlank { null },
                ),
            )
            _estado.update { it.copy(guardado = true) }
        }
    }
}
