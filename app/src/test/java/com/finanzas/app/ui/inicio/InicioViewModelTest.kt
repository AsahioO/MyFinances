package com.finanzas.app.ui.inicio

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.finanzas.app.data.local.AppDatabase
import com.finanzas.app.data.local.entity.EstadoMovimiento
import com.finanzas.app.data.local.entity.MovimientoEntity
import com.finanzas.app.data.local.entity.OrigenMovimiento
import com.finanzas.app.data.local.entity.TipoMovimiento
import com.finanzas.app.data.repository.CuentaRepository
import com.finanzas.app.data.repository.MovimientoRepository
import com.finanzas.app.data.repository.ReportesRepository
import com.finanzas.app.domain.cuenta.ObtenerSaldosCuentasUseCase
import com.finanzas.app.domain.reportes.ObtenerFlujoDelMesUseCase
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

/**
 * Test de integracion liviano: MovimientoRepository real + DB Room en
 * memoria (no un fake), porque el repositorio es una clase concreta, no una
 * interfaz. Mismo patron de BD que MovimientoDaoTest.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class InicioViewModelTest {

    private val dispatcherDePrueba = UnconfinedTestDispatcher()
    private lateinit var db: AppDatabase
    private lateinit var viewModel: InicioViewModel

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
            db = db,
        )
        val cuentas = CuentaRepository(db.cuentaDao())
        val reportes = ReportesRepository(db.movimientoDao())
        viewModel = InicioViewModel(
            repositorio = repositorio,
            obtenerFlujoDelMes = ObtenerFlujoDelMesUseCase(reportes),
            obtenerSaldosCuentas = ObtenerSaldosCuentasUseCase(cuentas, reportes),
        )
    }

    @After
    fun cerrarBase() {
        Dispatchers.resetMain()
        db.close()
    }

    @Test
    fun `expone flujo del mes, saldos de cuentas y movimientos recientes`() = runTest {
        val cuentaNu = db.cuentaDao().obtenerPorOrigen(OrigenMovimiento.NU)!!
        val ahora = System.currentTimeMillis()
        db.movimientoDao().insertar(
            MovimientoEntity(
                montoCentavos = 70_000L,
                tipo = TipoMovimiento.INGRESO,
                origen = OrigenMovimiento.NU,
                cuentaId = cuentaNu.id,
                fechaMovimiento = ahora,
                fechaRegistro = ahora,
                estado = EstadoMovimiento.CONFIRMADO,
            ),
        )
        db.movimientoDao().insertar(
            MovimientoEntity(
                montoCentavos = 5_000L,
                tipo = TipoMovimiento.EGRESO,
                origen = OrigenMovimiento.NU,
                cuentaId = cuentaNu.id,
                fechaMovimiento = ahora,
                fechaRegistro = ahora,
                estado = EstadoMovimiento.PENDIENTE_REVISION,
            ),
        )

        val estado = viewModel.estado.first { !it.cargando }

        assertEquals(70_000L, estado.flujoMes.ingresosCentavos)
        assertEquals(2, estado.saldosCuentas.size)
        assertEquals(2, estado.movimientosRecientes.size)
        assertEquals(1, estado.movimientosPendientes)
    }
}
