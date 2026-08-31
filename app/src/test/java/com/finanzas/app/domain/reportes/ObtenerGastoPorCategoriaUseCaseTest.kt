package com.finanzas.app.domain.reportes

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.finanzas.app.data.local.AppDatabase
import com.finanzas.app.data.local.entity.CategoriaEntity
import com.finanzas.app.data.local.entity.EstadoMovimiento
import com.finanzas.app.data.local.entity.MovimientoEntity
import com.finanzas.app.data.local.entity.OrigenMovimiento
import com.finanzas.app.data.local.entity.TipoMovimiento
import com.finanzas.app.data.repository.MovimientoRepository
import com.finanzas.app.data.repository.ReportesRepository
import com.finanzas.app.domain.model.RangoFechas
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ObtenerGastoPorCategoriaUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var useCase: ObtenerGastoPorCategoriaUseCase

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
            db = db,
        )
        useCase = ObtenerGastoPorCategoriaUseCase(
            ReportesRepository(db.movimientoDao()),
            repositorio,
        )
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    @Test
    fun `calcula la proporcion de cada categoria sobre el total del periodo`() = runTest {
        val comida = db.categoriaDao().insertar(CategoriaEntity(nombre = "Comida", icono = "Restaurant"))
        val salud = db.categoriaDao().insertar(CategoriaEntity(nombre = "Salud", icono = "LocalHospital"))

        db.movimientoDao().insertar(egreso(75_000L, comida, fecha = 100L))
        db.movimientoDao().insertar(egreso(25_000L, salud, fecha = 200L))

        val resultado = useCase(RangoFechas(desde = 0L, hasta = 1_000L)).first()

        assertEquals(2, resultado.size)
        val gastoComida = resultado.first { it.categoria?.id == comida }
        assertEquals(75_000L, gastoComida.montoCentavos)
        assertEquals(0.75f, gastoComida.proporcion, 0.001f)
    }

    @Test
    fun `un movimiento sin categoria se reporta con categoria null`() = runTest {
        db.movimientoDao().insertar(egreso(10_000L, categoriaId = null, fecha = 100L))

        val resultado = useCase(RangoFechas(desde = 0L, hasta = 1_000L)).first()

        assertEquals(1, resultado.size)
        assertEquals(null, resultado.first().categoria)
        assertEquals(1f, resultado.first().proporcion, 0.001f)
    }

    private fun egreso(montoCentavos: Long, categoriaId: Long?, fecha: Long) = MovimientoEntity(
        montoCentavos = montoCentavos,
        tipo = TipoMovimiento.EGRESO,
        origen = OrigenMovimiento.MANUAL,
        categoriaId = categoriaId,
        fechaMovimiento = fecha,
        fechaRegistro = fecha,
        estado = EstadoMovimiento.CONFIRMADO,
    )
}
