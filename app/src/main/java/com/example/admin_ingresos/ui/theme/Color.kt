package com.example.admin_ingresos.ui.theme

import androidx.compose.ui.graphics.Color

// === SISTEMA DE COLORES GLASSMORPHISM AVANZADO ===

// Background Principal - Degradado Nocturno Profundo
val BackgroundStart = Color(0xFF0D1117) // Azul noche profundo
val BackgroundEnd = Color(0xFF1D1335) // Morado oscuro
val BackgroundMid = Color(0xFF141627) // Punto medio del degradado

// Colores de Acento Vibrantes para CTAs
val AccentVibrantStart = Color(0xFF00FFA3) // Verde lima brillante
val AccentVibrantEnd = Color(0xFF03E1FF) // Cian brillante
val AccentVibrantMid = Color(0xFF00C8D4) // Punto medio del degradado

// Colores Semánticos Modernos
val IncomeGreen = Color(0xFF20BF55) // Verde brillante y positivo
val ExpenseRed = Color(0xFFF45B69) // Rosa enérgico pero no alarmante
val WarningAmber = Color(0xFFFFB300) // Naranja/amarillo intenso

// Glassmorphism - Superficies de Vidrio
val GlassWhite = Color(0x26FFFFFF) // Blanco 15% opacidad
val GlassWhiteSubtle = Color(0x1AFFFFFF) // Blanco 10% opacidad 
val GlassWhiteStrong = Color(0x33FFFFFF) // Blanco 20% opacidad
val GlassBlur = Color(0x0DFFFFFF) // Para efectos de blur

// Bordes de Vidrio
val GlassBorder = Color(0x33FFFFFF) // Borde translúcido 20%
val GlassBorderSubtle = Color(0x1AFFFFFF) // Borde sutil 10%

// Texto sobre Vidrio
val TextPrimary = Color(0xFFF0F0F0) // Blanco casi puro
val TextSecondary = Color(0xFFA8A8A8) // Gris claro para subtítulos
val TextOnAccent = Color(0xFF000000) // Negro para texto sobre acentos vibrantes

// Categorías con Colores Vibrantes Armónicos
val CategoryFood = Color(0xFFFF6B6B) // Rojo coral
val CategoryTransport = Color(0xFF4ECDC4) // Turquesa
val CategoryEntertainment = Color(0xFFFFE66D) // Amarillo suave
val CategoryHealth = Color(0xFF95E1D3) // Verde menta
val CategoryShopping = Color(0xFFA8E6CF) // Verde claro
val CategoryUtilities = Color(0xFFFFD93D) // Amarillo brillante
val CategoryEducation = Color(0xFF6C5CE7) // Violeta
val CategoryTravel = Color(0xFF74B9FF) // Azul cielo
val CategoryOther = Color(0xFFFD79A8) // Rosa

// Efectos Interactivos
val HoverEffect = Color(0x1AFFFFFF) // Para estados hover
val PressedEffect = Color(0x33FFFFFF) // Para estados pressed
val SelectedEffect = Color(0x26FFFFFF) // Para elementos seleccionados

// Superficies y Fondos
val SurfaceGlass = Color(0x26FFFFFF) // Superficie principal de vidrio
val SurfaceVariant = Color(0x1AFFFFFF) // Superficie secundaria
val Background = BackgroundStart // Fondo principal
val OnSurface = TextPrimary // Texto sobre superficie
val OnSurfaceVariant = TextSecondary // Texto secundario
val OnBackground = TextPrimary // Texto sobre fondo

// Esquema de colores para Material Theme
val Primary = AccentVibrantStart
val PrimaryContainer = GlassWhiteStrong  
val OnPrimary = TextOnAccent
val OnPrimaryContainer = TextPrimary

val Secondary = AccentVibrantEnd
val SecondaryContainer = GlassWhite
val OnSecondary = TextOnAccent
val OnSecondaryContainer = TextPrimary

val Error = ExpenseRed
val OnError = TextPrimary
val ErrorContainer = Color(0x33F45B69)
val OnErrorContainer = TextPrimary

val Outline = GlassBorderSubtle
val OutlineVariant = GlassBorder

// Legacy compatibility colors manteniendo compatibilidad
val Success = IncomeGreen
val SuccessGlass = Color(0x3320BF55)
val SuccessContainer = Color(0x3320BF55)
val OnSuccessContainer = TextPrimary

val Warning = WarningAmber
val WarningGlass = Color(0x33FFB300)
val WarningContainer = Color(0x33FFB300)
val OnWarningContainer = TextPrimary

val Info = Color(0xFF03E1FF)
val InfoGlass = Color(0x3303E1FF)
val InfoContainer = Color(0x3303E1FF)
val OnInfoContainer = TextPrimary

// Colores adicionales para compatibilidad
val Gray900 = Color(0xFF0D1117)
val Gray800 = Color(0xFF1D1335)
val Gray700 = Color(0xFF374151)
val Gray600 = Color(0xFF4B5563)
val Gray500 = Color(0xFF6B7280)
val Gray400 = Color(0xFF9CA3AF)
val Gray300 = Color(0xFFD1D5DB)
val Gray200 = Color(0xFFE5E7EB)
val Gray100 = Color(0xFFF3F4F6)
val Gray50 = Color(0xFFF9FAFB)

// === LEGACY COMPATIBILITY COLORS ===
// Añadimos colores para mantener compatibilidad con código existente

// Colores principales legacy
val CashFlowPrimary = AccentVibrantStart
val CashFlowPrimaryDark = AccentVibrantEnd
val CashFlowSecondary = Color(0xFF74B9FF)
val TertiaryPurple = Color(0xFF6C5CE7)

// Gradientes legacy
val BackgroundGradientStart = BackgroundStart
val BackgroundGradientEnd = BackgroundEnd
val GradientStart = BackgroundStart
val GradientMiddle = BackgroundMid
val GradientEnd = BackgroundEnd

// Superficies de vidrio legacy
val GlassSurface1 = GlassWhite
val GlassSurface2 = GlassWhiteSubtle
val GlassSurface3 = GlassWhiteStrong
val GlassSurface4 = GlassBlur

// === CATEGORY COLORS ===
// Colores para categorías de transacciones (eliminadas las duplicadas)

// Colores para alertas y estados
val InfoBlue = Color(0xFF3B82F6)
val SuccessGreen = IncomeGreen