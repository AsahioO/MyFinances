package com.finanzas.app.ui.common

import android.graphics.Color as ColorAndroid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Traduce el nombre de icono guardado en Room (CategoriaEntity.icono, sembrado
 * por CATEGORIAS_SEMILLA en Migraciones.kt) al ImageVector real de Material.
 * Un nombre desconocido (categoria creada a mano en el futuro con un icono
 * que no mapea) cae a un icono generico en vez de romper la UI.
 */
private val ICONOS_CATEGORIA: Map<String, ImageVector> = mapOf(
    "Restaurant" to Icons.Filled.Restaurant,
    "DirectionsCar" to Icons.Filled.DirectionsCar,
    "Bolt" to Icons.Filled.Bolt,
    "Home" to Icons.Filled.Home,
    "LocalHospital" to Icons.Filled.LocalHospital,
    "SportsEsports" to Icons.Filled.SportsEsports,
    "ShoppingBag" to Icons.Filled.ShoppingBag,
    "School" to Icons.Filled.School,
    "Subscriptions" to Icons.Filled.Subscriptions,
    "Category" to Icons.Filled.Category,
)

fun iconoCategoria(nombreIcono: String?): ImageVector =
    ICONOS_CATEGORIA[nombreIcono] ?: Icons.Filled.Category

/** Parsea el hex opcional de CategoriaEntity.color; cae a [porDefecto] si es nulo o invalido. */
fun colorCategoria(hex: String?, porDefecto: Color): Color =
    hex?.let { runCatching { Color(ColorAndroid.parseColor(it)) }.getOrNull() } ?: porDefecto

/**
 * Variante "fondo de tarjeta" del color de una categoria: el mismo tono
 * mezclado hacia blanco, para superficies suaves donde el color de acento
 * (icono, pill, sparkline) va encima. [intensidad] es la proporcion del color
 * base en la mezcla (0.10-0.15 es sutil; valores mayores refuerzan jerarquia,
 * p. ej. la tarjeta de mayor gasto en Top Movers).
 */
fun fondoCategoria(base: Color, intensidad: Float = 0.12f): Color =
    lerp(Color.White, base, intensidad.coerceIn(0f, 1f))
