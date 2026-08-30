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

    private fun movimientoDePrueba(montoCentavos: Long, tipo: TipoMovimiento) = MovimientoEntity(
        montoCentavos = montoCentavos,
        tipo = tipo,
        origen = OrigenMovimiento.MANUAL,
        fechaMovimiento = 1_756_500_900_000L,
        fechaRegistro = 1_756_500_900_000L,
        estado = EstadoMovimiento.CONFIRMADO,
    )
}
