package com.finanzas.app.domain.common

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime

class RangoFechasTest {

    private val utc = ZoneOffset.UTC

    @Test
    fun `rangoMesActual cubre desde el primer milisegundo hasta el ultimo del mes`() {
        val mitadDeAgosto = ZonedDateTime.of(2026, 8, 15, 12, 0, 0, 0, utc)
            .toInstant().toEpochMilli()

        val rango = rangoMesActual(ahora = mitadDeAgosto, zona = utc)

        val inicioEsperado = ZonedDateTime.of(2026, 8, 1, 0, 0, 0, 0, utc)
            .toInstant().toEpochMilli()
        val finEsperado = ZonedDateTime.of(2026, 9, 1, 0, 0, 0, 0, utc)
            .toInstant().toEpochMilli() - 1

        assertEquals(inicioEsperado, rango.desde)
        assertEquals(finEsperado, rango.hasta)
    }

    @Test
    fun `rangoMesActual en el primer dia del mes arranca en ese mismo dia`() {
        val primerDia = ZonedDateTime.of(2026, 2, 1, 0, 0, 0, 0, utc)
            .toInstant().toEpochMilli()

        val rango = rangoMesActual(ahora = primerDia, zona = utc)

        assertEquals(primerDia, rango.desde)
    }

    @Test
    fun `rangoMesActual respeta un anio bisiesto en febrero`() {
        val finDeFebreroBisiesto = ZonedDateTime.of(2028, 2, 29, 23, 59, 0, 0, utc)
            .toInstant().toEpochMilli()

        val rango = rangoMesActual(ahora = finDeFebreroBisiesto, zona = utc)

        val finEsperado = ZonedDateTime.of(2028, 3, 1, 0, 0, 0, 0, utc)
            .toInstant().toEpochMilli() - 1
        assertEquals(finEsperado, rango.hasta)
    }
}
