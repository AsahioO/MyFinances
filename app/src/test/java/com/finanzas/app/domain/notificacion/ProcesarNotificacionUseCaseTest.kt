package com.finanzas.app.domain.notificacion

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.finanzas.app.data.local.AppDatabase
import com.finanzas.app.data.local.entity.EstadoMovimiento
import com.finanzas.app.data.local.entity.OrigenMovimiento
import com.finanzas.app.data.notificacion.parser.NuNotificacionParser
import com.finanzas.app.data.repository.MovimientoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Verifica la orquestacion de negocio: dedupe, respeto del toggle de bancos
 * activos, y persistencia como PENDIENTE_REVISION. Mismo patron de BD en
 * memoria que MovimientoDaoTest.kt.
 */
@RunWith(RobolectricTestRunner::class)
class ProcesarNotificacionUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var useCase: ProcesarNotificacionUseCase

    @Before
    fun crearBase() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        )
            .addCallback(AppDatabase.SEED_CALLBACK)
            .allowMainThreadQueries()
            .build()
        val repositorio = MovimientoRepository(
            movimientoDao = db.movimientoDao(),
            categoriaDao = db.categoriaDao(),
            notificacionProcesadaDao = db.notificacionProcesadaDao(),
            bancoConfigDao = db.bancoConfigDao(),
            cuentaDao = db.cuentaDao(),
            db = db,
        )
        useCase = ProcesarNotificacionUseCase(listOf(NuNotificacionParser()), repositorio)
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    private fun notificacionDeIngreso(key: String = "key-ingreso") = NotificacionCruda(
        key = key,
        packageName = "com.nu.production",
        titulo = "¡Recibiste una transferencia!",
        texto = "Recibiste $700.00 en tu Cuenta Nu.",
        cuando = 1_756_500_000_000L,
    )

    @Test
    fun `procesa una notificacion reconocida y la deja pendiente de revision`() = runTest {
        useCase(notificacionDeIngreso())

        val movimientos = db.movimientoDao().observarTodos().first()
        assertEquals(1, movimientos.size)
        assertEquals(EstadoMovimiento.PENDIENTE_REVISION, movimientos.first().estado)
        assertEquals(OrigenMovimiento.NU, movimientos.first().origen)
        assertTrue(db.notificacionProcesadaDao().yaProcesada("key-ingreso"))

        val cuentaNu = db.cuentaDao().obtenerPorOrigen(OrigenMovimiento.NU)
        assertEquals(cuentaNu?.id, movimientos.first().cuentaId)
    }

    @Test
    fun `no duplica el movimiento si la misma notificacion llega dos veces`() = runTest {
        val notificacion = notificacionDeIngreso()

        useCase(notificacion)
        useCase(notificacion)

        assertEquals(1, db.movimientoDao().observarTodos().first().size)
    }

    @Test
    fun `no procesa notificaciones de un banco desactivado`() = runTest {
        db.bancoConfigDao().cambiarActivo("com.nu.production", false)

        useCase(notificacionDeIngreso())

        assertEquals(0, db.movimientoDao().observarTodos().first().size)
    }

    @Test
    fun `texto no reconocido no inserta movimiento ni marca de deduplicacion`() = runTest {
        val notificacion = NotificacionCruda(
            key = "key-rara",
            packageName = "com.nu.production",
            titulo = "Otra cosa",
            texto = "Esto no tiene el formato esperado.",
            cuando = 1_756_500_000_000L,
        )

        useCase(notificacion)

        assertEquals(0, db.movimientoDao().observarTodos().first().size)
        assertFalse(db.notificacionProcesadaDao().yaProcesada("key-rara"))
    }
}
