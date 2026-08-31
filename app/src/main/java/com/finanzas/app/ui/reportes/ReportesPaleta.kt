package com.finanzas.app.ui.reportes

import androidx.compose.ui.graphics.Color
import com.finanzas.app.ui.theme.ColoresSemanticos

/**
 * Color determinista por categoria para el donut: la misma categoria siempre
 * pinta igual, aunque su posicion cambie entre meses. Reusa tokens ya
 * definidos, no agrega paleta nueva.
 */
object ReportesPaleta {

    private val colores = ColoresSemanticos()
    private val paleta = listOf(
        colores.origenAutomatico,
        colores.origenManual,
        colores.ingreso,
        colores.egreso,
        colores.textoSecundario,
    )

    fun colorPara(categoriaId: Long?): Color =
        categoriaId?.let { paleta[(it % paleta.size).toInt()] } ?: colores.textoSecundario
}
