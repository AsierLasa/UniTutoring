package com.universitatcarlemany.unitutoring.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Esquema de colores para el tema claro de la aplicación.
 *
 * Utiliza la paleta de colores personalizada de UniTutoring para definir los colores
 * principales, de fondo, superficie y texto.
 */
private val LightColorScheme = lightColorScheme(
    primary = UCTrustBlue,
    secondary = UCActionCoral,
    background = UCBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = UCTextGrey,
    onSurface = UCTextGrey
)

/**
 * Esquema de colores para el tema oscuro de la aplicación.
 *
 * Adapta la paleta de UniTutoring para contextos de baja luminosidad,
 * asegurando un buen contraste y legibilidad.
 */
private val DarkColorScheme = darkColorScheme(
    primary = UCTrustBlueDark,
    secondary = UCActionCoralDark,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0)
)

/**
 * Composable principal que aplica el tema de UniTutoring a la interfaz de usuario.
 *
 * Esta función configura el [MaterialTheme] con los esquemas de color, la tipografía
 * y las formas personalizadas de la aplicación. También gestiona el color de la barra
 * de estado del sistema para una integración visual completa.
 *
 * @param darkTheme Booleano que indica si se debe usar el tema oscuro. Por defecto,
 * se basa en la configuración del sistema.
 * @param dynamicColor Booleano para habilitar los colores dinámicos de Android 12+.
 * Se mantiene en `false` por defecto para preservar la identidad de marca de la app.
 * @param content El contenido Composable al que se le aplicará el tema.
 */
@Composable
fun UniTutoringTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Selecciona el esquema de color apropiado basado en el tema (claro/oscuro)
    // y la preferencia de color dinámico.
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Efecto secundario para cambiar el color de la barra de estado del sistema
    // y el color de sus iconos para que coincida con el tema de la app.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Aplica el MaterialTheme con los valores personalizados.
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Proviene de ui/theme/Typography.kt
        shapes = AppShapes,      // Proviene de ui/theme/Shapes.kt (CORREGIDO)
        content = content
    )
}
