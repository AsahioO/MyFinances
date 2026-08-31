package com.finanzas.app.ui.reportes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzas.app.domain.reportes.GastoCategoria
import com.finanzas.app.domain.reportes.ObtenerGastoPorCategoriaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Todas las categorias con gasto en el mes actual, ordenadas de mayor a menor
 * (misma fuente que "Top movers" en Reportes, sin el take(4)).
 */
@HiltViewModel
class CategoriasViewModel @Inject constructor(
    obtenerGastoPorCategoria: ObtenerGastoPorCategoriaUseCase,
) : ViewModel() {

    val gastos: StateFlow<List<GastoCategoria>> = obtenerGastoPorCategoria()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )
}
