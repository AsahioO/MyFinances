package com.finanzas.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.finanzas.app.data.local.entity.EstadoMovimiento
import com.finanzas.app.data.local.entity.MovimientoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoDao {

    @Insert
    suspend fun insertar(movimiento: MovimientoEntity): Long

    @Insert
    suspend fun insertarTodos(movimientos: List<MovimientoEntity>): List<Long>

    @Update
    suspend fun actualizar(movimiento: MovimientoEntity)

    @Delete
    suspend fun eliminar(movimiento: MovimientoEntity)

    @Query("SELECT * FROM movimiento WHERE id = :id")
    suspend fun obtenerPorId(id: Long): MovimientoEntity?

    @Query("SELECT * FROM movimiento WHERE id = :id")
    fun observarPorId(id: Long): Flow<MovimientoEntity?>

    @Query("SELECT * FROM movimiento ORDER BY fechaMovimiento DESC, id DESC")
    fun observarTodos(): Flow<List<MovimientoEntity>>

    @Query("SELECT COUNT(*) FROM movimiento")
    fun observarConteo(): Flow<Int>

    @Query("SELECT * FROM movimiento WHERE estado = :estado ORDER BY fechaMovimiento DESC, id DESC")
    fun observarPorEstado(estado: EstadoMovimiento): Flow<List<MovimientoEntity>>

    @Query(
        """
        SELECT * FROM movimiento
        WHERE fechaMovimiento BETWEEN :desde AND :hasta
        ORDER BY fechaMovimiento DESC, id DESC
        """,
    )
    fun observarEnRango(desde: Long, hasta: Long): Flow<List<MovimientoEntity>>
}
