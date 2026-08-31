package com.finanzas.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cuenta/wallet donde se agrupan movimientos (p. ej. "Efectivo", "Nu"). El saldo
 * mostrado en UI no se cachea aqui: es [saldoInicialCentavos] mas la suma
 * reactiva de sus movimientos, para no desincronizarse si se edita o borra uno.
 */
@Entity(
    tableName = "cuenta",
    indices = [Index("archivada")],
)
data class CuentaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String,
    /**
     * Reusa [OrigenMovimiento] en vez de un enum propio: es el mismo dato
     * ("de donde viene el dinero") y ya tiene su mapeo a color en
     * ColoresSemanticos, asi que duplicarlo obligaria a mantener dos switches
     * de color sincronizados.
     */
    val origen: OrigenMovimiento,
    /** Saldo capturado a mano por el usuario al crear la cuenta. */
    val saldoInicialCentavos: Long = 0L,
    /** Nombre del icono de Material, p. ej. "AccountBalanceWallet". */
    val icono: String = "AccountBalanceWallet",
    val archivada: Boolean = false,
    /** Orden de despliegue en listas horizontales de wallets. */
    val orden: Int = 0,
)
