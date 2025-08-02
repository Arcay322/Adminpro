package com.example.admin_ingresos.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Glassmorphism Avanzado - Esquema de Colores Principal
private val GlassmorphismColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = AccentVibrantStart,
    onTertiary = TextOnAccent,
    tertiaryContainer = GlassWhiteStrong,
    onTertiaryContainer = TextPrimary,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = SurfaceGlass,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
    inverseSurface = TextPrimary,
    inverseOnSurface = Background,
    inversePrimary = AccentVibrantEnd
)



@Composable
fun CashFlowTheme(
    darkTheme: Boolean = true, // Forzamos dark theme para glassmorphism
    dynamicColor: Boolean = false, // Deshabilitamos colores dinámicos
    content: @Composable () -> Unit
) {
    // Siempre usamos el esquema glassmorphism
    val colorScheme = GlassmorphismColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Usar color de fondo nocturno para la barra de estado
            window.statusBarColor = BackgroundStart.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CashFlowTypography,
        shapes = CashFlowShapes,
        content = content
    )
}

// Alias para compatibilidad - siempre usa glassmorphism
@Composable
fun Admin_ingresosTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CashFlowTheme(
        darkTheme = true,
        dynamicColor = false,
        content = content
    )
}