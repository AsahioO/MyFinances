package com.finanzas.app.ui.movimientos

import androidx.compose.ui.graphics.Color
import com.finanzas.app.data.local.entity.TipoMovimiento
import com.finanzas.app.ui.common.MovimientoUi
import org.junit.Assert.assertEquals
import org.junit.Test

/** JUnit puro: MovimientoUi solo transporta Color de Compose, que corre en la JVM plana. */
class MovimientosViewModelTest {

    @Test
    fun `offset cero reemplaza la lista y offset mayor acumula sin duplicar`() {
        val pagina1 = listOf(movimientoUi(1L), movimientoUi(2L))
        val pagina2 = listOf(movimientoUi(3L), movimientoUi(2L))

        assertEquals(listOf(1L, 2L), acumularPagina(emptyList(), pagina1, 0).map { it.id })
        assertEquals(listOf(1L, 2L, 3L), acumularPagina(pagina1, pagina2, 2).map { it.id })
    }

    private fun movimientoUi(id: Long) = MovimientoUi(
        id = id,
        comercioOrigen = "Comercio",
        categoriaNombre = null,
        montoCentavos = 100L,
        tipo = TipoMovimiento.EGRESO,
        colorOrigen = Color(0xFF000000),
        fechaMovimiento = 0L,
        pendienteRevision = false,
    )
}
