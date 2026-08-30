package com.finanzas.app.domain.notificacion

import com.finanzas.app.data.local.entity.OrigenMovimiento

/**
 * Contrato que implementa cada banco soportado. Agregar Santander a futuro es
 * una clase nueva que implemente esta interfaz mas una linea en el modulo de
 * Hilt — no se toca el servicio ni los parsers existentes.
 */
interface ParserNotificacionBanco {
    val packageName: String
    val origen: OrigenMovimiento

    fun parsear(titulo: String?, texto: String?, cuando: Long): ResultadoParseoNotificacion
}

sealed interface ResultadoParseoNotificacion {
    data class Exitoso(val datos: NotificacionParseada) : ResultadoParseoNotificacion
    data object NoReconocido : ResultadoParseoNotificacion
}
