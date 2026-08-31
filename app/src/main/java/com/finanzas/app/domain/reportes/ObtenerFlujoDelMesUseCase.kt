package com.finanzas.app.domain.reportes

import com.finanzas.app.data.repository.MovimientoRepository
import com.finanzas.app.domain.common.RangoFechas
import com.finanzas.app.domain.common.rangoMesActual
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class FlujoMes(
    val ingresosCentavos: Long = 0L,
    val egresosCentavos: Long = 0L,
) {
    /** Numero protagonista de Inicio: ingresos - egresos, no un "balance real". */
    val netoCentavos: Long get() = ingresosCentavos - egresosCentavos
}

class ObtenerFlujoDelMesUseCase @Inject constructor(
    private val repositorio: MovimientoRepository,
) {
    operator fun invoke(rango: RangoFechas = rangoMesActual()): Flow<FlujoMes> =
        repositorio.observarFlujoEnRango(rango.desde, rango.hasta)
            .map { FlujoMes(it.totalIngresosCentavos, it.totalEgresosCentavos) }
}
