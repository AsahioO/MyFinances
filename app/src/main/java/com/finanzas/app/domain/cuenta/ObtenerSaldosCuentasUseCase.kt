package com.finanzas.app.domain.cuenta

import com.finanzas.app.data.local.entity.CuentaEntity
import com.finanzas.app.data.repository.MovimientoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class SaldoCuenta(
    val cuenta: CuentaEntity,
    val saldoCentavos: Long,
)

/**
 * Saldo por cuenta = saldoInicialCentavos + ingresos - egresos de sus movimientos.
 * Nunca se cachea: se recalcula reactivamente para no desincronizarse si un
 * movimiento se edita o se borra.
 */
class ObtenerSaldosCuentasUseCase @Inject constructor(
    private val repositorio: MovimientoRepository,
) {
    operator fun invoke(): Flow<List<SaldoCuenta>> =
        combine(
            repositorio.observarCuentasActivas(),
            repositorio.observarMovimientoPorCuenta(),
        ) { cuentas, movimientosPorCuenta ->
            val porCuentaId = movimientosPorCuenta.associateBy { it.cuentaId }
            cuentas.map { cuenta ->
                val delta = porCuentaId[cuenta.id]
                val saldo = cuenta.saldoInicialCentavos +
                    (delta?.totalIngresosCentavos ?: 0L) -
                    (delta?.totalEgresosCentavos ?: 0L)
                SaldoCuenta(cuenta, saldo)
            }
        }
}
