package com.finanzas.app.ui.inicio

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finanzas.app.data.local.entity.CuentaEntity
import com.finanzas.app.data.local.entity.OrigenMovimiento
import com.finanzas.app.data.local.entity.TipoMovimiento
import com.finanzas.app.domain.cuenta.SaldoCuenta
import com.finanzas.app.domain.reportes.FlujoMes
import com.finanzas.app.ui.common.MovimientoUi
import com.finanzas.app.ui.components.BoundsTransformAgregarBounds
import com.finanzas.app.ui.components.ClipAgregarBounds
import com.finanzas.app.ui.components.ContextoTransicion
import com.finanzas.app.ui.components.EncabezadoSeccion
import com.finanzas.app.ui.components.FilaMovimiento
import com.finanzas.app.ui.components.FondoPantalla
import com.finanzas.app.ui.components.colorSuperficie
import com.finanzas.app.ui.components.compartirContenido
import com.finanzas.app.ui.components.compartirLimite
import com.finanzas.app.ui.inicio.components.BannerPermiso
import com.finanzas.app.ui.inicio.components.EsqueletoInicio
import com.finanzas.app.ui.inicio.components.TarjetaFlujoMes
import com.finanzas.app.ui.inicio.components.TarjetaPendientes
import com.finanzas.app.ui.inicio.components.WalletCardUnica
import com.finanzas.app.ui.theme.AppFinanzasTheme
import com.finanzas.app.ui.theme.ColoresSemanticos
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme
import com.finanzas.app.ui.theme.SurfaceCrema
import com.finanzas.app.ui.theme.nombreMesActual

/**
 * Unica fuente de "agregar-bounds" en Inicio: el FAB circular de 56dp morfea
 * a CardHeroMonto en Agregar (ver OrigenLadoDp en TransicionCompartida.kt).
 * El estado vacio usa un boton tonal sin transicion para no duplicar la clave.
 */
@Composable
private fun colorFondoFab(contexto: ContextoTransicion?): Color =
    colorSuperficie(
        contexto = contexto,
        colorReposo = MaterialTheme.colorScheme.secondary,
        colorContraparte = SurfaceCrema,
    )

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun InicioScreen(
    onAgregar: () -> Unit,
    onFilaClick: (Long) -> Unit,
    onVerMovimientos: () -> Unit,
    onGestionarCuentas: () -> Unit,
    modifier: Modifier = Modifier,
    contextoTransicion: ContextoTransicion? = null,
    viewModel: InicioViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    // Android no tiene callback para el acceso a notificaciones y Samsung puede
    // revocarlo solo (plan.md#5): se re-consulta en cada vuelta a la pantalla.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refrescarPermisoNotificaciones()
    }

    InicioContenido(
        estado = estado,
        onAgregar = onAgregar,
        onFilaClick = onFilaClick,
        onVerMovimientos = onVerMovimientos,
        onGestionarCuentas = onGestionarCuentas,
        onActivarDeteccion = { contexto.startActivity(viewModel.intentAjustesNotificaciones()) },
        onDescartarBanner = viewModel::descartarBannerPermiso,
        modifier = modifier,
        contextoTransicion = contextoTransicion,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun InicioContenido(
    estado: InicioUiState,
    onAgregar: () -> Unit,
    onFilaClick: (Long) -> Unit,
    onVerMovimientos: () -> Unit,
    onGestionarCuentas: () -> Unit,
    onActivarDeteccion: () -> Unit,
    onDescartarBanner: () -> Unit,
    modifier: Modifier = Modifier,
    contextoTransicion: ContextoTransicion? = null,
) {
    // V1 de una sola wallet destacada (decision cerrada): si a futuro se
    // soportan varias, esto vuelve a un carrusel de ElevatedCards.
    val wallet = remember(estado.saldosCuentas) { estado.saldosCuentas.firstOrNull() }
    val recientesVacios = estado.movimientosRecientes.isEmpty()
    val mes = remember { nombreMesActual() }

    FondoPantalla(modifier = modifier.fillMaxSize(), conDegradado = true) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            // El degradado de FondoPantalla queda detras; el Scaffold solo aporta layout.
            containerColor = Color.Transparent,
            // MainActivity ya consume los insets en su propio Scaffold y los pasa
            // como padding al NavHost: pedirlos otra vez aqui duplicaria el margen.
            // Sin topBar a proposito: el mes vive en el hero y una barra
            // colapsable solo dejaba su area expandida vacia (~120dp).
            contentWindowInsets = WindowInsets(0),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAgregar,
                    modifier = Modifier.compartirLimite(
                        "agregar-bounds",
                        contextoTransicion,
                        boundsTransform = BoundsTransformAgregarBounds,
                        clip = ClipAgregarBounds,
                    ),
                    shape = CircleShape,
                    containerColor = colorFondoFab(contextoTransicion),
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Agregar movimiento",
                        modifier = Modifier.compartirContenido(contextoTransicion),
                    )
                }
            },
        ) { padding ->
            if (estado.cargando) {
                EsqueletoInicio(
                    modifier = Modifier
                        .padding(padding)
                        .padding(Dimens.EspacioL),
                )
                return@Scaffold
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = padding.mas(Dimens.EspacioL),
                verticalArrangement = Arrangement.spacedBy(Dimens.EspacioL),
            ) {
                // Los items condicionales se emiten o no, en vez de quedarse
                // como AnimatedVisibility de altura cero: spacedBy reservaria su
                // separacion igual y dejaria un hueco. La aparicion/desaparicion
                // la anima animateItem(), que es el mecanismo propio de la lista.
                if (estado.mostrarBannerPermiso) {
                    item(key = "banner-permiso", contentType = "banner-permiso") {
                        BannerPermiso(
                            onActivar = onActivarDeteccion,
                            onDescartar = onDescartarBanner,
                            modifier = Modifier.animateItem(),
                        )
                    }
                }

                item(key = "hero", contentType = "hero") {
                    TarjetaFlujoMes(
                        flujoMes = estado.flujoMes,
                        mes = mes,
                        modifier = Modifier.animateItem(),
                    )
                }

                if (estado.movimientosPendientes > 0) {
                    item(key = "pendientes", contentType = "pendientes") {
                        TarjetaPendientes(
                            cantidad = estado.movimientosPendientes,
                            onRevisar = onVerMovimientos,
                            modifier = Modifier.animateItem(),
                        )
                    }
                }

                if (wallet != null) {
                    item(key = "header-wallet", contentType = "header-wallet") {
                        EncabezadoSeccion(
                            titulo = "Wallets",
                            textoAccion = "Gestionar",
                            onAccion = onGestionarCuentas,
                            modifier = Modifier.animateItem(),
                        )
                    }
                    item(key = "wallet", contentType = "wallet") {
                        WalletCardUnica(
                            saldoCuenta = wallet,
                            onClick = onGestionarCuentas,
                            modifier = Modifier.animateItem(),
                        )
                    }
                }

                item(key = "header-recientes", contentType = "header-recientes") {
                    EncabezadoSeccion(
                        titulo = "Recientes",
                        textoAccion = "Ver todas".takeIf { !recientesVacios },
                        onAccion = onVerMovimientos.takeIf { !recientesVacios },
                    )
                }

                if (recientesVacios) {
                    item(key = "vacio", contentType = "vacio") {
                        EstadoVacioRecientes(onAgregar = onAgregar)
                    }
                } else {
                    item(key = "recientes-card", contentType = "recientes-card") {
                        TarjetaRecientes(
                            movimientos = estado.movimientosRecientes,
                            onFilaClick = onFilaClick,
                            onVerTodas = onVerMovimientos,
                            contextoTransicion = contextoTransicion,
                            modifier = Modifier.animateItem(),
                        )
                    }
                }

                // Aire bajo el FAB para que la ultima fila no quede tapada.
                item(key = "espaciador-fab", contentType = "espaciador") {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

/**
 * Recientes agrupados en una sola ElevatedCard M3: las filas comparten
 * contenedor con divisores internos y el "Ver todas" vive como footer de la
 * card en vez de solo en el encabezado.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun TarjetaRecientes(
    movimientos: List<MovimientoUi>,
    onFilaClick: (Long) -> Unit,
    onVerTodas: () -> Unit,
    modifier: Modifier = Modifier,
    contextoTransicion: ContextoTransicion? = null,
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = Dimens.EspacioS)) {
            movimientos.forEachIndexed { indice, movimiento ->
                FilaMovimiento(
                    movimiento = movimiento,
                    onClick = { onFilaClick(movimiento.id) },
                    contextoTransicion = contextoTransicion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.EspacioM),
                )
                if (indice < movimientos.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            horizontal = Dimens.EspacioM,
                            vertical = Dimens.EspacioS,
                        ),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
            TextButton(
                onClick = onVerTodas,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.EspacioS),
            ) {
                Text(text = "Ver todas")
            }
        }
    }
}

@Composable
private fun EstadoVacioRecientes(
    onAgregar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.EspacioL),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.EspacioS),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Aun no hay movimientos",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Registra tu primer gasto en efectivo o activa la deteccion automatica.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FilledTonalButton(onClick = onAgregar) {
                Text(text = "Agregar movimiento")
            }
        }
    }
}

/** Suma el padding propio de la lista al que reserva el Scaffold para la top bar. */
@Composable
private fun PaddingValues.mas(extra: Dp): PaddingValues {
    val direccion = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(direccion) + extra,
        top = calculateTopPadding() + extra,
        end = calculateEndPadding(direccion) + extra,
        bottom = calculateBottomPadding() + extra,
    )
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun InicioConDatosPreview() {
    AppFinanzasTheme {
        InicioContenido(
            estado = estadoMockConDatos(),
            onAgregar = {},
            onFilaClick = {},
            onVerMovimientos = {},
            onGestionarCuentas = {},
            onActivarDeteccion = {},
            onDescartarBanner = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun InicioVacioPreview() {
    AppFinanzasTheme {
        InicioContenido(
            estado = InicioUiState(cargando = false),
            onAgregar = {},
            onFilaClick = {},
            onVerMovimientos = {},
            onGestionarCuentas = {},
            onActivarDeteccion = {},
            onDescartarBanner = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun InicioCargandoPreview() {
    AppFinanzasTheme {
        InicioContenido(
            estado = InicioUiState(cargando = true),
            onAgregar = {},
            onFilaClick = {},
            onVerMovimientos = {},
            onGestionarCuentas = {},
            onActivarDeteccion = {},
            onDescartarBanner = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun InicioSinDeteccionPreview() {
    AppFinanzasTheme {
        InicioContenido(
            estado = estadoMockConDatos().copy(deteccionAutomaticaActiva = false),
            onAgregar = {},
            onFilaClick = {},
            onVerMovimientos = {},
            onGestionarCuentas = {},
            onActivarDeteccion = {},
            onDescartarBanner = {},
        )
    }
}

private fun estadoMockConDatos(): InicioUiState {
    val colores = ColoresSemanticos()
    val nombresComercio = listOf("Cafe Habana", "Uber", "Spotify", "Farmacia San Pablo")
    val cuenta = CuentaEntity(id = 1L, nombre = "Cuenta Nu", origen = OrigenMovimiento.NU)
    return InicioUiState(
        cargando = false,
        flujoMes = FlujoMes(ingresosCentavos = 3_500_000L, egresosCentavos = 1_850_078L),
        saldosCuentas = listOf(SaldoCuenta(cuenta = cuenta, saldoCentavos = 456_737L)),
        movimientosPendientes = 2,
        movimientosRecientes = nombresComercio.mapIndexed { indice, nombre ->
            MovimientoUi(
                id = indice.toLong() + 1,
                comercioOrigen = nombre,
                categoriaNombre = "Comida",
                montoCentavos = 5_000L + indice * 321L,
                tipo = if (indice == 2) TipoMovimiento.INGRESO else TipoMovimiento.EGRESO,
                colorOrigen = colores.origenAutomatico,
                fechaMovimiento = 0L,
                pendienteRevision = indice == 0,
                categoriaIcono = "Restaurant",
                categoriaColorHex = "#E07856",
            )
        },
    )
}
