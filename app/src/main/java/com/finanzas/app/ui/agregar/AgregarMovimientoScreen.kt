package com.finanzas.app.ui.agregar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.text.AnnotatedString
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
import com.finanzas.app.ui.components.FondoPantalla
import com.finanzas.app.ui.components.GridCategorias
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme
import com.finanzas.app.ui.theme.formatearMontoPlano
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

/**
 * Formulario de alta manual (feature A1): la unica forma de registrar
 * efectivo, y el destino del handoff de OCR (A4, con [AgregarMovimientoUiState.tienePrefill]).
 * Siempre nace MANUAL/CONFIRMADO (ver ViewModel) - mostaza, nunca violeta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarMovimientoScreen(
    onGuardado: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AgregarMovimientoViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(estado.guardado) {
        if (estado.guardado) onGuardado()
    }

    FondoPantalla(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Agregar movimiento", style = FinanzasTheme.monto.mediano) },
                    navigationIcon = {
                        IconButton(onClick = onCancelar) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancelar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.EspacioL, vertical = Dimens.EspacioM),
                verticalArrangement = Arrangement.spacedBy(Dimens.EspacioL),
            ) {
                if (estado.tienePrefill) {
                    BannerRevision()
                }

                CampoMonto(digitos = estado.montoDigitos, onChange = viewModel::onMontoChange)

                SelectorTipo(tipo = estado.tipo, onChange = viewModel::onTipoChange)

                SelectorCuenta(
                    cuentas = estado.cuentas,
                    seleccionada = estado.cuentaId,
                    onChange = viewModel::onCuentaChange,
                )

                SelectorCategoria(
                    categorias = estado.categorias,
                    seleccionada = estado.categoriaId,
                    onChange = viewModel::onCategoriaChange,
                )

                SelectorFecha(
                    fechaMillis = estado.fechaMillis,
                    esFutura = estado.fechaEnFuturo,
                    onChange = viewModel::onFechaChange,
                )

                OutlinedTextField(
                    value = estado.comercio,
                    onValueChange = viewModel::onComercioChange,
                    label = { Text("Comercio o persona") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = estado.notas,
                    onValueChange = viewModel::onNotasChange,
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Button(
                    onClick = viewModel::guardar,
                    enabled = estado.puedeGuardar,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FinanzasTheme.colores.origenManual,
                        contentColor = FinanzasTheme.colores.textoPrincipal,
                    ),
                ) {
                    Text("Guardar")
                }

                Spacer(modifier = Modifier.height(Dimens.EspacioM))
            }
        }
    }
}

@Composable
private fun BannerRevision(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.RadioChip))
            .background(FinanzasTheme.colores.origenManual.copy(alpha = 0.15f))
            .padding(Dimens.EspacioM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = FinanzasTheme.colores.origenManual,
        )
        Spacer(modifier = Modifier.width(Dimens.EspacioXS))
        Text("Revisa los datos detectados", style = FinanzasTheme.monto.pequeno)
    }
}

@Composable
private fun CampoMonto(digitos: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    // Invalido solo si ya escribio algo y el monto quedaria en 0 (plan.md:
    // "Validacion: borde rojo si 0 o >9 digitos" - lo de >9 digitos ya lo evita
    // el filtro/take(9) de onMontoChange, asi que aqui solo queda el caso 0).
    val invalido = digitos.isNotEmpty() && (digitos.toLongOrNull() ?: 0L) <= 0L
    OutlinedTextField(
        value = digitos,
        onValueChange = onChange,
        label = { Text("Monto") },
        singleLine = true,
        isError = invalido,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = MontoVisualTransformation,
        textStyle = FinanzasTheme.monto.grande.copy(textAlign = TextAlign.Center),
        modifier = modifier.fillMaxWidth(),
    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorTipo(tipo: TipoMovimiento, onChange: (TipoMovimiento) -> Unit, modifier: Modifier = Modifier) {
    val opciones = TipoMovimiento.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        opciones.forEachIndexed { indice, opcion ->
            SegmentedButton(
                selected = tipo == opcion,
                onClick = { onChange(opcion) },
                shape = SegmentedButtonDefaults.itemShape(index = indice, count = opciones.size),
                label = { Text(if (opcion == TipoMovimiento.EGRESO) "Egreso" else "Ingreso") },
            )
        }
    }
}

@Composable
private fun SelectorCuenta(
    cuentas: List<CuentaEntity>,
    seleccionada: Long?,
    onChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = "Cuenta",
            style = FinanzasTheme.monto.pequeno,
            color = FinanzasTheme.colores.textoSecundario,
        )
        Spacer(modifier = Modifier.height(Dimens.EspacioXS))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.EspacioXS)) {
            items(cuentas, key = { it.id }) { cuenta ->
                FilterChip(
                    selected = cuenta.id == seleccionada,
                    onClick = { onChange(cuenta.id) },
                    label = { Text(cuenta.nombre) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun SelectorCategoria(
    categorias: List<CategoriaEntity>,
    seleccionada: Long?,
    onChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = "Categoria: " + (categorias.firstOrNull { it.id == seleccionada }?.nombre ?: "Sin categorizar"),
            style = FinanzasTheme.monto.pequeno,
            color = FinanzasTheme.colores.textoSecundario,
        )
        Spacer(modifier = Modifier.height(Dimens.EspacioXS))
        GridCategorias(
            categorias = categorias,
            seleccionada = seleccionada,
            onChange = onChange,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorFecha(
    fechaMillis: Long,
    esFutura: Boolean,
    onChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var mostrarFecha by remember { mutableStateOf(false) }
    var mostrarHora by remember { mutableStateOf(false) }

    Column(modifier) {
        Text(
            text = "Fecha",
            style = FinanzasTheme.monto.pequeno,
            color = FinanzasTheme.colores.textoSecundario,
        )
        Spacer(modifier = Modifier.height(Dimens.EspacioXS))
        OutlinedButton(onClick = { mostrarFecha = true }) {
            Text(formatearFechaHora(fechaMillis))
        }
        if (esFutura) {
            Spacer(modifier = Modifier.height(Dimens.EspacioXXS))
            Text(
                text = "La fecha es futura",
                style = FinanzasTheme.monto.pequeno,
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
                    mostrarHora = true
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

/** DatePicker de M3 trabaja en millis UTC de medianoche: convierte desde un epoch millis local. */
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

private fun formatearFechaHora(millis: Long): String {
    val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-MX"))
    return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(formato)
}
