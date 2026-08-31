package com.finanzas.app.domain.reportes

import com.finanzas.app.data.local.entity.CategoriaEntity
import com.finanzas.app.data.repository.MovimientoRepository
import com.finanzas.app.data.repository.ReportesRepository
import com.finanzas.app.domain.model.RangoFechas
import com.finanzas.app.domain.model.rangoMesActual
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class GastoCategoria(
    /** null = "Sin categoria". */
    val categoria: CategoriaEntity?,
    val montoCentavos: Long,
    /** 0f..1f, participacion sobre el total de gasto del periodo. */
    val proporcion: Float,
    /** Montos individuales en orden cronologico dentro del periodo; base del sparkline de Top Movers. */
    val montosOrdenados: List<Long> = emptyList(),
)

/**
 * Gasto por categoria del periodo, ordenado de mayor a menor. Reportes usa
 * .take(4) de esta misma lista para "top movers": una sola fuente de verdad
 * para el donut y las tarjetas de top movers.
 */
class ObtenerGastoPorCategoriaUseCase @Inject constructor(
    private val reportes: ReportesRepository,
    private val movimientos: MovimientoRepository,
) {
    operator fun invoke(rango: RangoFechas = rangoMesActual()): Flow<List<GastoCategoria>> =
        combine(
            reportes.observarGastoPorCategoria(rango.desde, rango.hasta),
            reportes.observarMontosPorCategoriaEnRango(rango.desde, rango.hasta),
            movimientos.observarCategorias(),
        ) { gastos, montosCrudos, categorias ->
            val total = gastos.sumOf { it.totalCentavos }.coerceAtLeast(1L)
            val categoriaPorId = categorias.associateBy { it.id }
            // montosCrudos ya llega ordenado por fecha (ASC) desde SQL: groupBy
            // conserva ese orden dentro de cada sublista, sin re-ordenar en memoria.
            val montosPorCategoria = montosCrudos.groupBy { it.categoriaId }
            gastos.map { gasto ->
                GastoCategoria(
                    categoria = gasto.categoriaId?.let(categoriaPorId::get),
                    montoCentavos = gasto.totalCentavos,
                    proporcion = gasto.totalCentavos / total.toFloat(),
                    montosOrdenados = montosPorCategoria[gasto.categoriaId]?.map { it.montoCentavos } ?: emptyList(),
                )
            }
        }
}
