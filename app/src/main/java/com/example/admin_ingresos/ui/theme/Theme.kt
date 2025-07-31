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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Indigo500,
    onPrimary = Color.White,
    primaryContainer = Indigo700,
    onPrimaryContainer = Indigo100,
    secondary = Green600,
    onSecondary = Color.White,
    secondaryContainer = Green700,
    onSecondaryContainer = Green100,
    tertiary = Orange500,
    onTertiary = Color.White,
    error = Red500,
    onError = Color.White,
    errorContainer = Red600,
    onErrorContainer = Red100,
    background = BackgroundDark,
    onBackground = Gray100,
    surface = SurfaceDark,
    onSurface = Gray100,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Gray300,
    outline = Gray600,
    outlineVariant = Gray700
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo500,
    onPrimary = Color.White,
    primaryContainer = Indigo100,
    onPrimaryContainer = Indigo700,
    secondary = Green600,
    onSecondary = Color.White,
    secondaryContainer = Green100,
    onSecondaryContainer = Green700,
    tertiary = Orange500,
    onTertiary = Color.White,
    error = Red500,
    onError = Color.White,
    errorContainer = Red100,
    onErrorContainer = Red600,
    background = BackgroundLight,
    onBackground = Gray900,
    surface = SurfaceLight,
    onSurface = Gray900,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Gray700,
    outline = Gray400,
    outlineVariant = Gray200
)

@Composable
fun Admin_ingresosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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