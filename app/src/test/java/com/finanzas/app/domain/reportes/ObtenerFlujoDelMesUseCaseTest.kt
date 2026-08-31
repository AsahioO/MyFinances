package com.finanzas.app.domain.reportes

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.finanzas.app.data.local.AppDatabase
import com.finanzas.app.data.local.entity.EstadoMovimiento
import com.finanzas.app.data.local.entity.MovimientoEntity
import com.finanzas.app.data.local.entity.OrigenMovimiento
import com.finanzas.app.data.local.entity.TipoMovimiento
import com.finanzas.app.data.repository.MovimientoRepository
import com.finanzas.app.domain.common.RangoFechas
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ObtenerFlujoDelMesUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var repositorio: MovimientoRepository
    private lateinit var useCase: ObtenerFlujoDelMesUseCase

    @Before
    fun crearBase() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        )
            .addCallback(AppDatabase.SEED_CALLBACK)
            .allowMainThreadQueries()
            .build()
        repositorio = MovimientoRepository(
            movimientoDao = db.movimientoDao(),
            categoriaDao = db.categoriaDao(),
            notificacionProcesadaDao = db.notificacionProcesadaDao(),
            bancoConfigDao = db.bancoConfigDao(),
            cuentaDao = db.cuentaDao(),
            db = db,
        )
        useCase = ObtenerFlujoDelMesUseCase(repositorio)
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    @Test
    fun `netoCentavos es ingresos menos egresos del rango dado`() = runTest {
        db.movimientoDao().insertar(movimiento(70_000L, TipoMovimiento.INGRESO, fecha = 100L))
        db.movimientoDao().insertar(movimiento(21_900L, TipoMovimiento.EGRESO, fecha = 200L))
        db.movimientoDao().insertar(movimiento(5_000L, TipoMovimiento.EGRESO, fecha = 9_000L))

        val flujo = useCase(RangoFechas(desde = 0L, hasta = 1_000L)).first()

        assertEquals(70_000L, flujo.ingresosCentavos)
        assertEquals(21_900L, flujo.egresosCentavos)
        assertEquals(48_100L, flujo.netoCentavos)
    }

    private fun movimiento(montoCentavos: Long, tipo: TipoMovimiento, fecha: Long) = MovimientoEntity(
        montoCentavos = montoCentavos,
        tipo = tipo,
        origen = OrigenMovimiento.MANUAL,
        fechaMovimiento = fecha,
        fechaRegistro = fecha,
        estado = EstadoMovimiento.CONFIRMADO,
    )
}
