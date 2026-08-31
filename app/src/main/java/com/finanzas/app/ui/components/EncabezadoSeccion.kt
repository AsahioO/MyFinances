package com.finanzas.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import com.finanzas.app.ui.theme.FamiliaTitulos
import com.finanzas.app.ui.theme.FinanzasTheme
import java.util.Locale

private val LOCALE_ES_MX_SECCION = Locale.forLanguageTag("es-MX")

/**
 * Titulo de seccion en mayusculas con tracking amplio (encabezado tipo
 * "TOP MOVERS"), y opcionalmente una accion "Ver todas" alineada a la derecha.
 * Reutilizable: mismo tratamiento visual en cualquier lista de la app.
 */
@Composable
fun EncabezadoSeccion(
    titulo: String,
    modifier: Modifier = Modifier,
    textoAccion: String? = null,
    onAccion: (() -> Unit)? = null,
) {
    val tituloMayus = remember(titulo) { titulo.uppercase(LOCALE_ES_MX_SECCION) }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = tituloMayus,
            fontFamily = FamiliaTitulos,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            letterSpacing = 1.5.sp,
            color = FinanzasTheme.colores.textoSecundario,
        )
        if (textoAccion != null && onAccion != null) {
            TextButton(onClick = onAccion) {
                Text(text = textoAccion)
            }
        }
    }
}
