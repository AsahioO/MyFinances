package com.finanzas.app.data.notificacion.parser

import com.finanzas.app.data.local.entity.OrigenMovimiento
import com.finanzas.app.data.local.entity.TipoMovimiento
import com.finanzas.app.domain.notificacion.NotificacionParseada
import com.finanzas.app.domain.notificacion.ParserNotificacionBanco
import com.finanzas.app.domain.notificacion.ResultadoParseoNotificacion
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import javax.inject.Inject

/**
 * Parser de Nu (`com.nu.production`). Reconoce los dos formatos reales
 * confirmados en plan.md: transferencia recibida y compra aprobada. El texto
 * de compra ya trae comercio, monto y fecha/hora separados por frases fijas
 * ("Compraste en", "con tu tarjeta de Cuenta Nu por", "el"), asi que se corta
 * en esos puntos en vez de inventar delimitadores.
 */
class NuNotificacionParser @Inject constructor() : ParserNotificacionBanco {

    override val packageName = "com.nu.production"
    override val origen = OrigenMovimiento.NU

    override fun parsear(titulo: String?, texto: String?, cuando: Long): ResultadoParseoNotificacion {
        if (texto == null) return ResultadoParseoNotificacion.NoReconocido
        val parseado = parsearIngreso(texto, cuando) ?: parsearEgreso(texto, cuando)
        return parseado?.let { ResultadoParseoNotificacion.Exitoso(it) }
            ?: ResultadoParseoNotificacion.NoReconocido
    }

    private fun parsearIngreso(texto: String, cuando: Long): NotificacionParseada? {
        val match = REGEX_INGRESO.find(texto) ?: return null
        val montoCentavos = montoACentavos(match.groupValues[1]) ?: return null
        return NotificacionParseada(
            montoCentavos = montoCentavos,
            tipo = TipoMovimiento.INGRESO,
            comercioOrigen = null,
            fechaMovimiento = cuando,
        )
    }

    private fun parsearEgreso(texto: String, cuando: Long): NotificacionParseada? {
        val match = REGEX_EGRESO.find(texto) ?: return null
        val (comercio, montoTexto, fechaTexto) = match.destructured
        val montoCentavos = montoACentavos(montoTexto) ?: return null
        return NotificacionParseada(
            montoCentavos = montoCentavos,
            tipo = TipoMovimiento.EGRESO,
            comercioOrigen = comercio.trim(),
            fechaMovimiento = parsearFecha(fechaTexto) ?: cuando,
        )
    }

    /** Nunca Double: separa pesos y centavos en el punto decimal. */
    private fun montoACentavos(monto: String): Long? {
        val partes = monto.replace(",", "").split(".")
        if (partes.size != 2) return null
        val pesos = partes[0].toLongOrNull() ?: return null
        val centavos = partes[1].padEnd(2, '0').take(2).toLongOrNull() ?: return null
        return pesos * 100 + centavos
    }

    private fun parsearFecha(fecha: String): Long? = try {
        LocalDateTime.parse(fecha, FORMATO_FECHA)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    } catch (e: DateTimeParseException) {
        null
    }

    private companion object {
        val REGEX_INGRESO = Regex("""Recibiste \$([\d,]+\.\d{2}) en tu Cuenta Nu\.""")
        val REGEX_EGRESO = Regex(
            """Compraste en (.+?) con tu tarjeta de Cuenta Nu por \$([\d,]+\.\d{2}) el (\d{2}/\d{2}/\d{4} \d{2}:\d{2})\."""
        )
        val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ROOT)
    }
}
