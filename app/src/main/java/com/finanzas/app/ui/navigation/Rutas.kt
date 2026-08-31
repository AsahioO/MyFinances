package com.finanzas.app.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Rutas tipadas. No existe ni una sola ruta como string literal en la app:
 * navegar con un objeto o data class hace que el compilador cache los errores
 * de destino y de argumentos.
 *
 * Cada feature tiene su propio grafo anidado (el objeto `Grafo*`) para que
 * agregar una seccion nueva sea un grafo nuevo, sin tocar los existentes.
 */

// --- Inicio ---

@Serializable
data object GrafoInicio

@Serializable
data object RutaInicio

// --- Movimientos ---

@Serializable
data object GrafoMovimientos

@Serializable
data object RutaMovimientos

// --- Reportes ---

@Serializable
data object GrafoReportes

@Serializable
data object RutaReportes

// --- Ajustes ---

@Serializable
data object GrafoAjustes

@Serializable
data object RutaAjustes

// --- Acciones ---
// Pantallas secundarias que "se apilan encima" (plan.md#3): no tienen tab
// propio en el bottom nav y son alcanzables desde mas de una seccion (p. ej.
// agregar se abre desde Inicio y desde el FAB de Movimientos), asi que viven
// en su propio grafo en vez de anidarse "dentro" de una sola seccion.

@Serializable
data object GrafoAcciones

/**
 * Formulario de alta manual. Con [montoCentavos]/[comercio]/[fechaMillis] en
 * -1/null llega vacio (entrada normal); con datos llega prellenado (handoff
 * desde OCR). -1L es centinela de "sin valor": Navigation Compose no soporta
 * Long? nativo en rutas tipadas.
 */
@Serializable
data class RutaAgregarManual(
    val montoCentavos: Long = -1L,
    val comercio: String? = null,
    val fechaMillis: Long = -1L,
)

/**
 * Detalle de un movimiento (plan.md pantalla 5): ver/editar categoria, notas
 * y estado; alcanzable desde Inicio y Movimientos, por eso vive en GrafoAcciones.
 */
@Serializable
data class RutaDetalleMovimiento(val movimientoId: Long)
