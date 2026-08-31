package com.finanzas.app.data.repository

import com.finanzas.app.data.local.dao.CuentaDao
import com.finanzas.app.data.local.entity.CuentaEntity
import com.finanzas.app.data.local.entity.OrigenMovimiento
import kotlinx.coroutines.flow.Flow

/** Acceso a cuentas/wallets. Separado de MovimientoRepository para que el listener de
 * notificaciones no dependa de todo el CRUD de movimientos. */
class CuentaRepository(
    private val cuentaDao: CuentaDao,
) {

    fun observarActivas(): Flow<List<CuentaEntity>> = cuentaDao.observarActivas()

    suspend fun obtenerPorOrigen(origen: OrigenMovimiento): CuentaEntity? =
        cuentaDao.obtenerPorOrigen(origen)
}
