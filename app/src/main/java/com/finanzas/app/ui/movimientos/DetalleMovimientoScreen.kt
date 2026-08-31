package com.finanzas.app.ui.movimientos

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finanzas.app.data.local.entity.EstadoMovimiento
import com.finanzas.app.data.local.entity.OrigenMovimiento
import com.finanzas.app.data.local.entity.TipoMovimiento
import com.finanzas.app.ui.components.FondoPantalla
import com.finanzas.app.ui.components.GridCategorias
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme
import com.finanzas.app.ui.theme.TextoMontoConCentavos
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.CancellationException

/**
 * Detalle de movimiento: edita categoria/notas y confirma pendientes. El
 * gesto de regreso predictivo (Android 14+) pide confirmacion si hay cambios
 * sin guardar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMovimientoScreen(
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetalleMovimientoViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var mostrarDialogoDescartar by remember { mutableStateOf(false) }

    // Regreso predictivo (Android 14+): el lambda corre durante el gesto y el
    // codigo posterior al collect se ejecuta cuando el gesto se completa.
    // Con ediciones sin guardar se abre la confirmacion de descarte; sin
    // ediciones, se cierra el detalle (el back queda interceptado a proposito).
    PredictiveBackHandler(enabled = true) { progress ->
        try {
            progress.collect { }
            if (estado.hayEdiciones) {
                mostrarDialogoDescartar = true
            } else {
                onCerrar()
            }
        } catch (e: CancellationException) {
            throw e
        }
    }

    val movimiento = estado.movimiento

    FondoPantalla(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Detalle", style = FinanzasTheme.monto.mediano) },
                    navigationIcon = {
                        IconButton(onClick = onCerrar) {
                            Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
        ) { padding ->
            when {
                estado.cargando || movimiento == null -> {
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = Dimens.EspacioL, vertical = Dimens.EspacioM),
                        verticalArrangement = Arrangement.spacedBy(Dimens.EspacioL),
                    ) {
                        CabeceraMovimiento(estado)
                        GridCategorias(
                            categorias = estado.categorias,
                            seleccionada = estado.categoriaId,
                            onChange = viewModel::onCategoriaChange,
                        )
                        OutlinedTextField(
                            value = estado.notas,
                            onValueChange = viewModel::onNotasChange,
                            label = { Text("Notas") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (estado.pendienteRevision) {
                            Button(
                                onClick = { viewModel.guardar(marcarRevisado = true) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = FinanzasTheme.colores.origenAutomatico,
                                    contentColor = FinanzasTheme.colores.textoPrincipal,
                                ),
                            ) {
                                Icon(Icons.Filled.Done, contentDescription = null)
                                Spacer(modifier = Modifier.padding(Dimens.EspacioXS))
                                Text("Marcar como revisado")
                            }
                        }
                        Button(
                            onClick = { viewModel.guardar() },
                            enabled = estado.hayEdiciones,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FinanzasTheme.colores.origenManual,
                                contentColor = FinanzasTheme.colores.textoPrincipal,
                            ),
                        ) {
                            Text("Guardar cambios")
                        }
                        Spacer(modifier = Modifier.height(Dimens.EspacioM))
                    }
                }
            }
        }
    }

    if (mostrarDialogoDescartar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoDescartar = false },
            title = { Text("Descartar cambios") },
            text = { Text("Hay cambios sin guardar. ¿Salir sin guardarlos?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoDescartar = false
                    onCerrar()
                }) { Text("Descartar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoDescartar = false }) { Text("Seguir editando") }
            },
        )
    }
}

@Composable
private fun CabeceraMovimiento(estado: DetalleMovimientoUiState) {
    val movimiento = estado.movimiento ?: return
    val signo = if (movimiento.tipo == TipoMovimiento.EGRESO) -1L else 1L
    val colorMonto = if (movimiento.tipo == TipoMovimiento.EGRESO) {
        FinanzasTheme.colores.egreso
    } else {
        FinanzasTheme.colores.ingreso
    }
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.EspacioXS)) {
        Text(
            text = movimiento.comercioOrigen ?: "Movimiento manual",
            style = FinanzasTheme.monto.pequeno,
            color = FinanzasTheme.colores.textoSecundario,
        )
        TextoMontoConCentavos(
            centavos = signo * movimiento.montoCentavos,
            estiloEntero = FinanzasTheme.monto.grande.copy(color = colorMonto),
            estiloCentavos = FinanzasTheme.monto.mediano.copy(color = colorMonto),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.EspacioM)) {
            Text(
                text = formatearFechaHora(movimiento.fechaMovimiento),
                style = FinanzasTheme.monto.pequeno,
                color = FinanzasTheme.colores.textoSecundario,
            )
            Text(
                text = if (movimiento.origen == OrigenMovimiento.MANUAL) "Manual" else movimiento.origen.name,
                style = FinanzasTheme.monto.pequeno,
                color = FinanzasTheme.colores.textoSecundario,
            )
            if (movimiento.estado == EstadoMovimiento.PENDIENTE_REVISION) {
                Text(
                    text = "Pendiente",
                    style = FinanzasTheme.monto.pequeno,
                    color = FinanzasTheme.colores.pendienteRevision,
                )
            }
        }
    }
}

private fun formatearFechaHora(millis: Long): String {
    val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-MX"))
    return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(formato)
}
