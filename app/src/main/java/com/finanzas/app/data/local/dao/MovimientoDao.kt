package com.finanzas.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.finanzas.app.data.local.entity.EstadoMovimiento
import com.finanzas.app.data.local.entity.MovimientoEntity
import com.finanzas.app.data.local.entity.TipoMovimiento
import kotlinx.coroutines.flow.Flow

/** Total gastado/ingresado por categoria en un rango de fechas. */
data class GastoPorCategoria(
    val categoriaId: Long?,
    val totalCentavos: Long,
)

/** Ingresos y egresos totales de un rango de fechas. */
data class FlujoPeriodo(
    val totalIngresosCentavos: Long,
    val totalEgresosCentavos: Long,
)

/** Ingresos y egresos totales agrupados por cuenta, sin limite de fecha. */
data class MovimientoPorCuenta(
    val cuentaId: Long?,
    val totalIngresosCentavos: Long,
    val totalEgresosCentavos: Long,
)

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

    @Query(
        """
        SELECT categoriaId, SUM(montoCentavos) AS totalCentavos
        FROM movimiento
        WHERE tipo = :tipo AND fechaMovimiento BETWEEN :desde AND :hasta
        GROUP BY categoriaId
        ORDER BY totalCentavos DESC
        """,
    )
    fun observarGastoPorCategoria(
        desde: Long,
        hasta: Long,
        tipo: TipoMovimiento = TipoMovimiento.EGRESO,
    ): Flow<List<GastoPorCategoria>>

    @Query(
        """
        SELECT
            COALESCE(SUM(CASE WHEN tipo = 'INGRESO' THEN montoCentavos ELSE 0 END), 0) AS totalIngresosCentavos,
            COALESCE(SUM(CASE WHEN tipo = 'EGRESO' THEN montoCentavos ELSE 0 END), 0) AS totalEgresosCentavos
        FROM movimiento
        WHERE fechaMovimiento BETWEEN :desde AND :hasta
        """,
    )
    fun observarFlujoEnRango(desde: Long, hasta: Long): Flow<FlujoPeriodo>

    @Query(
        """
        SELECT cuentaId,
            SUM(CASE WHEN tipo = 'INGRESO' THEN montoCentavos ELSE 0 END) AS totalIngresosCentavos,
            SUM(CASE WHEN tipo = 'EGRESO' THEN montoCentavos ELSE 0 END) AS totalEgresosCentavos
        FROM movimiento
        GROUP BY cuentaId
        """,
    )
    fun observarMovimientoPorCuenta(): Flow<List<MovimientoPorCuenta>>
}
