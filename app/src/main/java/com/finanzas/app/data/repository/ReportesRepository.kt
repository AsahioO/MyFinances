package com.finanzas.app.data.repository

import com.finanzas.app.data.local.dao.FlujoPeriodo
import com.finanzas.app.data.local.dao.GastoPorCategoria
import com.finanzas.app.data.local.dao.MovimientoDao
import com.finanzas.app.data.local.dao.MovimientoPorCuenta
import com.finanzas.app.data.local.entity.MovimientoEntity
import com.finanzas.app.data.local.entity.OrigenMovimiento
import com.finanzas.app.data.local.entity.TipoMovimiento
import kotlinx.coroutines.flow.Flow

/**
 * Agregados y listados limitados para pantallas de reportes e inicio: gasto por
 * categoria, flujo en rango, totales por cuenta. La agregacion vive en SQL
 * (GROUP BY), no en memoria, para que escalar filas no escale trabajo en la app.
 */
class ReportesRepository(
    private val movimientoDao: MovimientoDao,
) {

    fun observarGastoPorCategoria(desde: Long, hasta: Long): Flow<List<GastoPorCategoria>> =
        movimientoDao.observarGastoPorCategoria(desde, hasta)

    fun observarFlujoEnRango(desde: Long, hasta: Long): Flow<FlujoPeriodo> =
        movimientoDao.observarFlujoEnRango(desde, hasta)

    fun observarMovimientoPorCuenta(): Flow<List<MovimientoPorCuenta>> =
        movimientoDao.observarMovimientoPorCuenta()

    fun observarEnRangoLimit(desde: Long, hasta: Long, limit: Int): Flow<List<MovimientoEntity>> =
        movimientoDao.observarEnRangoLimit(desde, hasta, limit)

    /**
     * Listado LIMIT/OFFSET del historial con filtros, para MovimientosScreen.
     * El filtrado y el limite viven en SQL (ver MovimientoDao).
     */
    fun observarMovimientosFiltrados(
        desde: Long?,
        hasta: Long?,
        tipo: TipoMovimiento?,
        origen: OrigenMovimiento?,
        soloPendientes: Boolean,
        limite: Int,
        offset: Int,
    ): Flow<List<MovimientoEntity>> =
        movimientoDao.observarMovimientosFiltrados(desde, hasta, tipo, origen, soloPendientes, limite, offset)
}
