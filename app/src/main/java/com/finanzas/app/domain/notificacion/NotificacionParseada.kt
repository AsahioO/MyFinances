package com.finanzas.app.domain.notificacion

import com.finanzas.app.data.local.entity.TipoMovimiento

/** Resultado estructurado de extraer los datos financieros de una notificacion. */
data class NotificacionParseada(
    val montoCentavos: Long,
    val tipo: TipoMovimiento,
    val comercioOrigen: String?,
    val fechaMovimiento: Long,
)
