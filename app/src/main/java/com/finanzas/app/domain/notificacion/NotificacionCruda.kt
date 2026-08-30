package com.finanzas.app.domain.notificacion

/** Datos crudos capturados en `onNotificationPosted()`, sin parsear todavia. */
data class NotificacionCruda(
    val key: String,
    val packageName: String,
    val titulo: String?,
    val texto: String?,
    /** `sbn.postTime`: fallback de fecha si el parser no logra extraer una. */
    val cuando: Long,
)
