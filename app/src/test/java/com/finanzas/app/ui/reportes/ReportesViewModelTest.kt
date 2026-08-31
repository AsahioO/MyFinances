package com.finanzas.app.ui.reportes

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.finanzas.app.data.local.AppDatabase
import com.finanzas.app.data.local.entity.CategoriaEntity
import com.finanzas.app.data.local.entity.EstadoMovimiento
import com.finanzas.app.data.local.entity.MovimientoEntity
import com.finanzas.app.data.local.entity.OrigenMovimiento
import com.finanzas.app.data.local.entity.TipoMovimiento
import com.finanzas.app.data.repository.MovimientoRepository
import com.finanzas.app.domain.reportes.ObtenerGastoPorCategoriaUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ReportesViewModelTest {

    private val dispatcherDePrueba = UnconfinedTestDispatcher()
    private lateinit var db: AppDatabase
    private lateinit var viewModel: ReportesViewModel

    @Before
    fun crearBase() {
        Dispatchers.setMain(dispatcherDePrueba)
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
        viewModel = ReportesViewModel(
            repositorio = repositorio,
            obtenerGastoPorCategoria = ObtenerGastoPorCategoriaUseCase(repositorio),
        )
    }

    @After
    fun cerrarBase() {
        Dispatchers.resetMain()
        db.close()
    }

    @Test
    fun `expone el total gastado, el donut y los top movers del mes`() = runTest {
        val comida = db.categoriaDao().insertar(CategoriaEntity(nombre = "Comida", icono = "Restaurant"))
        val ahora = System.currentTimeMillis()
        db.movimientoDao().insertar(
            MovimientoEntity(
                montoCentavos = 15_000L,
                tipo = TipoMovimiento.EGRESO,
                origen = OrigenMovimiento.MANUAL,
                categoriaId = comida,
                fechaMovimiento = ahora,
                fechaRegistro = ahora,
                estado = EstadoMovimiento.CONFIRMADO,
            ),
        )

        val estado = viewModel.estado.first { !it.cargando }

        assertEquals(15_000L, estado.totalGastadoCentavos)
        assertEquals(1, estado.segmentosDonut.size)
        assertEquals(1, estado.topMovers.size)
        assertEquals("Comida", estado.topMovers.first().categoria?.nombre)
        assertEquals(1, estado.transaccionesRecientes.size)
    }
}
