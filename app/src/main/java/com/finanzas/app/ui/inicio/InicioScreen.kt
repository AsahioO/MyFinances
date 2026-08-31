package com.finanzas.app.ui.inicio

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finanzas.app.data.local.entity.CuentaEntity
import com.finanzas.app.data.local.entity.OrigenMovimiento
import com.finanzas.app.data.local.entity.TipoMovimiento
import com.finanzas.app.domain.cuenta.SaldoCuenta
import com.finanzas.app.domain.reportes.FlujoMes
import com.finanzas.app.ui.common.MovimientoUi
import com.finanzas.app.ui.components.AccesoRapidoChip
import com.finanzas.app.ui.components.BoundsTransformAgregarBounds
import com.finanzas.app.ui.components.ClipAgregarBounds
import com.finanzas.app.ui.components.ContextoTransicion
import com.finanzas.app.ui.components.EncabezadoSeccion
import com.finanzas.app.ui.components.FilaMovimiento
import com.finanzas.app.ui.components.FondoPantalla
import com.finanzas.app.ui.components.colorSuperficie
import com.finanzas.app.ui.components.compartirLimite
import com.finanzas.app.ui.inicio.components.HeroBalance
import com.finanzas.app.ui.inicio.components.WalletCardUnica
import com.finanzas.app.ui.theme.AppFinanzasTheme
import com.finanzas.app.ui.theme.ColoresSemanticos
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme
import com.finanzas.app.ui.theme.SurfaceCrema

private val ColoresCirculoAgregar: IconButtonColors
    @Composable get() = IconButtonDefaults.iconButtonColors(
        containerColor = FinanzasTheme.colores.textoSecundario.copy(alpha = 0.1f),
    )

/** El color de fondo del circulo morfea al de CardHeroMonto durante la transicion. */
@Composable
private fun ColoresCirculoAgregar(contexto: ContextoTransicion?): IconButtonColors =
    IconButtonDefaults.iconButtonColors(
        containerColor = colorSuperficie(
            contexto = contexto,
            colorReposo = FinanzasTheme.colores.origenManual.copy(alpha = 0.15f),
            colorContraparte = SurfaceCrema,
        ),
        contentColor = FinanzasTheme.colores.origenManual,
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
    InicioContenido(
        estado = estado,
        onAgregar = onAgregar,
        onFilaClick = onFilaClick,
        onVerMovimientos = onVerMovimientos,
        onGestionarCuentas = onGestionarCuentas,
        modifier = modifier,
        contextoTransicion = contextoTransicion,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun InicioContenido(
    estado: InicioUiState,
    onAgregar: () -> Unit,
    onFilaClick: (Long) -> Unit,
    onVerMovimientos: () -> Unit,
    onGestionarCuentas: () -> Unit,
    modifier: Modifier = Modifier,
    contextoTransicion: ContextoTransicion? = null,
) {
    // V1 de una sola wallet destacada (decision cerrada): si a futuro se
    // soportan varias, esto vuelve a un LazyRow como el que tenia antes.
    val wallet = remember(estado.saldosCuentas) { estado.saldosCuentas.firstOrNull() }
    val recientesVacios = estado.movimientosRecientes.isEmpty()
    val accionRecientes = remember(recientesVacios, onVerMovimientos) {
        onVerMovimientos.takeIf { !recientesVacios }
    }
    val textoAccionRecientes = remember(recientesVacios) {
        "Ver todas".takeIf { !recientesVacios }
    }

    FondoPantalla(modifier = modifier.fillMaxSize(), conDegradado = true) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Dimens.EspacioL),
            verticalArrangement = Arrangement.spacedBy(Dimens.EspacioL),
        ) {
            item(contentType = "hero") { HeroBalance(flujoMes = estado.flujoMes) }

            item(contentType = "accesos") {
                FilaAccesosRapidos(
                    movimientosPendientes = estado.movimientosPendientes,
                    onAgregar = onAgregar,
                    onPendientes = onVerMovimientos,
                    contexto = contextoTransicion,
                )
            }

            if (wallet != null) {
                item(contentType = "wallet") {
                    TarjetaWallets(wallet = wallet, onAgregarCuenta = onGestionarCuentas)
                }
            }

            item(contentType = "header-recientes") {
                EncabezadoSeccion(
                    titulo = "Recientes",
                    textoAccion = textoAccionRecientes,
                    onAccion = accionRecientes,
                )
            }

            if (recientesVacios) {
                item(contentType = "vacio") {
                    EstadoVacioRecientes(onAgregar = onAgregar, contexto = contextoTransicion)
                }
            }

            items(
                items = estado.movimientosRecientes,
                key = { it.id },
                contentType = { "movimiento" },
            ) { movimiento ->
                FilaMovimiento(
                    movimiento = movimiento,
                    onClick = { onFilaClick(movimiento.id) },
                    contextoTransicion = contextoTransicion,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun FilaAccesosRapidos(
    movimientosPendientes: Int,
    onAgregar: () -> Unit,
    onPendientes: () -> Unit,
    modifier: Modifier = Modifier,
    contexto: ContextoTransicion? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.EspacioL, Alignment.CenterHorizontally),
    ) {
        AccesoRapidoChip(
            icono = Icons.Filled.Add,
            etiqueta = "Agregar",
            onClick = onAgregar,
            modifierCirculo = Modifier.compartirLimite(
                "agregar-bounds",
                contexto,
                boundsTransform = BoundsTransformAgregarBounds,
                clip = ClipAgregarBounds,
            ),
            colores = ColoresCirculoAgregar(contexto),
            contexto = contexto,
        )
        AccesoRapidoChip(
            icono = Icons.Filled.PendingActions,
            etiqueta = "Pendientes",
            onClick = onPendientes,
            contador = movimientosPendientes,
        )
    }
}

@Composable
private fun TarjetaWallets(
    wallet: SaldoCuenta,
    onAgregarCuenta: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.RadioTarjeta),
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(Dimens.EspacioM)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                EncabezadoSeccion(titulo = "Wallets", modifier = Modifier.weight(1f))
                IconButton(onClick = onAgregarCuenta) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Agregar cuenta",
                        tint = FinanzasTheme.colores.origenAutomatico,
                    )
                }
            }
            Spacer(modifier = Modifier.height(Dimens.EspacioS))
            WalletCardUnica(saldoCuenta = wallet)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun EstadoVacioRecientes(
    onAgregar: () -> Unit,
    modifier: Modifier = Modifier,
    contexto: ContextoTransicion? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Aun no hay movimientos",
            style = FinanzasTheme.monto.pequeno,
            color = FinanzasTheme.colores.textoSecundario,
        )
        Spacer(modifier = Modifier.height(Dimens.EspacioS))
        AccesoRapidoChip(
            icono = Icons.Filled.Add,
            etiqueta = "Agregar movimiento",
            onClick = onAgregar,
            modifierCirculo = Modifier.compartirLimite(
                "agregar-bounds",
                contexto,
                boundsTransform = BoundsTransformAgregarBounds,
                clip = ClipAgregarBounds,
            ),
            colores = ColoresCirculoAgregar(contexto),
            contexto = contexto,
        )
    }
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
