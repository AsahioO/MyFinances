package com.finanzas.app.ui.theme

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

/**
 * Separa el entero de los centavos en dos estilos distintos dentro del mismo
 * string (ej. "$23,580." grande + "59" pequeno), como pide plan.md §8. Pura:
 * no depende de composicion, se puede probar con JUnit sin Robolectric.
 */
fun formatearMontoAnotado(
    centavos: Long,
    estiloEntero: SpanStyle,
    estiloCentavos: SpanStyle,
    locale: Locale = Locale.forLanguageTag("es-MX"),
): AnnotatedString {
    val signo = if (centavos < 0) "-" else ""
    val absCentavos = abs(centavos)
    val pesos = absCentavos / 100
    val resto = absCentavos % 100
    val formatoMiles = NumberFormat.getIntegerInstance(locale)
    return buildAnnotatedString {
        withStyle(estiloEntero) { append("$signo\$${formatoMiles.format(pesos)}.") }
        withStyle(estiloCentavos) { append(resto.toString().padStart(2, '0')) }
    }
}

/**
 * Version de un solo estilo de [formatearMontoAnotado]: para VisualTransformation
 * de un TextField, que solo acepta un string plano (no puede mezclar dos
 * TextStyle en un mismo campo editable).
 */
fun formatearMontoPlano(centavos: Long, locale: Locale = Locale.forLanguageTag("es-MX")): String {
    val signo = if (centavos < 0) "-" else ""
    val absCentavos = abs(centavos)
    val pesos = absCentavos / 100
    val resto = absCentavos % 100
    val formatoMiles = NumberFormat.getIntegerInstance(locale)
    return "$signo\$${formatoMiles.format(pesos)}.${resto.toString().padStart(2, '0')}"
}

@Composable
fun TextoMontoConCentavos(
    centavos: Long,
    estiloEntero: TextStyle,
    estiloCentavos: TextStyle,
    modifier: Modifier = Modifier,
) {
    Text(
        text = formatearMontoAnotado(
            centavos = centavos,
            estiloEntero = estiloEntero.toSpanStyle(),
            estiloCentavos = estiloCentavos.toSpanStyle(),
        ),
        modifier = modifier,
    )
}
