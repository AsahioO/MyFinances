package com.finanzas.app.ui.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzas.app.data.local.entity.BancoConfigEntity
import com.finanzas.app.data.local.entity.CategoriaEntity
import com.finanzas.app.data.repository.MovimientoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AjustesUiState(
    val bancos: List<BancoConfigEntity> = emptyList(),
    val categorias: List<CategoriaEntity> = emptyList(),
)

/**
 * Hub de Ajustes (plan.md pantalla 8/9): hoy gestiona el toggle de bancos
 * conectados y lista las categorias; cada opcion nueva es una seccion mas.
 */
@HiltViewModel
class AjustesViewModel @Inject constructor(
    private val movimientos: MovimientoRepository,
) : ViewModel() {

    val estado: StateFlow<AjustesUiState> = combine(
        movimientos.observarBancos(),
        movimientos.observarCategorias(),
    ) { bancos, categorias ->
        AjustesUiState(bancos = bancos, categorias = categorias)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AjustesUiState(),
    )

    fun cambiarActivoBanco(banco: BancoConfigEntity, activo: Boolean) {
        viewModelScope.launch {
            movimientos.cambiarActivoBanco(banco.packageName, activo)
        }
    }
}
