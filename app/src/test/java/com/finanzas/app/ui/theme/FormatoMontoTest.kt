package com.finanzas.app.ui.theme

import androidx.compose.ui.text.SpanStyle
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

/** JUnit puro: NumberFormat corre en la JVM plana, sin Robolectric. */
class FormatoMontoTest {

    private val estiloEntero = SpanStyle()
    private val estiloCentavos = SpanStyle()
    private val localeMx: Locale = Locale.forLanguageTag("es-MX")

    @Test
    fun `separa miles con coma y centavos con dos digitos`() {
        val resultado = formatearMontoAnotado(2_358_059L, estiloEntero, estiloCentavos, localeMx)

        assertEquals("$23,580.59", resultado.text)
    }

    @Test
    fun `un monto negativo antepone el signo antes del simbolo de pesos`() {
        val resultado = formatearMontoAnotado(-21_900L, estiloEntero, estiloCentavos, localeMx)

        assertEquals("-$219.00", resultado.text)
    }

    @Test
    fun `centavos de un solo digito se rellenan con cero a la izquierda`() {
        val resultado = formatearMontoAnotado(105L, estiloEntero, estiloCentavos, localeMx)

        assertEquals("$1.05", resultado.text)
    }

    @Test
    fun `cero centavos se muestra como punto00`() {
        val resultado = formatearMontoAnotado(0L, estiloEntero, estiloCentavos, localeMx)

        assertEquals("$0.00", resultado.text)
    }
}
