package com.finanzas.app.data.local

import androidx.room.TypeConverter
import com.finanzas.app.data.local.entity.EstadoMovimiento
import com.finanzas.app.data.local.entity.OrigenMovimiento
import com.finanzas.app.data.local.entity.TipoMovimiento

/**
 * Los enums se guardan por nombre y no por ordinal: reordenar un enum no debe
 * corromper filas ya escritas.
 */
class Convertidores {

    @TypeConverter
    fun tipoAString(valor: TipoMovimiento): String = valor.name

    @TypeConverter
    fun stringATipo(valor: String): TipoMovimiento = TipoMovimiento.valueOf(valor)

    @TypeConverter
    fun origenAString(valor: OrigenMovimiento): String = valor.name

    @TypeConverter
    fun stringAOrigen(valor: String): OrigenMovimiento = OrigenMovimiento.valueOf(valor)

    @TypeConverter
    fun estadoAString(valor: EstadoMovimiento): String = valor.name

    @TypeConverter
    fun stringAEstado(valor: String): EstadoMovimiento = EstadoMovimiento.valueOf(valor)
}
