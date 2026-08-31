package com.finanzas.app.domain.cuenta

import com.finanzas.app.data.local.entity.CuentaEntity
import com.finanzas.app.data.repository.CuentaRepository
import com.finanzas.app.data.repository.ReportesRepository
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
    private val cuentas: CuentaRepository,
    private val reportes: ReportesRepository,
) {
    operator fun invoke(): Flow<List<SaldoCuenta>> =
        combine(
            cuentas.observarActivas(),
            reportes.observarMovimientoPorCuenta(),
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
