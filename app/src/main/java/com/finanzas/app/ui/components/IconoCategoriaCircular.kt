package com.finanzas.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Icono de categoria dentro de un circulo de fondo tintado. Reutilizable donde
 * sea que se represente una categoria en miniatura (fila de transaccion,
 * badge de esquina en Top Movers, listado de categorias).
 */
@Composable
fun IconoCategoriaCircular(
    icono: ImageVector,
    colorFondo: Color,
    colorIcono: Color,
    modifier: Modifier = Modifier,
    tamano: Dp = 40.dp,
    tamanoIcono: Dp = tamano / 2,
) {
    Box(
        modifier = modifier
            .size(tamano)
            .background(colorFondo, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = colorIcono,
            modifier = Modifier.size(tamanoIcono),
        )
    }
}
