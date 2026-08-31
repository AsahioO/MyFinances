package com.finanzas.app.domain.cuenta

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.finanzas.app.data.local.AppDatabase
import com.finanzas.app.data.local.entity.EstadoMovimiento
import com.finanzas.app.data.local.entity.MovimientoEntity
import com.finanzas.app.data.local.entity.OrigenMovimiento
import com.finanzas.app.data.local.entity.TipoMovimiento
import com.finanzas.app.data.repository.MovimientoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ObtenerSaldosCuentasUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var repositorio: MovimientoRepository
    private lateinit var useCase: ObtenerSaldosCuentasUseCase

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
        useCase = ObtenerSaldosCuentasUseCase(repositorio)
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    @Test
    fun `sin movimientos el saldo de cada cuenta es su saldo inicial`() = runTest {
        val saldos = useCase().first()

        assertEquals(2, saldos.size)
        assertEquals(setOf(0L), saldos.map { it.saldoCentavos }.toSet())
    }

    @Test
    fun `el saldo suma ingresos y resta egresos sobre el saldo inicial`() = runTest {
        val cuentaNu = db.cuentaDao().obtenerPorOrigen(OrigenMovimiento.NU)!!

        db.movimientoDao().insertar(movimiento(70_000L, TipoMovimiento.INGRESO, cuentaNu.id))
        db.movimientoDao().insertar(movimiento(21_900L, TipoMovimiento.EGRESO, cuentaNu.id))

        val saldoNu = useCase().first().first { it.cuenta.id == cuentaNu.id }

        assertEquals(48_100L, saldoNu.saldoCentavos)
    }

    private fun movimiento(montoCentavos: Long, tipo: TipoMovimiento, cuentaId: Long) = MovimientoEntity(
        montoCentavos = montoCentavos,
        tipo = tipo,
        origen = OrigenMovimiento.NU,
        cuentaId = cuentaId,
        fechaMovimiento = 1_756_500_900_000L,
        fechaRegistro = 1_756_500_900_000L,
        estado = EstadoMovimiento.CONFIRMADO,
    )
}
