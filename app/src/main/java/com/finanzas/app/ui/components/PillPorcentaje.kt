package com.finanzas.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanzas.app.ui.theme.FinanzasTheme
import com.finanzas.app.ui.theme.Ink

/**
 * Badge redondeado para una cifra de porcentaje (p. ej. "% del total del mes").
 * Sin semantica de tendencia (rojo/verde): el color de fondo es el que le pase
 * quien llama (normalmente el acento de una categoria), y el texto se resuelve
 * a blanco o [Ink] segun cual de los dos de mejor contraste sobre ese fondo.
 */
@Composable
fun PillPorcentaje(
    porcentaje: Int,
    colorFondo: Color,
    modifier: Modifier = Modifier,
) {
    val colorTexto = if (colorFondo.luminance() > 0.5f) Ink else Color.White
    Text(
        text = "$porcentaje%",
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(colorFondo)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = FinanzasTheme.monto.pequeno.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
        color = colorTexto,
    )
}
