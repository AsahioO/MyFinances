package com.finanzas.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.finanzas.app.data.local.entity.NotificacionProcesadaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificacionProcesadaDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(notificacion: NotificacionProcesadaEntity): Long

    /** Consulta directa (no Flow): el listener necesita una respuesta puntual, no un stream. */
    @Query("SELECT EXISTS(SELECT 1 FROM notificacion_procesada WHERE key = :key)")
    suspend fun yaProcesada(key: String): Boolean

    @Query("SELECT * FROM notificacion_procesada ORDER BY fechaProcesado DESC")
    fun observarTodas(): Flow<List<NotificacionProcesadaEntity>>

    @Query("DELETE FROM notificacion_procesada WHERE fechaProcesado < :antesDe")
    suspend fun purgarAnterioresA(antesDe: Long): Int
}
