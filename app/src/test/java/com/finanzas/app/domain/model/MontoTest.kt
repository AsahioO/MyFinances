package com.finanzas.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Extraida de NuNotificacionParser (antes montoACentavos privado): la usan
 * todos los parsers de texto->dinero (bancos, OCR, CSV) y el formulario de
 * alta manual.
 */
class MontoTest {

    @Test
    fun `convierte un monto simple a centavos`() {
        assertEquals(21_900L, montoTextoACentavos("219.00"))
    }

    @Test
    fun `ignora comas de miles`() {
        assertEquals(123_456L, montoTextoACentavos("1,234.56"))
    }

    @Test
    fun `completa centavos faltantes con cero`() {
        assertEquals(70_000L, montoTextoACentavos("700.0"))
        assertEquals(70_000L, montoTextoACentavos("700."))
    }

    @Test
    fun `sin punto decimal devuelve null`() {
        assertNull(montoTextoACentavos("219"))
    }

    @Test
    fun `texto no numerico devuelve null`() {
        assertNull(montoTextoACentavos("abc.de"))
    }

    @Test
    fun `monto cero es valido`() {
        assertEquals(0L, montoTextoACentavos("0.00"))
    }
}
