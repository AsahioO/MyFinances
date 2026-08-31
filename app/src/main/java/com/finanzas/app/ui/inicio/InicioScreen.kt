package com.finanzas.app.ui.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finanzas.app.domain.cuenta.SaldoCuenta
import com.finanzas.app.ui.components.AccesoRapidoChip
import com.finanzas.app.ui.components.FilaMovimiento
import com.finanzas.app.ui.components.FondoPantalla
import com.finanzas.app.ui.inicio.components.TarjetaCuenta
import com.finanzas.app.ui.theme.Dimens
import com.finanzas.app.ui.theme.FinanzasTheme
import com.finanzas.app.ui.theme.TextoMontoConCentavos

@Composable
fun InicioScreen(
    modifier: Modifier = Modifier,
    viewModel: InicioViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    FondoPantalla(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Dimens.EspacioL),
            verticalArrangement = Arrangement.spacedBy(Dimens.EspacioL),
        ) {
            item {
                Text(
                    text = "Flujo de este mes",
                    style = FinanzasTheme.monto.pequeno,
                    color = FinanzasTheme.colores.textoSecundario,
                )
                TextoMontoConCentavos(
                    centavos = estado.flujoMes.netoCentavos,
                    estiloEntero = FinanzasTheme.monto.grande,
                    estiloCentavos = FinanzasTheme.monto.mediano,
                )
            }

            item { AccesosRapidos(movimientosPendientes = estado.movimientosPendientes) }

            if (estado.saldosCuentas.isNotEmpty()) {
                item {
                    Text(text = "Cuentas", style = FinanzasTheme.monto.mediano)
                }
                item { ListaCuentas(saldos = estado.saldosCuentas) }
            }

            item {
                Text(
                    text = "Recientes",
                    style = FinanzasTheme.monto.mediano,
                )
            }

            if (estado.movimientosRecientes.isEmpty()) {
                item {
                    Text(
                        text = "Aun no hay movimientos",
                        style = FinanzasTheme.monto.pequeno,
                        color = FinanzasTheme.colores.textoSecundario,
                    )
                }
            }

            items(estado.movimientosRecientes, key = { it.id }) { movimiento ->
                FilaMovimiento(movimiento = movimiento)
            }
        }
    }
}

@Composable
private fun AccesosRapidos(movimientosPendientes: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        // Sin accion real todavia: alta manual, revision de pendientes y
        // gestion de categorias quedan fuera del alcance de esta iteracion.
        AccesoRapidoChip(icono = Icons.Filled.Add, etiqueta = "Agregar", onClick = {})
        AccesoRapidoChip(
            icono = Icons.Filled.PendingActions,
            etiqueta = "Pendientes",
            onClick = {},
            contador = movimientosPendientes,
        )
        AccesoRapidoChip(icono = Icons.Filled.Category, etiqueta = "Categorias", onClick = {})
    }
}

@Composable
private fun ListaCuentas(saldos: List<SaldoCuenta>, modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.EspacioS),
    ) {
        items(saldos, key = { it.cuenta.id }) { saldo ->
            TarjetaCuenta(saldoCuenta = saldo)
        }
    }
}
