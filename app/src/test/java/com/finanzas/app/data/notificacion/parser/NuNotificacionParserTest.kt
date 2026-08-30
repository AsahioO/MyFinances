package com.finanzas.app.data.notificacion.parser

import com.finanzas.app.data.local.entity.TipoMovimiento
import com.finanzas.app.domain.notificacion.ResultadoParseoNotificacion
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifica el parseo puro contra los formatos reales de Nu capturados en
 * plan.md. No necesita Robolectric: es una funcion pura sobre texto.
 */
class NuNotificacionParserTest {

    private val parser = NuNotificacionParser()

    @Test
    fun `reconoce una transferencia recibida`() {
        val resultado = parser.parsear(
            titulo = "¡Recibiste una transferencia!",
            texto = "Recibiste $700.00 en tu Cuenta Nu.",
            cuando = 1_756_500_000_000L,
        )

        assertTrue(resultado is ResultadoParseoNotificacion.Exitoso)
        val datos = (resultado as ResultadoParseoNotificacion.Exitoso).datos
        assertEquals(70_000L, datos.montoCentavos)
        assertEquals(TipoMovimiento.INGRESO, datos.tipo)
        assertNull(datos.comercioOrigen)
    }

    @Test
    fun `reconoce una compra aprobada y extrae comercio, monto y fecha`() {
        val resultado = parser.parsear(
            titulo = "Compra aprobada por $219.00",
            texto = "Compraste en PAYPAL *NVIDIA CORP con tu tarjeta de Cuenta Nu por $219.00 el 29/08/2026 13:35.",
            cuando = 1_756_500_000_000L,
        )

        assertTrue(resultado is ResultadoParseoNotificacion.Exitoso)
        val datos = (resultado as ResultadoParseoNotificacion.Exitoso).datos
        assertEquals(21_900L, datos.montoCentavos)
        assertEquals(TipoMovimiento.EGRESO, datos.tipo)
        assertEquals("PAYPAL *NVIDIA CORP", datos.comercioOrigen)

        val fechaEsperada = LocalDateTime.of(2026, 8, 29, 13, 35)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        assertEquals(fechaEsperada, datos.fechaMovimiento)
    }

    @Test
    fun `texto no reconocido no genera resultado`() {
        val resultado = parser.parsear(
            titulo = "Alguna otra notificacion",
            texto = "Esto no tiene el formato esperado.",
            cuando = 1_756_500_000_000L,
        )

        assertEquals(ResultadoParseoNotificacion.NoReconocido, resultado)
    }

    @Test
    fun `texto nulo no genera resultado`() {
        val resultado = parser.parsear(titulo = null, texto = null, cuando = 1_756_500_000_000L)

        assertEquals(ResultadoParseoNotificacion.NoReconocido, resultado)
    }
}
