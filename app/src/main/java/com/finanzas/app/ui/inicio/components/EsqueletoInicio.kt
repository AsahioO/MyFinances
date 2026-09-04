package com.finanzas.app.ui.inicio.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.Motion
import com.finanzas.app.ui.theme.SurfaceLavanda

private const val AlphaMinima = 0.35f
private const val AlphaMaxima = 0.75f

private val AlturaHero = 232.dp
private val AlturaWallet = 140.dp
private const val FILAS_FANTASMA = 4

/**
 * Placeholders con la forma del contenido real mientras el primer emit de Room
 * llega: hero ElevatedCard, wallet y la card contenedora de recientes con sus
 * filas. Una sola InfiniteTransition para todos los bloques, y solo existe
 * mientras este composable esta en el arbol.
 */
@Composable
fun EsqueletoInicio(modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "esqueleto")
    val alfa by transicion.animateFloat(
        initialValue = AlphaMinima,
        targetValue = AlphaMaxima,
        animationSpec = infiniteRepeatable(
            animation = tween(Motion.PulsoEsqueletoMillis),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulso_esqueleto",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Cargando tus movimientos" },
        verticalArrangement = Arrangement.spacedBy(Dimens.EspacioL),
    ) {
        Bloque(alto = AlturaHero, alfa = alfa, modifier = Modifier.fillMaxWidth())
        Bloque(alto = AlturaWallet, alfa = alfa, modifier = Modifier.fillMaxWidth())
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(alfa)
                .clip(RoundedCornerShape(28.dp))
                .background(SurfaceLavanda)
                .padding(vertical = Dimens.EspacioS),
            verticalArrangement = Arrangement.spacedBy(Dimens.EspacioS),
        ) {
            repeat(FILAS_FANTASMA) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.EspacioM),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.EspacioS),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(SurfaceLavanda, CircleShape),
                    )
                    Bloque(alto = 16.dp, alfa = 1f, modifier = Modifier.weight(0.6f))
                }
            }
        }
    }
}

@Composable
private fun Bloque(alto: Dp, alfa: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(alto)
            .alpha(alfa)
            .background(SurfaceLavanda, RoundedCornerShape(28.dp)),
    )
}
