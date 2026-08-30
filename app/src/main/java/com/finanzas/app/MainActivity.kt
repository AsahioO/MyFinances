package com.finanzas.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.finanzas.app.ui.navigation.DestinoPrincipal
import com.finanzas.app.ui.navigation.FinanzasNavHost
import com.finanzas.app.ui.theme.AppFinanzasTheme
import com.finanzas.app.ui.theme.FinanzasTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AppFinanzasTheme {
                PantallaPrincipal()
            }
        }
    }
}

@Composable
private fun PantallaPrincipal() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BarraNavegacion(navController) },
    ) { padding ->
        FinanzasNavHost(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
}

@Composable
private fun BarraNavegacion(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destinoActual = backStackEntry?.destination

    NavigationBar {
        DestinoPrincipal.entries.forEach { destino ->
            // Se compara contra el grafo, no contra la pantalla, para que la
            // pestana siga marcada al entrar a una pantalla secundaria.
            val seleccionado = destinoActual?.hierarchy?.any { nodo ->
                nodo.hasRoute(destino.grafo::class)
            } == true

            NavigationBarItem(
                selected = seleccionado,
                onClick = {
                    if (!seleccionado) {
                        navController.navigate(destino.grafo) {
                            // Volver a la pestana no apila copias, y cada
                            // seccion conserva su propia pila interna.
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (seleccionado) destino.iconoActivo else destino.iconoInactivo,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(destino.etiqueta)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FinanzasTheme.colores.origenAutomatico,
                    selectedTextColor = FinanzasTheme.colores.textoPrincipal,
                    unselectedIconColor = FinanzasTheme.colores.textoSecundario,
                    unselectedTextColor = FinanzasTheme.colores.textoSecundario,
                ),
            )
        }
    }
}
