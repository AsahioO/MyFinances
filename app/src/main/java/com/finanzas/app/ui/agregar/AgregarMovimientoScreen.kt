package com.finanzas.app.ui.agregar

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finanzas.app.data.local.entity.CategoriaEntity
import com.finanzas.app.data.local.entity.CuentaEntity
import com.finanzas.app.data.local.entity.TipoMovimiento
import com.finanzas.app.ui.components.BoundsTransformAgregarBounds
import com.finanzas.app.ui.components.ClipAgregarBounds
import com.finanzas.app.ui.components.ContextoTransicion
import com.finanzas.app.ui.components.DesvanecimientoMaximoRetroceso
import com.finanzas.app.ui.components.EasingRetrocesoPredictivo
import com.finanzas.app.ui.components.EncogimientoMaximoRetroceso
import com.finanzas.app.ui.components.FondoPantalla
import com.finanzas.app.ui.components.GridCategorias
import com.finanzas.app.ui.components.colorSuperficie
import com.finanzas.app.ui.components.compartirContenido
import com.finanzas.app.ui.components.compartirLimite
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme
import com.finanzas.app.ui.theme.SurfaceCrema
import com.finanzas.app.ui.theme.SurfaceLavanda
import com.finanzas.app.ui.theme.formatearMontoPlano
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.CancellationException

/**
 * Formulario de alta manual rediseñado: hero de monto protagonista + detalles
 * agrupados en card + acción sticky abajo. Misma semántica mostaza (manual)
 * y paleta cálida fija — solo cambia la jerarquía visual para que todo quepa
 * en ~1 viewport sin scroll largo.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AgregarMovimientoScreen(
    onGuardado: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier,
    contextoTransicion: ContextoTransicion? = null,
    viewModel: AgregarMovimientoViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(estado.guardado) {
        if (estado.guardado) onGuardado()
    }

    val hayEdiciones = estado.montoDigitos.isNotEmpty() || estado.comercio.isNotBlank() ||
        estado.notas.isNotBlank() || estado.categoriaId != null || estado.tipo != TipoMovimiento.EGRESO
    var mostrarDialogoDescartar by remember { mutableStateOf(false) }

    val progresoRetroceso = remember { Animatable(0f) }
    PredictiveBackHandler(enabled = true) { progress ->
        try {
            progress.collect { evento -> progresoRetroceso.snapTo(evento.progress) }
            if (hayEdiciones) {
                progresoRetroceso.animateTo(0f)
                mostrarDialogoDescartar = true
            } else {
                onCancelar()
            }
        } catch (e: CancellationException) {
            withContext(NonCancellable) {
                progresoRetroceso.animateTo(0f)
            }
            throw e
        }
    }

    FondoPantalla(modifier = modifier.fillMaxSize(), conDegradado = true) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Nuevo movimiento", style = FinanzasTheme.monto.mediano)
                            Text(
                                "Efectivo · se guarda como confirmado",
                                style = MaterialTheme.typography.labelSmall,
                                color = FinanzasTheme.colores.textoSecundario,
                            )
                        }
                    },
                    navigationIcon = {
                        Surface(
                            shape = CircleShape,
                            color = SurfaceCrema,
                            shadowElevation = 1.dp,
                            modifier = Modifier.padding(start = 4.dp),
                        ) {
                            IconButton(
                                onClick = { if (hayEdiciones) mostrarDialogoDescartar = true else onCancelar() },
                                modifier = Modifier.size(40.dp),
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
            bottomBar = {
                Surface(color = SurfaceCrema, shadowElevation = 4.dp) {
                    Button(
                        onClick = viewModel::guardar,
                        enabled = estado.puedeGuardar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.EspacioL),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FinanzasTheme.colores.origenManual,
                            contentColor = FinanzasTheme.colores.textoPrincipal,
                        ),
                    ) {
                        Text("Guardar")
                    }
                }
            },
        ) { paddingInterno ->
            Column(
                modifier = Modifier
                    .padding(paddingInterno)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.EspacioL),
                verticalArrangement = Arrangement.spacedBy(Dimens.EspacioL),
            ) {
                CardHeroMonto(
                    tipo = estado.tipo,
                    digitos = estado.montoDigitos,
                    onTipoChange = viewModel::onTipoChange,
                    onMontoChange = viewModel::onMontoChange,
                    colorFondo = colorSuperficie(
                        contexto = contextoTransicion,
                        colorReposo = SurfaceCrema,
                        colorContraparte = FinanzasTheme.colores.origenManual.copy(alpha = 0.15f),
                    ),
                    contenidoModifier = Modifier.compartirContenido(contextoTransicion),
                    modifier = Modifier
                        .compartirLimite(
                            "agregar-bounds",
                            contextoTransicion,
                            boundsTransform = BoundsTransformAgregarBounds,
                            clip = ClipAgregarBounds,
                        )
                        .graphicsLayer {
                            val progresoSuavizado = EasingRetrocesoPredictivo.transform(progresoRetroceso.value)
                            val escala = 1f - progresoSuavizado * EncogimientoMaximoRetroceso
                            scaleX = escala
                            scaleY = escala
                            alpha = 1f - progresoSuavizado * DesvanecimientoMaximoRetroceso
                        },
                )

                CardDetalles(
                    cuentas = estado.cuentas,
                    cuentaId = estado.cuentaId,
                    onCuentaChange = viewModel::onCuentaChange,
                    fechaMillis = estado.fechaMillis,
                    esFutura = estado.fechaEnFuturo,
                    onFechaChange = viewModel::onFechaChange,
                    categorias = estado.categorias,
                    categoriaId = estado.categoriaId,
                    onCategoriaChange = viewModel::onCategoriaChange,
                    comercio = estado.comercio,
                    onComercioChange = viewModel::onComercioChange,
                    notas = estado.notas,
                    onNotasChange = viewModel::onNotasChange,
                )

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }

    if (mostrarDialogoDescartar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoDescartar = false },
            title = { Text("Descartar cambios") },
            text = { Text("Hay datos sin guardar. ¿Salir sin guardarlos?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoDescartar = false
                    onCancelar()
                }) { Text("Descartar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoDescartar = false }) { Text("Seguir editando") }
            },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero Monto
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CardHeroMonto(
    tipo: TipoMovimiento,
    digitos: String,
    onTipoChange: (TipoMovimiento) -> Unit,
    onMontoChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    colorFondo: Color = SurfaceCrema,
    contenidoModifier: Modifier = Modifier,
) {
    val colorMonto = if (tipo == TipoMovimiento.EGRESO) FinanzasTheme.colores.egreso else FinanzasTheme.colores.ingreso
    val invalido = digitos.isNotEmpty() && (digitos.toLongOrNull() ?: 0L) <= 0L

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.RadioTarjeta),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(contenidoModifier)
                .padding(Dimens.EspacioL),
            verticalArrangement = Arrangement.spacedBy(Dimens.EspacioM),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ToggleTipoPill(tipo = tipo, onChange = onTipoChange)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (tipo == TipoMovimiento.EGRESO) "¿Cuánto gastaste?" else "¿Cuánto recibiste?",
                    style = MaterialTheme.typography.labelMedium,
                    color = FinanzasTheme.colores.textoSecundario,
                )
                Spacer(modifier = Modifier.height(Dimens.EspacioXS))
                OutlinedTextField(
                    value = digitos,
                    onValueChange = onMontoChange,
                    singleLine = true,
                    isError = invalido,
                    placeholder = {
                        Text(
                            "$0.00",
                            style = FinanzasTheme.monto.grande.copy(
                                textAlign = TextAlign.Center,
                                color = FinanzasTheme.colores.textoSecundario.copy(alpha = 0.35f),
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = MontoVisualTransformation,
                    textStyle = FinanzasTheme.monto.grande.copy(
                        textAlign = TextAlign.Center,
                        color = if (invalido) FinanzasTheme.colores.egreso else colorMonto,
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (invalido) FinanzasTheme.colores.egreso else Color.Transparent,
                        unfocusedBorderColor = if (invalido) FinanzasTheme.colores.egreso.copy(alpha = 0.6f) else Color.Transparent,
                        errorBorderColor = FinanzasTheme.colores.egreso,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        cursorColor = colorMonto,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (invalido) {
                    Text(
                        "Ingresa un monto mayor a $0.00",
                        style = MaterialTheme.typography.labelSmall,
                        color = FinanzasTheme.colores.egreso,
                    )
                } else {
                    Text(
                        "Máx. 9 dígitos · se guarda en centavos",
                        style = MaterialTheme.typography.labelSmall,
                        color = FinanzasTheme.colores.textoSecundario.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleTipoPill(
    tipo: TipoMovimiento,
    onChange: (TipoMovimiento) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(SurfaceLavanda)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        PillTipo(
            seleccionado = tipo == TipoMovimiento.EGRESO,
            icono = Icons.AutoMirrored.Filled.TrendingDown,
            texto = "Gasto",
            colorActivo = FinanzasTheme.colores.egreso,
            onClick = { onChange(TipoMovimiento.EGRESO) },
            modifier = Modifier.weight(1f),
        )
        PillTipo(
            seleccionado = tipo == TipoMovimiento.INGRESO,
            icono = Icons.AutoMirrored.Filled.TrendingUp,
            texto = "Ingreso",
            colorActivo = FinanzasTheme.colores.ingreso,
            onClick = { onChange(TipoMovimiento.INGRESO) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PillTipo(
    seleccionado: Boolean,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String,
    colorActivo: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (seleccionado) colorActivo else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.EspacioM, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = if (seleccionado) SurfaceCrema else FinanzasTheme.colores.textoSecundario,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = if (seleccionado) SurfaceCrema else FinanzasTheme.colores.textoSecundario,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Card Detalles agrupados
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CardDetalles(
    cuentas: List<CuentaEntity>,
    cuentaId: Long?,
    onCuentaChange: (Long) -> Unit,
    fechaMillis: Long,
    esFutura: Boolean,
    onFechaChange: (Long) -> Unit,
    categorias: List<CategoriaEntity>,
    categoriaId: Long?,
    onCategoriaChange: (Long) -> Unit,
    comercio: String,
    onComercioChange: (String) -> Unit,
    notas: String,
    onNotasChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.RadioTarjeta),
        colors = CardDefaults.cardColors(containerColor = SurfaceCrema),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.EspacioM),
            verticalArrangement = Arrangement.spacedBy(Dimens.EspacioM),
        ) {
            SeccionCuenta(
                cuentas = cuentas,
                seleccionada = cuentaId,
                onChange = onCuentaChange,
            )

            HorizontalDivider(color = SurfaceLavanda, thickness = 1.dp)

            SeccionFechaHora(
                fechaMillis = fechaMillis,
                esFutura = esFutura,
                onChange = onFechaChange,
            )

            HorizontalDivider(color = SurfaceLavanda, thickness = 1.dp)

            SeccionCategoriaColapsable(
                categorias = categorias,
                seleccionada = categoriaId,
                onChange = onCategoriaChange,
            )

            HorizontalDivider(color = SurfaceLavanda, thickness = 1.dp)

            OutlinedTextField(
                value = comercio,
                onValueChange = onComercioChange,
                label = { Text("Comercio o persona") },
                placeholder = { Text("Ej. OXXO, Uber, mamá…") },
                leadingIcon = {
                    Icon(Icons.Filled.Storefront, contentDescription = null, tint = FinanzasTheme.colores.textoSecundario)
                },
                singleLine = true,
                shape = RoundedCornerShape(Dimens.RadioChip),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FinanzasTheme.colores.origenManual,
                    unfocusedBorderColor = SurfaceLavanda,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = notas,
                onValueChange = onNotasChange,
                label = { Text("Notas") },
                placeholder = { Text("Opcional — ej. efectivo, propina…") },
                leadingIcon = {
                    Icon(Icons.Filled.EditNote, contentDescription = null, tint = FinanzasTheme.colores.textoSecundario)
                },
                shape = RoundedCornerShape(Dimens.RadioChip),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FinanzasTheme.colores.origenManual,
                    unfocusedBorderColor = SurfaceLavanda,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SeccionCuenta(
    cuentas: List<CuentaEntity>,
    seleccionada: Long?,
    onChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(FinanzasTheme.colores.origenManual.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBalanceWallet,
                    contentDescription = null,
                    tint = FinanzasTheme.colores.origenManual,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(modifier = Modifier.width(Dimens.EspacioS))
            Text(
                "Cuenta",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = FinanzasTheme.colores.textoPrincipal,
            )
            Spacer(modifier = Modifier.width(Dimens.EspacioS))
            Text(
                cuentas.firstOrNull { it.id == seleccionada }?.nombre ?: "Selecciona",
                style = MaterialTheme.typography.labelSmall,
                color = FinanzasTheme.colores.textoSecundario,
            )
        }
        Spacer(modifier = Modifier.height(Dimens.EspacioS))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.EspacioXS),
            contentPadding = PaddingValues(end = Dimens.EspacioS),
        ) {
            items(cuentas, key = { it.id }) { cuenta ->
                val sel = cuenta.id == seleccionada
                FilterChip(
                    selected = sel,
                    onClick = { onChange(cuenta.id) },
                    label = { Text(cuenta.nombre, style = MaterialTheme.typography.labelMedium) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FinanzasTheme.colores.origenAutomatico,
                        selectedLabelColor = SurfaceCrema,
                        selectedLeadingIconColor = SurfaceCrema,
                        containerColor = SurfaceLavanda,
                        labelColor = FinanzasTheme.colores.textoSecundario,
                    ),
                    border = null,
                    shape = RoundedCornerShape(50),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeccionFechaHora(
    fechaMillis: Long,
    esFutura: Boolean,
    onChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var mostrarFecha by remember { mutableStateOf(false) }
    var mostrarHora by remember { mutableStateOf(false) }

    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(FinanzasTheme.colores.textoSecundario.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = FinanzasTheme.colores.textoSecundario,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(modifier = Modifier.width(Dimens.EspacioS))
            Text(
                "Fecha y hora",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = FinanzasTheme.colores.textoPrincipal,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (esFutura) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = FinanzasTheme.colores.egreso.copy(alpha = 0.12f),
                ) {
                    Text(
                        "Futura",
                        style = MaterialTheme.typography.labelSmall,
                        color = FinanzasTheme.colores.egreso,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(Dimens.EspacioS))
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.EspacioS), modifier = Modifier.fillMaxWidth()) {
            PillFechaHora(
                icono = Icons.Filled.CalendarMonth,
                texto = formatearSoloFecha(fechaMillis),
                onClick = { mostrarFecha = true },
                modifier = Modifier.weight(1f),
            )
            PillFechaHora(
                icono = Icons.Filled.Schedule,
                texto = formatearSoloHora(fechaMillis),
                onClick = { mostrarHora = true },
                modifier = Modifier.weight(1f),
            )
        }
        if (esFutura) {
            Spacer(modifier = Modifier.height(Dimens.EspacioXXS))
            Text(
                "La fecha es futura",
                style = MaterialTheme.typography.labelSmall,
                color = FinanzasTheme.colores.egreso,
            )
        }
    }

    if (mostrarFecha) {
        val estadoFecha = rememberDatePickerState(
            initialSelectedDateMillis = fechaUtcDeMillisLocal(fechaMillis),
        )
        DatePickerDialog(
            onDismissRequest = { mostrarFecha = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoFecha.selectedDateMillis?.let { seleccionUtc ->
                        onChange(combinarFechaUtcConHoraLocal(seleccionUtc, fechaMillis))
                    }
                    mostrarFecha = false
                }) { Text("Siguiente") }
            },
            dismissButton = { TextButton(onClick = { mostrarFecha = false }) { Text("Cancelar") } },
        ) {
            DatePicker(state = estadoFecha)
        }
    }

    if (mostrarHora) {
        val calendario = remember(fechaMillis) {
            Calendar.getInstance().apply { timeInMillis = fechaMillis }
        }
        val estadoHora = rememberTimePickerState(
            initialHour = calendario.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendario.get(Calendar.MINUTE),
        )
        AlertDialog(
            onDismissRequest = { mostrarHora = false },
            confirmButton = {
                TextButton(onClick = {
                    onChange(combinarHora(fechaMillis, estadoHora.hour, estadoHora.minute))
                    mostrarHora = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { mostrarHora = false }) { Text("Cancelar") } },
            text = { TimePicker(state = estadoHora) },
        )
    }
}

@Composable
private fun PillFechaHora(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(SurfaceLavanda)
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.EspacioM, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = FinanzasTheme.colores.textoSecundario,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(Dimens.EspacioXS))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = FinanzasTheme.colores.textoSecundario,
        )
    }
}

@Composable
private fun SeccionCategoriaColapsable(
    categorias: List<CategoriaEntity>,
    seleccionada: Long?,
    onChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expandida by remember { mutableStateOf(seleccionada == null) }
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expandida = !expandida }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(FinanzasTheme.colores.origenAutomatico.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Category,
                    contentDescription = null,
                    tint = FinanzasTheme.colores.origenAutomatico,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(modifier = Modifier.width(Dimens.EspacioS))
            Text(
                "Categoría",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = FinanzasTheme.colores.textoPrincipal,
                modifier = Modifier.weight(1f),
            )
            Text(
                categorias.firstOrNull { it.id == seleccionada }?.nombre ?: "Sin categoría",
                style = MaterialTheme.typography.labelSmall,
                color = FinanzasTheme.colores.textoSecundario,
            )
            Icon(
                imageVector = if (expandida) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = FinanzasTheme.colores.textoSecundario,
            )
        }
        if (expandida) {
            Spacer(modifier = Modifier.height(Dimens.EspacioS))
            GridCategorias(
                categorias = categorias,
                seleccionada = seleccionada,
                onChange = {
                    onChange(it)
                    expandida = false
                },
            )
        }
    }
}

/**
 * Digitos crudos ("21900") -> texto formateado ("$219.00") en vivo. El
 * OffsetMapping siempre manda el cursor al final: es la simplificacion usual
 * para campos de dinero, donde se escribe como calculadora (los digitos se
 * agregan/borran por la derecha), no se edita en medio del numero.
 */
private object MontoVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text
        val centavos = digitos.toLongOrNull() ?: 0L
        val formateado = formatearMontoPlano(centavos)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = formateado.length
            override fun transformedToOriginal(offset: Int): Int = digitos.length
        }
        return TransformedText(AnnotatedString(formateado), offsetMapping)
    }
}

private fun formatearSoloFecha(millis: Long): String {
    val formato = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("es-MX"))
    return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(formato)
}

private fun formatearSoloHora(millis: Long): String {
    val formato = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("es-MX"))
    return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(formato)
}

private fun fechaUtcDeMillisLocal(millis: Long): Long =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun combinarFechaUtcConHoraLocal(fechaUtcMillis: Long, horaReferenciaMillis: Long): Long {
    val fecha = Instant.ofEpochMilli(fechaUtcMillis).atZone(ZoneOffset.UTC).toLocalDate()
    val zona = ZoneId.systemDefault()
    val horaLocal = Instant.ofEpochMilli(horaReferenciaMillis).atZone(zona).toLocalTime()
    return fecha.atTime(horaLocal).atZone(zona).toInstant().toEpochMilli()
}

private fun combinarHora(fechaReferenciaMillis: Long, hora: Int, minuto: Int): Long {
    val zona = ZoneId.systemDefault()
    val fecha = Instant.ofEpochMilli(fechaReferenciaMillis).atZone(zona).toLocalDate()
    return fecha.atTime(hora, minuto).atZone(zona).toInstant().toEpochMilli()
}
