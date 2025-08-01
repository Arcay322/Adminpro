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

// CashFlow Dark Color Scheme
private val CashFlowDarkColorScheme = darkColorScheme(
    primary = CashFlowPrimary,
    onPrimary = OnPrimary,
    primaryContainer = CashFlowPrimaryDark,
    onPrimaryContainer = CashFlowPrimaryLight,
    secondary = CashFlowSecondary,
    onSecondary = OnSecondary,
    secondaryContainer = CashFlowSecondaryDark,
    onSecondaryContainer = CashFlowSecondaryLight,
    tertiary = TertiaryPurple,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryPurpleDark,
    onTertiaryContainer = TertiaryPurpleLight,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    inverseSurface = OnBackgroundDark,
    inverseOnSurface = BackgroundDark,
    inversePrimary = CashFlowPrimary
)

// CashFlow Light Color Scheme
private val CashFlowLightColorScheme = lightColorScheme(
    primary = CashFlowPrimary,
    onPrimary = OnPrimary,
    primaryContainer = CashFlowPrimaryLight,
    onPrimaryContainer = CashFlowPrimaryDark,
    secondary = CashFlowSecondary,
    onSecondary = OnSecondary,
    secondaryContainer = CashFlowSecondaryLight,
    onSecondaryContainer = CashFlowSecondaryDark,
    tertiary = TertiaryPurple,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryPurpleLight,
    onTertiaryContainer = TertiaryPurpleDark,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    inverseSurface = OnBackgroundLight,
    inverseOnSurface = BackgroundLight,
    inversePrimary = OnPrimary
)

@Composable
fun CashFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CashFlowDarkColorScheme
        else -> CashFlowLightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CashFlowTypography,
        shapes = CashFlowShapes,
        content = content
    )
}

// Alias para compatibilidad
@Composable
fun Admin_ingresosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    CashFlowTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}