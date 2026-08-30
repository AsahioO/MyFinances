package com.finanzas.app.ui.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finanzas.app.ui.theme.FinanzasTheme

/**
 * Placeholder. Muestra el conteo real leido de Room para confirmar que la
 * cadena de datos llega hasta la UI.
 */
@Composable
fun InicioScreen(
    modifier: Modifier = Modifier,
    viewModel: InicioViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Inicio",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "${estado.totalMovimientos}",
            style = FinanzasTheme.monto.grande,
        )
        Text(
            text = "movimientos en la base de datos",
            style = MaterialTheme.typography.bodyMedium,
            color = FinanzasTheme.colores.textoSecundario,
        )
    }
}
