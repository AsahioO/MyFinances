package com.finanzas.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categoria")
data class CategoriaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    /** Nombre visible, p. ej. "Comida". */
    val nombre: String,
    /** Nombre del icono de Material, p. ej. "Restaurant". */
    val icono: String,
    /** Color hex opcional (#RRGGBB) usado en Reportes. */
    val color: String? = null,
)
