package com.finanzas.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.finanzas.app.data.local.dao.MovimientoDao
import com.finanzas.app.data.local.entity.EstadoMovimiento
import com.finanzas.app.data.local.entity.MovimientoEntity
import com.finanzas.app.data.local.entity.OrigenMovimiento
import com.finanzas.app.data.local.entity.TipoMovimiento
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Verifica la cadena mas baja: entidad -> DAO -> SQLite, con base en memoria.
 * Corre en la JVM (Robolectric), no necesita emulador.
 */
@RunWith(RobolectricTestRunner::class)
class MovimientoDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: MovimientoDao

    @Before
    fun crearBase() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        )
            .addCallback(AppDatabase.SEED_CALLBACK)
            .allowMainThreadQueries()
            .build()
        dao = db.movimientoDao()
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    @Test
    fun `insertar un movimiento y leerlo de vuelta conserva todos los campos`() = runTest {
        val compra = MovimientoEntity(
            montoCentavos = 21_900L, // $219.00
            tipo = TipoMovimiento.EGRESO,
            origen = OrigenMovimiento.NU,
            comercioOrigen = "PAYPAL *NVIDIA CORP",
            categoriaId = null,
            fechaMovimiento = 1_756_500_900_000L,
            fechaRegistro = 1_756_500_905_000L,
            estado = EstadoMovimiento.PENDIENTE_REVISION,
            notas = null,
        )

        val id = dao.insertar(compra)
        val leido = dao.obtenerPorId(id)

        assertNotNull(leido)
        assertEquals(compra.copy(id = id), leido)
    }

    @Test
    fun `el conteo observable refleja los movimientos insertados`() = runTest {
        assertEquals(0, dao.observarConteo().first())

        dao.insertar(movimientoDePrueba(montoCentavos = 70_000L, tipo = TipoMovimiento.INGRESO))
        dao.insertar(movimientoDePrueba(montoCentavos = 21_900L, tipo = TipoMovimiento.EGRESO))

        assertEquals(2, dao.observarConteo().first())
    }

    @Test
    fun `la semilla deja Nu activo al crear la base`() = runTest {
        val bancos = db.bancoConfigDao().observarActivos().first()

        assertEquals(1, bancos.size)
        assertEquals("com.nu.production", bancos.first().packageName)
        assertEquals("Nu", bancos.first().nombreDisplay)
    }

    @Test
    fun `observarGastoPorCategoria suma solo egresos del rango, agrupados por categoria`() = runTest {
        val categoriaComida = db.categoriaDao().insertar(
            com.finanzas.app.data.local.entity.CategoriaEntity(nombre = "Comida", icono = "Restaurant"),
        )
        dao.insertar(
            movimientoDePrueba(montoCentavos = 10_000L, tipo = TipoMovimiento.EGRESO)
                .copy(categoriaId = categoriaComida, fechaMovimiento = 100L),
        )
        dao.insertar(
            movimientoDePrueba(montoCentavos = 5_000L, tipo = TipoMovimiento.EGRESO)
                .copy(categoriaId = categoriaComida, fechaMovimiento = 200L),
        )
        // Ingreso: no debe sumar al gasto.
        dao.insertar(
            movimientoDePrueba(montoCentavos = 999_999L, tipo = TipoMovimiento.INGRESO)
                .copy(categoriaId = categoriaComida, fechaMovimiento = 150L),
        )
        // Fuera de rango: no debe contarse.
        dao.insertar(
            movimientoDePrueba(montoCentavos = 1_000L, tipo = TipoMovimiento.EGRESO)
                .copy(categoriaId = categoriaComida, fechaMovimiento = 9_000L),
        )

        val resultado = dao.observarGastoPorCategoria(desde = 0L, hasta = 1_000L).first()

        assertEquals(1, resultado.size)
        assertEquals(categoriaComida, resultado.first().categoriaId)
        assertEquals(15_000L, resultado.first().totalCentavos)
    }

    @Test
    fun `observarFlujoEnRango suma ingresos y egresos por separado`() = runTest {
        dao.insertar(
            movimientoDePrueba(montoCentavos = 70_000L, tipo = TipoMovimiento.INGRESO)
                .copy(fechaMovimiento = 100L),
        )
        dao.insertar(
            movimientoDePrueba(montoCentavos = 21_900L, tipo = TipoMovimiento.EGRESO)
                .copy(fechaMovimiento = 200L),
        )
        dao.insertar(
            movimientoDePrueba(montoCentavos = 5_000L, tipo = TipoMovimiento.EGRESO)
                .copy(fechaMovimiento = 9_000L), // fuera de rango
        )

        val flujo = dao.observarFlujoEnRango(desde = 0L, hasta = 1_000L).first()

        assertEquals(70_000L, flujo.totalIngresosCentavos)
        assertEquals(21_900L, flujo.totalEgresosCentavos)
    }

    @Test
    fun `observarFlujoEnRango devuelve ceros cuando no hay movimientos en el rango`() = runTest {
        val flujo = dao.observarFlujoEnRango(desde = 0L, hasta = 1_000L).first()

        assertEquals(0L, flujo.totalIngresosCentavos)
        assertEquals(0L, flujo.totalEgresosCentavos)
    }

    @Test
    fun `observarMovimientoPorCuenta agrupa ingresos y egresos por cuenta, sin limite de fecha`() = runTest {
        val cuentaNu = db.cuentaDao().obtenerPorOrigen(OrigenMovimiento.NU)!!.id
        val cuentaEfectivo = db.cuentaDao().obtenerPorOrigen(OrigenMovimiento.MANUAL)!!.id

        dao.insertar(
            movimientoDePrueba(montoCentavos = 70_000L, tipo = TipoMovimiento.INGRESO)
                .copy(cuentaId = cuentaNu),
        )
        dao.insertar(
            movimientoDePrueba(montoCentavos = 21_900L, tipo = TipoMovimiento.EGRESO)
                .copy(cuentaId = cuentaNu),
        )
        dao.insertar(
            movimientoDePrueba(montoCentavos = 5_000L, tipo = TipoMovimiento.EGRESO)
                .copy(cuentaId = cuentaEfectivo),
        )

        val porCuenta = dao.observarMovimientoPorCuenta().first().associateBy { it.cuentaId }

        assertEquals(70_000L, porCuenta[cuentaNu]?.totalIngresosCentavos)
        assertEquals(21_900L, porCuenta[cuentaNu]?.totalEgresosCentavos)
        assertEquals(0L, porCuenta[cuentaEfectivo]?.totalIngresosCentavos)
        assertEquals(5_000L, porCuenta[cuentaEfectivo]?.totalEgresosCentavos)
    }

    @Test
    fun `observarRecientes respeta el limite y ordena de mas reciente a mas antiguo`() = runTest {
        dao.insertar(movimientoDePrueba(10_000L, TipoMovimiento.EGRESO).copy(fechaMovimiento = 100L))
        dao.insertar(movimientoDePrueba(20_000L, TipoMovimiento.EGRESO).copy(fechaMovimiento = 300L))
        dao.insertar(movimientoDePrueba(30_000L, TipoMovimiento.EGRESO).copy(fechaMovimiento = 200L))

        val recientes = dao.observarRecientes(limit = 2).first()

        assertEquals(listOf(20_000L, 30_000L), recientes.map { it.montoCentavos })
    }

    @Test
    fun `observarEnRangoLimit filtra por rango, ordena y limita`() = runTest {
        dao.insertar(movimientoDePrueba(10_000L, TipoMovimiento.EGRESO).copy(fechaMovimiento = 100L))
        dao.insertar(movimientoDePrueba(20_000L, TipoMovimiento.EGRESO).copy(fechaMovimiento = 400L))
        dao.insertar(movimientoDePrueba(30_000L, TipoMovimiento.EGRESO).copy(fechaMovimiento = 200L))
        dao.insertar(movimientoDePrueba(40_000L, TipoMovimiento.EGRESO).copy(fechaMovimiento = 9_000L)) // fuera de rango

        val enRango = dao.observarEnRangoLimit(desde = 0L, hasta = 1_000L, limit = 1).first()

        assertEquals(listOf(20_000L), enRango.map { it.montoCentavos })
    }

    private fun movimientoDePrueba(montoCentavos: Long, tipo: TipoMovimiento) = MovimientoEntity(
        montoCentavos = montoCentavos,
        tipo = tipo,
        origen = OrigenMovimiento.MANUAL,
        fechaMovimiento = 1_756_500_900_000L,
        fechaRegistro = 1_756_500_900_000L,
        estado = EstadoMovimiento.CONFIRMADO,
    )
}
