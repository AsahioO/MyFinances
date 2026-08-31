package com.finanzas.app.data.repository

import androidx.room.withTransaction
import com.finanzas.app.data.local.AppDatabase
import com.finanzas.app.data.local.dao.BancoConfigDao
import com.finanzas.app.data.local.dao.CategoriaDao
import com.finanzas.app.data.local.dao.MovimientoDao
import com.finanzas.app.data.local.dao.NotificacionProcesadaDao
import com.finanzas.app.data.local.entity.BancoConfigEntity
import com.finanzas.app.data.local.entity.CategoriaEntity
import com.finanzas.app.data.local.entity.EstadoMovimiento
import com.finanzas.app.data.local.entity.MovimientoEntity
import com.finanzas.app.data.local.entity.NotificacionProcesadaEntity
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a movimientos, categorias, deduplicacion y bancos configurados. Los
 * agregados para UI viven en `ReportesRepository` y las cuentas en
 * `CuentaRepository`: las pantallas inyectan solo lo que usan.
 *
 * Se construye desde `di/RepositoryModule`, no con `@Inject constructor`, para
 * que exista una sola definicion del binding.
 */
class MovimientoRepository(
    private val movimientoDao: MovimientoDao,
    private val categoriaDao: CategoriaDao,
    private val notificacionProcesadaDao: NotificacionProcesadaDao,
    private val bancoConfigDao: BancoConfigDao,
    private val db: AppDatabase,
) {

    // --- Movimientos ---

    fun observarRecientes(limit: Int): Flow<List<MovimientoEntity>> =
        movimientoDao.observarRecientes(limit)

    fun observarConteoMovimientos(): Flow<Int> = movimientoDao.observarConteo()

    fun observarMovimiento(id: Long): Flow<MovimientoEntity?> = movimientoDao.observarPorId(id)

    fun observarPendientesDeRevision(): Flow<List<MovimientoEntity>> =
        movimientoDao.observarPorEstado(EstadoMovimiento.PENDIENTE_REVISION)

    suspend fun obtenerMovimiento(id: Long): MovimientoEntity? = movimientoDao.obtenerPorId(id)

    suspend fun insertarMovimiento(movimiento: MovimientoEntity): Long =
        movimientoDao.insertar(movimiento)

    suspend fun actualizarMovimiento(movimiento: MovimientoEntity) =
        movimientoDao.actualizar(movimiento)

    suspend fun eliminarMovimiento(movimiento: MovimientoEntity) =
        movimientoDao.eliminar(movimiento)

    // --- Categorias ---

    fun observarCategorias(): Flow<List<CategoriaEntity>> = categoriaDao.observarTodas()

    suspend fun obtenerCategoria(id: Long): CategoriaEntity? = categoriaDao.obtenerPorId(id)

    suspend fun insertarCategoria(categoria: CategoriaEntity): Long =
        categoriaDao.insertar(categoria)

    suspend fun actualizarCategoria(categoria: CategoriaEntity) =
        categoriaDao.actualizar(categoria)

    suspend fun eliminarCategoria(categoria: CategoriaEntity) = categoriaDao.eliminar(categoria)

    // --- Deduplicacion de notificaciones ---

    suspend fun notificacionYaProcesada(key: String): Boolean =
        notificacionProcesadaDao.yaProcesada(key)

    suspend fun registrarNotificacionProcesada(notificacion: NotificacionProcesadaEntity) =
        notificacionProcesadaDao.insertar(notificacion)

    suspend fun purgarNotificacionesAnterioresA(antesDe: Long): Int =
        notificacionProcesadaDao.purgarAnterioresA(antesDe)

    /**
     * Inserta el movimiento automatico y su marca de deduplicacion en una sola
     * transaccion. Devuelve null si la notificacion ya se habia procesado
     * (el listener puede recibir la misma actualizada mas de una vez).
     */
    suspend fun registrarMovimientoAutomatico(
        notificacionProcesada: NotificacionProcesadaEntity,
        movimiento: MovimientoEntity,
    ): Long? = db.withTransaction {
        if (notificacionProcesadaDao.yaProcesada(notificacionProcesada.key)) {
            null
        } else {
            val movimientoId = movimientoDao.insertar(movimiento)
            notificacionProcesadaDao.insertar(notificacionProcesada.copy(movimientoId = movimientoId))
            movimientoId
        }
    }

    // --- Bancos configurados ---

    fun observarBancos(): Flow<List<BancoConfigEntity>> = bancoConfigDao.observarTodos()

    fun observarBancosActivos(): Flow<List<BancoConfigEntity>> = bancoConfigDao.observarActivos()

    suspend fun paquetesDeBancosActivos(): List<String> = bancoConfigDao.paquetesActivos()

    suspend fun guardarBanco(banco: BancoConfigEntity) = bancoConfigDao.insertar(banco)

    suspend fun cambiarActivoBanco(packageName: String, activo: Boolean) =
        bancoConfigDao.cambiarActivo(packageName, activo)
}
