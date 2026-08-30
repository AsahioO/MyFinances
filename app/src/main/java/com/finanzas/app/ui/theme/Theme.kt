package com.finanzas.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle

/**
 * ColorScheme armado a mano a partir de los tokens fijos. NO se usa
 * dynamicColor / Material You en ningun punto: la paleta la define el diseno de
 * la app y el color carga significado (violeta = automatico, mostaza = manual),
 * asi que derivarla del wallpaper la romperia.
 */
private val EsquemaFinanzas: ColorScheme = lightColorScheme(
    primary = Violeta,
    onPrimary = SurfaceCrema,
    primaryContainer = SurfaceLavanda,
    onPrimaryContainer = Ink,
    inversePrimary = Violeta,

    secondary = Mostaza,
    onSecondary = Ink,
    secondaryContainer = SurfaceLavanda,
    onSecondaryContainer = Ink,

    tertiary = Menta,
    onTertiary = SurfaceCrema,
    tertiaryContainer = SurfaceLavanda,
    onTertiaryContainer = Ink,

    background = SurfaceCrema,
    onBackground = TextoPrincipal,

    surface = SurfaceCrema,
    onSurface = TextoPrincipal,
    surfaceVariant = SurfaceLavanda,
    onSurfaceVariant = TextoSecundario,
    surfaceTint = Violeta,

    surfaceContainerLowest = SurfaceCrema,
    surfaceContainerLow = SurfaceCrema,
    surfaceContainer = SurfaceLavanda,
    surfaceContainerHigh = SurfaceLavanda,
    surfaceContainerHighest = SurfaceLavanda,
    surfaceBright = SurfaceCrema,
    surfaceDim = SurfaceLavanda,

    inverseSurface = Ink,
    inverseOnSurface = SurfaceCrema,

    error = Coral,
    onError = SurfaceCrema,
    errorContainer = SurfaceLavanda,
    onErrorContainer = Ink,

    outline = TextoSecundario,
    outlineVariant = SurfaceLavanda,
    scrim = Ink,
)

private val ColoresSemanticosFinanzas = ColoresSemanticos()

/** Estilos de monto, que no tienen rol equivalente en Material 3. */
data class TipografiaMonto(
    val grande: TextStyle = MontoGrande,
    val mediano: TextStyle = MontoMediano,
    val pequeno: TextStyle = MontoPequeno,
)

private val LocalTipografiaMonto = staticCompositionLocalOf { TipografiaMonto() }

@Composable
fun AppFinanzasTheme(content: @Composable () -> Unit) {
    // Un solo esquema, sin variante oscura ni dinamica: la paleta es fija.
    CompositionLocalProvider(
        LocalColoresSemanticos provides ColoresSemanticosFinanzas,
        LocalTipografiaMonto provides TipografiaMonto(),
    ) {
        MaterialTheme(
            colorScheme = EsquemaFinanzas,
            typography = TipografiaFinanzas,
            content = content,
        )
    }
}

/** Acceso corto a los extras del tema: `FinanzasTheme.colores`, `FinanzasTheme.monto`. */
object FinanzasTheme {
    val colores: ColoresSemanticos
        @Composable @ReadOnlyComposable get() = LocalColoresSemanticos.current

    val monto: TipografiaMonto
        @Composable @ReadOnlyComposable get() = LocalTipografiaMonto.current
}
