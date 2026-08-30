package com.finanzas.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Lista blanca de apps bancarias cuyas notificaciones se leen.
 * Agregar un banco nuevo es insertar una fila, no tocar codigo.
 */
@Entity(tableName = "banco_config")
data class BancoConfigEntity(
    @PrimaryKey
    val packageName: String,
    val nombreDisplay: String,
    val activo: Boolean,
)
