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

private val DarkColorScheme = darkColorScheme(
    primary = AzulPrimario,
    onPrimary = BlancoOnPrimario,
    primaryContainer = AzulContenedorPrimario
)

private val LightColorScheme = lightColorScheme(
    primary = AzulPrimario,
    onPrimary = BlancoOnPrimario,
    primaryContainer = AzulContenedorPrimario,
    background = FondoApp,
    onBackground = TextoPrincipal,
    surface = FondoTarjeta,
    onSurface = TextoPrincipal,
    surfaceVariant = SuperficieAlterna,
    onSurfaceVariant = TextoSecundario,
    outline = Divisor
)

@Composable
fun EntornoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Paleta de marca fija: no usar dynamic color (ver CLAUDE.md)
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}