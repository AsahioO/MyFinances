package com.finanzas.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.finanzas.app.R

/**
 * Downloadable Fonts: las tres familias las sirve Google Play Services, asi que
 * no viven como .ttf dentro del APK. Si el proveedor no esta disponible, Compose
 * cae a la familia de respaldo declarada en cada FontFamily.
 */
private val proveedorGoogleFonts = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.finanzas_fonts_certs,
)

/**
 * Mientras la fuente descarga (o si el dispositivo no trae el proveedor), Compose
 * resuelve al tipo de letra por defecto de la plataforma en vez de fallar.
 */
private fun familiaDescargable(nombre: String): FontFamily {
    val fuente = GoogleFont(nombre)
    return FontFamily(
        Font(googleFont = fuente, fontProvider = proveedorGoogleFonts, weight = FontWeight.Normal),
        Font(googleFont = fuente, fontProvider = proveedorGoogleFonts, weight = FontWeight.Medium),
        Font(googleFont = fuente, fontProvider = proveedorGoogleFonts, weight = FontWeight.SemiBold),
        Font(googleFont = fuente, fontProvider = proveedorGoogleFonts, weight = FontWeight.Bold),
    )
}

/** Titulos y headers de seccion. */
val FamiliaTitulos: FontFamily = familiaDescargable("Space Grotesk")

/** Montos y cifras: monoespaciada, para que se lean como libro contable. */
val FamiliaMonto: FontFamily = familiaDescargable("IBM Plex Mono")

/** Cuerpo de texto y UI general. */
val FamiliaCuerpo: FontFamily = familiaDescargable("Inter")

private val base = Typography()

val TipografiaFinanzas = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = FamiliaTitulos),
    displayMedium = base.displayMedium.copy(fontFamily = FamiliaTitulos),
    displaySmall = base.displaySmall.copy(fontFamily = FamiliaTitulos),
    headlineLarge = base.headlineLarge.copy(fontFamily = FamiliaTitulos),
    headlineMedium = base.headlineMedium.copy(fontFamily = FamiliaTitulos),
    headlineSmall = base.headlineSmall.copy(fontFamily = FamiliaTitulos),
    titleLarge = base.titleLarge.copy(fontFamily = FamiliaTitulos),
    titleMedium = base.titleMedium.copy(fontFamily = FamiliaTitulos),
    titleSmall = base.titleSmall.copy(fontFamily = FamiliaTitulos),
    bodyLarge = base.bodyLarge.copy(fontFamily = FamiliaCuerpo),
    bodyMedium = base.bodyMedium.copy(fontFamily = FamiliaCuerpo),
    bodySmall = base.bodySmall.copy(fontFamily = FamiliaCuerpo),
    labelLarge = base.labelLarge.copy(fontFamily = FamiliaCuerpo),
    labelMedium = base.labelMedium.copy(fontFamily = FamiliaCuerpo),
    labelSmall = base.labelSmall.copy(fontFamily = FamiliaCuerpo),
)

// Los montos no caben en ningun rol de Material: se exponen aparte.

val MontoGrande = TextStyle(
    fontFamily = FamiliaMonto,
    fontWeight = FontWeight.Bold,
    fontSize = 40.sp,
    lineHeight = 44.sp,
)

val MontoMediano = TextStyle(
    fontFamily = FamiliaMonto,
    fontWeight = FontWeight.SemiBold,
    fontSize = 22.sp,
    lineHeight = 26.sp,
)

val MontoPequeno = TextStyle(
    fontFamily = FamiliaMonto,
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    lineHeight = 20.sp,
)
