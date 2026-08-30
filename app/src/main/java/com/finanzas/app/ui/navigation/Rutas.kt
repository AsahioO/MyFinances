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
