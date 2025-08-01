package com.example.admin_ingresos.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.admin_ingresos.R

// Enhanced CashFlow Typography with Inter font family
val CashFlowFontFamily = FontFamily(
    Font(R.font.inter_thin, FontWeight.Thin),
    Font(R.font.inter_extralight, FontWeight.ExtraLight),
    Font(R.font.inter_light, FontWeight.Light),
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold),
    Font(R.font.inter_black, FontWeight.Black),
    
    // Italic variants
    Font(R.font.inter_thinitalic, FontWeight.Thin, FontStyle.Italic),
    Font(R.font.inter_extralightitalic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.inter_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.inter_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.inter_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.inter_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.inter_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.inter_extrabolditalic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(R.font.inter_blackitalic, FontWeight.Black, FontStyle.Italic)
)

// Display font family for headers and emphasis
val CashFlowDisplayFontFamily = FontFamily(
    Font(R.font.interdisplay_thin, FontWeight.Thin),
    Font(R.font.interdisplay_extralight, FontWeight.ExtraLight),
    Font(R.font.interdisplay_light, FontWeight.Light),
    Font(R.font.interdisplay_regular, FontWeight.Normal),
    Font(R.font.interdisplay_medium, FontWeight.Medium),
    Font(R.font.interdisplay_semibold, FontWeight.SemiBold),
    Font(R.font.interdisplay_bold, FontWeight.Bold),
    Font(R.font.interdisplay_extrabold, FontWeight.ExtraBold),
    Font(R.font.interdisplay_black, FontWeight.Black)
)

// Enhanced Typography with financial context
val CashFlowTypography = Typography(
    // Display styles for headers and main titles
    displayLarge = TextStyle(
        fontFamily = CashFlowDisplayFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = CashFlowDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = CashFlowDisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    
    // Headline styles for section headers
    headlineLarge = TextStyle(
        fontFamily = CashFlowDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = CashFlowDisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = CashFlowDisplayFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    
    // Title styles for cards and components
    titleLarge = TextStyle(
        fontFamily = CashFlowFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = CashFlowFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = CashFlowFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    
    // Body text styles
    bodyLarge = TextStyle(
        fontFamily = CashFlowFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = CashFlowFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = CashFlowFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    
    // Label styles for buttons and form elements
    labelLarge = TextStyle(
        fontFamily = CashFlowFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = CashFlowFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = CashFlowFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

// Financial specific text styles
object FinancialTextStyles {
    val CurrencyLarge = TextStyle(
        fontFamily = CashFlowDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
    )
    
    val CurrencyMedium = TextStyle(
        fontFamily = CashFlowFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    )
    
    val CurrencySmall = TextStyle(
        fontFamily = CashFlowFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
    )
    
    val PercentageText = TextStyle(
        fontFamily = CashFlowFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    )
    
    val TransactionTitle = TextStyle(
        fontFamily = CashFlowFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    )
    
    val TransactionSubtitle = TextStyle(
        fontFamily = CashFlowFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    )
}
