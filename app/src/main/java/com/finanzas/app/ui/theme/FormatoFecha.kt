package com.finanzas.app.ui.theme

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
