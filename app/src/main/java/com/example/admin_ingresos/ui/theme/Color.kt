package com.example.admin_ingresos.ui.theme

import androidx.compose.ui.graphics.Color

// === SISTEMA DE COLORES GLASSMORPHISM AVANZADO ===

// Background Principal - Unico fondo negro para toda la app
val BackgroundStart = Color(0xFF000000) // Negro puro
val BackgroundEnd = Color(0xFF000000) // Negro puro
val BackgroundMid = Color(0xFF000000) // Negro puro

// Colores de Acento Vibrantes para CTAs
val AccentVibrantStart = Color(0xFF00FFA3) // Verde lima brillante
val AccentVibrantEnd = Color(0xFF03E1FF) // Cian brillante
val AccentVibrantMid = Color(0xFF00C8D4) // Punto medio del degradado

// Colores Semánticos Modernos
// Valores solicitados para montos (se aplican independientemente del tema)
val IncomeGreen = Color(0xFF6BFF6B) // Verde para valores positivos (ingresos)
val ExpenseRed = Color(0xFFFF5B5B) // Rojo para valores negativos (gastos)
val WarningAmber = Color(0xFFFFB300) // Naranja/amarillo intenso

// Glassmorphism - Superficies de Vidrio (blanco translúcido, preserve glass effect)
var GlassWhite = Color(0x26FFFFFF) // Blanco 15% opacidad
var GlassWhiteSubtle = Color(0x1AFFFFFF) // Blanco 10% opacidad
var GlassWhiteStrong = Color(0x33FFFFFF) // Blanco 20% opacidad
var GlassBlur = Color(0x0DFFFFFF) // Para efectos de blur

// Bordes de Vidrio
var GlassBorder = Color(0x33FFFFFF) // Borde translúcido 20%
var GlassBorderSubtle = Color(0x1AFFFFFF) // Borde sutil 10%

// Texto sobre Vidrio
// Texto principal y secundario (aumentamos contraste para mayor claridad)
var TextPrimary = Color(0xFFFFFFFF) // Blanco puro para máxima legibilidad
var TextSecondary = Color(0xFFBFC7CC) // Gris claro más brillante para subtítulos
var TextOnAccent = Color(0xFF000000) // Negro para texto sobre acentos vibrantes

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
var SurfaceGlass = Color(0x26FFFFFF) // Superficie principal de vidrio
var SurfaceVariant = Color(0x1AFFFFFF) // Superficie secundaria
var Background = BackgroundStart // Fondo principal
var OnSurface = TextPrimary // Texto sobre superficie
var OnSurfaceVariant = TextSecondary // Texto secundario
var OnBackground = TextPrimary // Texto sobre fondo

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

// Simple helper para mapear nombres de categoría a colores definidos arriba
fun getCategoryColor(categoryName: String?): Color {
	val n = categoryName?.lowercase()?.trim() ?: ""
	return when {
		n.contains("comida") || n.contains("aliment") || n.contains("restaur") || n.contains("supermerc") -> CategoryFood
		n.contains("transporte") || n.contains("taxi") || n.contains("uber") || n.contains("gasolina") -> CategoryTransport
		n.contains("entretenimiento") || n.contains("ocio") || n.contains("cine") -> CategoryEntertainment
		n.contains("salud") || n.contains("farmacia") || n.contains("médic") -> CategoryHealth
		n.contains("compra") || n.contains("compras") || n.contains("ropa") || n.contains("shopping") -> CategoryShopping
		n.contains("hogar") || n.contains("servicios") || n.contains("luz") || n.contains("agua") -> CategoryUtilities
		n.contains("educacion") || n.contains("estudi") || n.contains("curso") -> CategoryEducation
		n.contains("viaje") || n.contains("vacacion") || n.contains("turismo") -> CategoryTravel
		n.isBlank() -> CategoryOther
		else -> CategoryOther
	}
}