package com.finanzas.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.finanzas.app.data.local.entity.CuentaEntity
import com.finanzas.app.data.local.entity.OrigenMovimiento
import kotlinx.coroutines.flow.Flow

@Dao
interface CuentaDao {

    @Insert
    suspend fun insertar(cuenta: CuentaEntity): Long

    @Update
    suspend fun actualizar(cuenta: CuentaEntity)

    @Query("SELECT * FROM cuenta WHERE id = :id")
    suspend fun obtenerPorId(id: Long): CuentaEntity?

    /** Usada por ProcesarNotificacionUseCase para asignar cuenta a un movimiento automatico. */
    @Query("SELECT * FROM cuenta WHERE origen = :origen AND archivada = 0 LIMIT 1")
    suspend fun obtenerPorOrigen(origen: OrigenMovimiento): CuentaEntity?

    @Query("SELECT * FROM cuenta WHERE archivada = 0 ORDER BY orden ASC, id ASC")
    fun observarActivas(): Flow<List<CuentaEntity>>
}
