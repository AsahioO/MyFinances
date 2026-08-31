package com.finanzas.app.ui.agregar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.finanzas.app.data.local.entity.CategoriaEntity
import com.finanzas.app.data.local.entity.CuentaEntity
import com.finanzas.app.data.local.entity.EstadoMovimiento
import com.finanzas.app.data.local.entity.MovimientoEntity
import com.finanzas.app.data.local.entity.OrigenMovimiento
import com.finanzas.app.data.local.entity.TipoMovimiento
import com.finanzas.app.data.repository.CuentaRepository
import com.finanzas.app.data.repository.MovimientoRepository
import com.finanzas.app.ui.navigation.RutaAgregarManual
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * [montoDigitos] son los digitos crudos que escribe el usuario (sin punto:
 * "21900" = $219.00), no el texto formateado que se ve en pantalla -
 * MontoVisualTransformation en el Composable se encarga de mostrarlo como
 * "$219.00". Guardar los digitos crudos evita ambiguedad de parseo (donde
 * quedo el punto decimal) que si tendria guardar el texto ya formateado.
 */
data class AgregarMovimientoUiState(
    val montoDigitos: String = "",
    val tipo: TipoMovimiento = TipoMovimiento.EGRESO,
    val cuentaId: Long? = null,
    val categoriaId: Long? = null,
    val fechaMillis: Long = System.currentTimeMillis(),
    val comercio: String = "",
    val notas: String = "",
    val cuentas: List<CuentaEntity> = emptyList(),
    val categorias: List<CategoriaEntity> = emptyList(),
    val tienePrefill: Boolean = false,
    val guardado: Boolean = false,
) {
    val montoCentavos: Long? get() = montoDigitos.toLongOrNull()
    val puedeGuardar: Boolean get() = (montoCentavos ?: 0L) > 0L
    val fechaEnFuturo: Boolean get() = fechaMillis > System.currentTimeMillis()
}

/**
 * A diferencia de InicioViewModel/ReportesViewModel (puros, solo lectura via
 * combine().stateIn()), este VM tiene estado propio mutable: es el primer
 * formulario de escritura de la app. El StateFlow expuesto sigue el mismo
 * nombre/forma (`estado`) que el resto para mantener el patron reconocible.
 */
@HiltViewModel
class AgregarMovimientoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val movimientos: MovimientoRepository,
    private val cuentas: CuentaRepository,
) : ViewModel() {

    private val _estado: MutableStateFlow<AgregarMovimientoUiState>

    init {
        val prefill = savedStateHandle.toRoute<RutaAgregarManual>()
        val tienePrefill = prefill.montoCentavos >= 0 || prefill.comercio != null
        _estado = MutableStateFlow(
            AgregarMovimientoUiState(
                montoDigitos = if (prefill.montoCentavos >= 0) prefill.montoCentavos.toString() else "",
                comercio = prefill.comercio.orEmpty(),
                fechaMillis = if (prefill.fechaMillis >= 0) prefill.fechaMillis else System.currentTimeMillis(),
                tienePrefill = tienePrefill,
            ),
        )

        viewModelScope.launch {
            combine(cuentas.observarActivas(), movimientos.observarCategorias()) { c, cat -> c to cat }
                .collect { (listaCuentas, listaCategorias) ->
                    _estado.update {
                        it.copy(
                            cuentas = listaCuentas,
                            categorias = listaCategorias,
                            // Preselecciona la primera cuenta activa (Efectivo, por orden)
                            // solo si el usuario no eligio ya una a mano.
                            cuentaId = it.cuentaId ?: listaCuentas.firstOrNull()?.id,
                        )
                    }
                }
        }
    }

    val estado: StateFlow<AgregarMovimientoUiState> = _estado.asStateFlow()

    /** Filtra a solo digitos y limita a 9 (plan.md: borde rojo si el monto tiene mas). */
    fun onMontoChange(texto: String) =
        _estado.update { it.copy(montoDigitos = texto.filter(Char::isDigit).take(9)) }

    fun onTipoChange(tipo: TipoMovimiento) = _estado.update { it.copy(tipo = tipo) }

    fun onCuentaChange(id: Long) = _estado.update { it.copy(cuentaId = id) }

    /** Toca la misma categoria ya seleccionada para volver a "Sin categorizar". */
    fun onCategoriaChange(id: Long) =
        _estado.update { it.copy(categoriaId = if (it.categoriaId == id) null else id) }

    fun onFechaChange(millis: Long) = _estado.update { it.copy(fechaMillis = millis) }

    fun onComercioChange(texto: String) = _estado.update { it.copy(comercio = texto) }

    fun onNotasChange(texto: String) = _estado.update { it.copy(notas = texto) }

    fun guardar() {
        val actual = _estado.value
        val monto = actual.montoCentavos ?: return
        if (monto <= 0L) return

        viewModelScope.launch {
            val ahora = System.currentTimeMillis()
            movimientos.insertarMovimiento(
                MovimientoEntity(
                    montoCentavos = monto,
                    tipo = actual.tipo,
                    origen = OrigenMovimiento.MANUAL,
                    comercioOrigen = actual.comercio.trim().ifBlank { null },
                    categoriaId = actual.categoriaId,
                    cuentaId = actual.cuentaId,
                    fechaMovimiento = actual.fechaMillis,
                    fechaRegistro = ahora,
                    // Lo capturado a mano nace CONFIRMADO (Enums.kt): a diferencia
                    // de lo detectado por notificacion, el usuario ya lo reviso al
                    // llenar el formulario.
                    estado = EstadoMovimiento.CONFIRMADO,
                    notas = actual.notas.trim().ifBlank { null },
                ),
            )
            _estado.update { it.copy(guardado = true) }
        }
    }
}
