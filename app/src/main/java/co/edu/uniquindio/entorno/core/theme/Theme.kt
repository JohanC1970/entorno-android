package co.edu.uniquindio.entorno.core.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val LightColorScheme = lightColorScheme(
    primary = PrimarioClaro,
    onPrimary = OnPrimarioClaro,
    primaryContainer = ContenedorPrimarioClaro,
    onPrimaryContainer = OnContenedorPrimarioClaro,
    background = FondoClaro,
    onBackground = OnFondoClaro,
    surface = SuperficieClaro,
    onSurface = OnSuperficieClaro,
    surfaceVariant = SuperficieVarianteClaro,
    onSurfaceVariant = OnSuperficieVarianteClaro,
    outline = ContornoClaro,
    outlineVariant = ContornoVarianteClaro
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimarioOscuro,
    onPrimary = OnPrimarioOscuro,
    primaryContainer = ContenedorPrimarioOscuro,
    onPrimaryContainer = OnContenedorPrimarioOscuro,
    background = FondoOscuro,
    onBackground = OnFondoOscuro,
    surface = SuperficieOscuro,
    onSurface = OnSuperficieOscuro,
    surfaceVariant = SuperficieVarianteOscuro,
    onSurfaceVariant = OnSuperficieVarianteOscuro,
    outline = ContornoOscuro,
    outlineVariant = ContornoVarianteOscuro
)

/**
 * Indica si la app está en oscuro según el tema ELEGIDO, no el del sistema.
 * isSystemInDarkTheme() no sirve para esto: ignora la preferencia del usuario.
 */
val LocalDarkTheme = staticCompositionLocalOf { false }

@Composable
fun EntornoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Con true, Material You (Android 12+) reemplaza nuestra paleta de marca
    // por colores extraídos del fondo de pantalla del usuario. Por eso el
    // valor por defecto es false: todos los usuarios ven la misma identidad
    // visual definida en la Fase 1.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }

}