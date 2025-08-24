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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.admin_ingresos.ui.theme.AppThemeManager
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Glassmorphism Avanzado - Esquema de Colores Principal
// Build the dark color scheme on-demand so it reads the current dynamic
// legacy tokens (TextPrimary, OnBackground, SurfaceGlass, etc.) at runtime.
private fun buildGlassmorphismColorScheme() = darkColorScheme(
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

// Light mode color scheme requested by user
private val CashFlowLightColorScheme = lightColorScheme(
    primary = Color(0xFF00BFA5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCF3E6),
    onPrimaryContainer = Color(0xFF004D3F),
    secondary = Color(0xFF4DB6AC),
    onSecondary = Color.White,
    tertiary = Color(0xFFFF8A65),
    onTertiary = Color.White,
    background = Color(0xFFF0F2F5),
    onBackground = Color(0xFF1F1F1F),
    surface = Color.White,
    onSurface = Color(0xFF1F1F1F),
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF616161),
    outline = Color(0xFFBDBDBD),
    error = Color(0xFFB00020),
    onError = Color.White
)



@Composable
fun CashFlowTheme(
    isDarkTheme: Boolean = true,
    backgroundColor: Color? = null,
    content: @Composable () -> Unit
) {
    // Use the centralized AppThemeManager as the single source of truth so
    // all compositions observe the same light/dark flag and avoid hybrid
    // states where parts render with different schemes.
    val forceLight by AppThemeManager.forceLight.collectAsState()
    val resolvedIsDark = !forceLight

    // Select color scheme based on resolvedIsDark
    val colorScheme = if (resolvedIsDark) {
        // Explicit dark scheme: avoid reading mutable legacy tokens here so
        // MaterialTheme receives stable dark values. We'll remap legacy
        // tokens below from this canonical scheme.
        val bg = backgroundColor ?: BackgroundStart
        darkColorScheme(
            primary = AccentVibrantStart,
            onPrimary = Color(0xFF000000),
            primaryContainer = Color(0x33FFFFFF),
            onPrimaryContainer = Color(0xFFFFFFFF),
            secondary = AccentVibrantEnd,
            onSecondary = Color(0xFF000000),
            secondaryContainer = Color(0x26FFFFFF),
            onSecondaryContainer = Color(0xFFFFFFFF),
            tertiary = AccentVibrantStart,
            onTertiary = Color(0xFF000000),
            tertiaryContainer = Color(0x33FFFFFF),
            onTertiaryContainer = Color(0xFFFFFFFF),
            error = ExpenseRed,
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0x33F45B69),
            onErrorContainer = Color(0xFFFFFFFF),
            background = bg,
            onBackground = Color(0xFFFFFFFF),
            surface = Color(0x26FFFFFF),
            onSurface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0x1AFFFFFF),
            onSurfaceVariant = Color(0xFFBFC7CC),
            outline = Color(0x33FFFFFF),
            outlineVariant = Color(0x1AFFFFFF),
            inverseSurface = Color(0xFFFFFFFF),
            inverseOnSurface = bg,
            inversePrimary = AccentVibrantEnd
        )
    } else {
        // Light scheme - ignore glassmorphism tokens
        val base = CashFlowLightColorScheme
        // If a custom backgroundColor is provided, use it, otherwise use the light background
        val bg = backgroundColor ?: base.background
        base.copy(
            background = bg,
            onBackground = base.onBackground
        )
    }

    // Update global legacy tokens so existing UI that references them updates immediately
    // Remap only the primary token from the user preference so components that read
    // MaterialTheme.colorScheme.primary use the selected color while preserving
    // onPrimary and the light/dark contrast logic.
    val appPrimaryInt by AppThemeManager.primaryColor.collectAsState()
    val appPrimaryColor = Color(appPrimaryInt)
    val remappedColorScheme = colorScheme.copy(primary = appPrimaryColor)

    GlassWhite = remappedColorScheme.surface.copy(alpha = 0.18f)
    GlassWhiteSubtle = remappedColorScheme.surface.copy(alpha = 0.10f)
    GlassWhiteStrong = remappedColorScheme.surface.copy(alpha = 0.22f)
    GlassBlur = remappedColorScheme.surface.copy(alpha = 0.06f)

    GlassBorder = remappedColorScheme.outline
    GlassBorderSubtle = remappedColorScheme.outline.copy(alpha = 0.6f)

    TextPrimary = remappedColorScheme.onBackground
    TextSecondary = remappedColorScheme.onSurfaceVariant
    TextOnAccent = remappedColorScheme.onPrimary

    SurfaceGlass = remappedColorScheme.surface
    SurfaceVariant = remappedColorScheme.surfaceVariant
    Background = remappedColorScheme.background
    OnSurface = remappedColorScheme.onSurface
    OnSurfaceVariant = remappedColorScheme.onSurfaceVariant
    OnBackground = remappedColorScheme.onBackground

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Use provided background color for status & navigation bars when available
            val bg = (backgroundColor ?: remappedColorScheme.background).toArgb()
            window.statusBarColor = bg
            window.navigationBarColor = bg
            val insetsController = WindowCompat.getInsetsController(window, view)
            // For light theme we want dark status bar icons
            insetsController.isAppearanceLightStatusBars = !resolvedIsDark
            try {
                insetsController.isAppearanceLightNavigationBars = !resolvedIsDark
            } catch (_: Exception) {
                // ignore
            }
        }
    }

    MaterialTheme(
        colorScheme = remappedColorScheme,
        typography = CashFlowTypography,
        shapes = CashFlowShapes,
        content = content
    )
}

// Alias para compatibilidad - siempre usa glassmorphism
@Composable
fun Admin_ingresosTheme(
    isDarkTheme: Boolean = true,
    backgroundColor: Color? = null,
    content: @Composable () -> Unit
) {
    CashFlowTheme(
        isDarkTheme = isDarkTheme,
        backgroundColor = backgroundColor,
        content = content
    )
}