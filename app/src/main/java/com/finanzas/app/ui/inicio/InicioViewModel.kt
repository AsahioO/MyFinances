package com.finanzas.app.ui.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzas.app.data.repository.MovimientoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Por ahora solo expone el conteo de movimientos: es la prueba de punta a punta
 * de que la cadena Room -> Repositorio -> ViewModel -> Compose esta bien
 * conectada. El contenido real de Inicio (flujo del mes, ultimos movimientos)
 * se implementa despues.
 */
@HiltViewModel
class InicioViewModel @Inject constructor(
    repositorio: MovimientoRepository,
) : ViewModel() {

    val estado: StateFlow<InicioUiState> = repositorio.observarConteoMovimientos()
        .map { conteo -> InicioUiState(totalMovimientos = conteo, cargando = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = InicioUiState(),
        )
}

data class InicioUiState(
    val totalMovimientos: Int = 0,
    val cargando: Boolean = true,
)
