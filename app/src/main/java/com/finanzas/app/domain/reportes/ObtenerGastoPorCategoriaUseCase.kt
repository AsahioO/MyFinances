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
            movimientos.observarCategorias(),
        ) { gastos, categorias ->
            val total = gastos.sumOf { it.totalCentavos }.coerceAtLeast(1L)
            val categoriaPorId = categorias.associateBy { it.id }
            gastos.map { gasto ->
                GastoCategoria(
                    categoria = gasto.categoriaId?.let(categoriaPorId::get),
                    montoCentavos = gasto.totalCentavos,
                    proporcion = gasto.totalCentavos / total.toFloat(),
                )
            }
        }
}
