package com.finanzas.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.finanzas.app.ui.theme.SurfaceCrema
import com.finanzas.app.ui.theme.SurfaceLavanda
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.finanzas.app.ui.navigation.DestinoPrincipal
import com.finanzas.app.ui.navigation.FinanzasNavHost
import com.finanzas.app.ui.navigation.GrafoAcciones
import com.finanzas.app.ui.navigation.RutaAgregarManual
import com.finanzas.app.ui.theme.AppFinanzasTheme
import com.finanzas.app.ui.theme.FinanzasTheme
import dagger.hilt.android.AndroidEntryPoint

/** Extra del intent del App Shortcut "Agregar gasto en efectivo" (res/xml/shortcuts.xml). */
private const val EXTRA_DESTINO = "destino"
private const val DESTINO_AGREGAR = "agregar"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Estado Compose fuera de setContent: onNewIntent puede re-disparar el
    // shortcut con la app ya abierta (launchMode singleTask), y el cambio
    // debe recomponer PantallaPrincipal para re-navegar.
    private val destinoInicial = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        destinoInicial.value = intent?.getStringExtra(EXTRA_DESTINO)
        setContent {
            AppFinanzasTheme {
                PantallaPrincipal(destinoInicial = destinoInicial.value)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        destinoInicial.value = intent.getStringExtra(EXTRA_DESTINO)
    }
}

@Composable
private fun PantallaPrincipal(destinoInicial: String?) {
    val navController = rememberNavController()

    // El shortcut de mantener presionado el icono ("Agregar gasto en
    // efectivo") no tiene su propia pantalla: abre la app y navega directo al
    // formulario de A1, una sola vez por lanzamiento del intent.
    LaunchedEffect(destinoInicial) {
        if (destinoInicial == DESTINO_AGREGAR) {
            navController.navigate(RutaAgregarManual())
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    // Las pantallas de GrafoAcciones (agregar, detalle...) se apilan encima
    // sin tab propio (plan.md#3): ocultar el bottom nav ahi evita que, al
    // llegar desde una seccion distinta a la que las contiene, la pestana
    // activa salte a la seccion "equivocada".
    val enGrafoAcciones = backStackEntry?.destination?.hierarchy?.any { nodo ->
        nodo.hasRoute(GrafoAcciones::class)
    } == true

    Scaffold(
        containerColor = SurfaceLavanda,
        bottomBar = { if (!enGrafoAcciones) BarraNavegacion(navController) },
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

    NavigationBar(
        containerColor = SurfaceCrema,
        tonalElevation = 6.dp,
        modifier = Modifier.border(width = 1.dp, color = Color(0xFFE6DDD3)),
    ) {
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
