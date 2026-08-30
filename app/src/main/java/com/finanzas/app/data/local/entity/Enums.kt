package com.finanzas.app.data.local.entity

/** Direccion del dinero. */
enum class TipoMovimiento {
    INGRESO,
    EGRESO,
}

/**
 * De donde salio el movimiento. Tiene peso semantico en la UI:
 * [NU] y [SANTANDER] se pintan en violeta (detectado automatico),
 * [MANUAL] en mostaza (capturado a mano).
 */
enum class OrigenMovimiento {
    NU,
    SANTANDER,
    MANUAL,
}

/**
 * Lo detectado automaticamente entra como [PENDIENTE_REVISION] hasta que el
 * usuario lo valida; lo capturado a mano nace [CONFIRMADO].
 */
enum class EstadoMovimiento {
    PENDIENTE_REVISION,
    CONFIRMADO,
}
