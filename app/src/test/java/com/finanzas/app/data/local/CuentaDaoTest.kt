package com.finanzas.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.finanzas.app.data.local.dao.CuentaDao
import com.finanzas.app.data.local.entity.CuentaEntity
import com.finanzas.app.data.local.entity.OrigenMovimiento
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CuentaDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: CuentaDao

    @Before
    fun crearBase() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        )
            .addCallback(AppDatabase.SEED_CALLBACK)
            .allowMainThreadQueries()
            .build()
        dao = db.cuentaDao()
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    @Test
    fun `la semilla deja Efectivo y Nu activas al crear la base`() = runTest {
        val activas = dao.observarActivas().first()

        assertEquals(2, activas.size)
        assertEquals(setOf("Efectivo", "Nu"), activas.map { it.nombre }.toSet())
    }

    @Test
    fun `obtenerPorOrigen encuentra la cuenta activa de ese origen`() = runTest {
        val cuenta = dao.obtenerPorOrigen(OrigenMovimiento.NU)

        assertEquals("Nu", cuenta?.nombre)
    }

    @Test
    fun `obtenerPorOrigen no encuentra cuenta para un origen sin cuenta creada`() = runTest {
        val cuenta = dao.obtenerPorOrigen(OrigenMovimiento.SANTANDER)

        assertNull(cuenta)
    }

    @Test
    fun `una cuenta archivada no aparece en observarActivas ni en obtenerPorOrigen`() = runTest {
        val efectivo = dao.observarActivas().first().first { it.nombre == "Efectivo" }
        dao.actualizar(efectivo.copy(archivada = true))

        assertEquals(1, dao.observarActivas().first().size)
        assertNull(dao.obtenerPorOrigen(OrigenMovimiento.MANUAL))
    }

    @Test
    fun `insertar una cuenta nueva conserva todos los campos`() = runTest {
        val nueva = CuentaEntity(
            nombre = "Ahorros",
            origen = OrigenMovimiento.MANUAL,
            saldoInicialCentavos = 100_000L,
            icono = "Savings",
            orden = 5,
        )

        val id = dao.insertar(nueva)
        val leida = dao.obtenerPorId(id)

        assertEquals(nueva.copy(id = id), leida)
    }
}
