package com.finanzas.app.domain.model

/**
 * Convierte un monto en texto ("219.00", "1,234.56") a centavos. Nunca Double:
 * separa pesos y centavos en el punto decimal para no arrastrar errores de
 * punto flotante. Usado por todos los parsers de texto->dinero (bancos, OCR,
 * CSV) y por el formulario de alta manual, para no repetir la logica.
 */
fun montoTextoACentavos(monto: String): Long? {
    val partes = monto.replace(",", "").split(".")
    if (partes.size != 2) return null
    val pesos = partes[0].toLongOrNull() ?: return null
    val centavos = partes[1].padEnd(2, '0').take(2).toLongOrNull() ?: return null
    return pesos * 100 + centavos
}
