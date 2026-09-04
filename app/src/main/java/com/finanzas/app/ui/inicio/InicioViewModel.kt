package com.finanzas.app.ui.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzas.app.data.notificacion.EstadoPermisoNotificaciones
import com.finanzas.app.data.repository.MovimientoRepository
import com.finanzas.app.domain.cuenta.ObtenerSaldosCuentasUseCase
import com.finanzas.app.domain.cuenta.SaldoCuenta
import com.finanzas.app.domain.reportes.FlujoMes
import com.finanzas.app.domain.reportes.ObtenerFlujoDelMesUseCase
import com.finanzas.app.ui.common.MovimientoUi
import com.finanzas.app.ui.common.aUi
import com.finanzas.app.ui.theme.ColoresSemanticos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

private const val MAX_MOVIMIENTOS_RECIENTES = 5

@androidx.compose.runtime.Immutable
data class InicioUiState(
    val cargando: Boolean = true,
    val flujoMes: FlujoMes = FlujoMes(),
    val saldosCuentas: List<SaldoCuenta> = emptyList(),
    val movimientosRecientes: List<MovimientoUi> = emptyList(),
    val movimientosPendientes: Int = 0,
    /** Optimista mientras no se ha consultado: evita un parpadeo del banner al abrir. */
    val deteccionAutomaticaActiva: Boolean = true,
    val bannerPermisoDescartado: Boolean = false,
) {
    val mostrarBannerPermiso: Boolean
        get() = !cargando && !deteccionAutomaticaActiva && !bannerPermisoDescartado
}

@HiltViewModel
class InicioViewModel @Inject constructor(
    private val repositorio: MovimientoRepository,
    obtenerFlujoDelMes: ObtenerFlujoDelMesUseCase,
    obtenerSaldosCuentas: ObtenerSaldosCuentasUseCase,
    private val permisoNotificaciones: EstadoPermisoNotificaciones,
) : ViewModel() {

    // ColoresSemanticos no depende de composicion (data class con defaults):
    // el ViewModel no es @Composable, asi que no puede leer LocalColoresSemanticos.
    private val colores = ColoresSemanticos()

    // Android no avisa cuando el acceso a notificaciones cambia (plan.md#5), asi
    // que este flow no observa nada: lo empuja la pantalla en cada ON_RESUME.
    private val permisoYDescarte = MutableStateFlow(EstadoPermiso())

    // combine tipado llega hasta 5 flows: los datos de Room se resuelven en un
    // primer combine y el permiso se cruza en un segundo, en vez de caer al
    // overload de vararg con casts a mano.
    private val datos = combine(
        obtenerFlujoDelMes(),
        obtenerSaldosCuentas(),
        repositorio.observarRecientes(MAX_MOVIMIENTOS_RECIENTES),
        repositorio.observarCategorias(),
        repositorio.observarPendientesDeRevision().map { it.size },
    ) { flujo, saldos, movimientos, categorias, pendientes ->
        val categoriaPorId = categorias.associateBy { it.id }
        DatosInicio(
            flujoMes = flujo,
            saldosCuentas = saldos,
            movimientosRecientes = movimientos.map {
                it.aUi(it.categoriaId?.let(categoriaPorId::get), colores)
            },
            movimientosPendientes = pendientes,
        )
    }

    val estado: StateFlow<InicioUiState> = combine(
        datos,
        permisoYDescarte,
    ) { datos, permiso ->
        InicioUiState(
            cargando = false,
            flujoMes = datos.flujoMes,
            saldosCuentas = datos.saldosCuentas,
            movimientosRecientes = datos.movimientosRecientes,
            movimientosPendientes = datos.movimientosPendientes,
            deteccionAutomaticaActiva = permiso.deteccionActiva,
            bannerPermisoDescartado = permiso.bannerDescartado,
        )
    }.distinctUntilChanged().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InicioUiState(),
    )

    /** Llamar en cada ON_RESUME: Samsung puede revocar el acceso por su cuenta (plan.md#5). */
    fun refrescarPermisoNotificaciones() {
        permisoYDescarte.update { it.copy(deteccionActiva = permisoNotificaciones.deteccionActiva()) }
    }

    /**
     * Descarte de sesion, no persistido: plan.md#5 pide que el banner reaparezca
     * mientras el permiso siga sin concederse.
     */
    fun descartarBannerPermiso() {
        permisoYDescarte.update { it.copy(bannerDescartado = true) }
    }

    fun intentAjustesNotificaciones() = permisoNotificaciones.intentAjustes()

    private data class EstadoPermiso(
        val deteccionActiva: Boolean = true,
        val bannerDescartado: Boolean = false,
    )

    private data class DatosInicio(
        val flujoMes: FlujoMes,
        val saldosCuentas: List<SaldoCuenta>,
        val movimientosRecientes: List<MovimientoUi>,
        val movimientosPendientes: Int,
    )
}
