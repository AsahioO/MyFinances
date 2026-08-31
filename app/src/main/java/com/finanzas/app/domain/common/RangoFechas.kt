package com.finanzas.app.domain.common

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/** Rango cerrado [desde, hasta] en epoch millis. */
data class RangoFechas(val desde: Long, val hasta: Long)

/** Primer al ultimo milisegundo del mes calendario que contiene [ahora]. */
fun rangoMesActual(
    ahora: Long = System.currentTimeMillis(),
    zona: ZoneId = ZoneId.systemDefault(),
): RangoFechas {
    val fecha = ZonedDateTime.ofInstant(Instant.ofEpochMilli(ahora), zona)
    val inicioMes = fecha.toLocalDate().withDayOfMonth(1).atStartOfDay(zona)
    val inicioMesSiguiente = inicioMes.plusMonths(1)
    return RangoFechas(
        desde = inicioMes.toInstant().toEpochMilli(),
        hasta = inicioMesSiguiente.toInstant().toEpochMilli() - 1,
    )
}
