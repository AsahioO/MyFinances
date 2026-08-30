package com.finanzas.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Bitacora de deduplicacion: guarda el `sbn.key` de cada notificacion ya
 * procesada, para que una notificacion actualizada por el sistema no vuelva a
 * generar un movimiento.
 */
@Entity(tableName = "notificacion_procesada")
data class NotificacionProcesadaEntity(
    @PrimaryKey
    val key: String,
    val packageName: String,
    val fechaProcesado: Long,
    /** Movimiento generado, si la notificacion si resulto ser un movimiento. */
    val movimientoId: Long? = null,
)
