package com.finanzas.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla central. El dinero SIEMPRE se guarda en centavos como [Long]:
 * nunca Double ni Float, para no arrastrar errores de punto flotante.
 */
@Entity(
    tableName = "movimiento",
    foreignKeys = [
        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            // Borrar una categoria no borra historial financiero, solo lo descategoriza.
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("fechaMovimiento"),
        Index("categoriaId"),
        Index("estado"),
    ],
)
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    /** Monto en centavos. 219.00 se guarda como 21900. */
    val montoCentavos: Long,
    val tipo: TipoMovimiento,
    val origen: OrigenMovimiento,
    /** Comercio o persona extraida del parseo de la notificacion. */
    val comercioOrigen: String? = null,
    val categoriaId: Long? = null,
    /** Fecha real del movimiento, epoch millis. */
    val fechaMovimiento: Long,
    /** Cuando se inserto la fila, epoch millis. Sirve para depurar duplicados y retrasos. */
    val fechaRegistro: Long,
    val estado: EstadoMovimiento,
    val notas: String? = null,
)
