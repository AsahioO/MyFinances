package com.finanzas.app.ui.theme

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val LOCALE_ES_MX = Locale.forLanguageTag("es-MX")
private val FORMATO_FECHA_CORTA = DateTimeFormatter.ofPattern("dd MMM", LOCALE_ES_MX)
private val ZONA_SISTEMA = ZoneId.systemDefault()

/** "26 ene": fecha corta para subtitulos de fila (movimientos, top movers). */
fun formatearFechaCorta(millis: Long, locale: Locale = LOCALE_ES_MX): String {
    val formato = if (locale === LOCALE_ES_MX || locale == LOCALE_ES_MX) FORMATO_FECHA_CORTA else DateTimeFormatter.ofPattern("dd MMM", locale)
    val zona = if (locale === LOCALE_ES_MX || locale == LOCALE_ES_MX) ZONA_SISTEMA else ZoneId.systemDefault()
    return Instant.ofEpochMilli(millis).atZone(zona).format(formato)
}

/**
 * "Septiembre": mes calendario en curso, capitalizado, para el titulo de
 * Inicio. Debe coincidir con el mes que resume el hero, que sale de
 * rangoMesActual() en ObtenerFlujoDelMesUseCase — de ahi que se lea el reloj
 * del sistema aqui y no se reciba como parametro.
 */
fun nombreMesActual(locale: Locale = LOCALE_ES_MX): String {
    val mes = YearMonth.now(ZoneId.systemDefault())
        .month
        .getDisplayName(TextStyle.FULL_STANDALONE, locale)
    return mes.replaceFirstChar { it.titlecase(locale) }
}
