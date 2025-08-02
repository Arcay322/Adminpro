package com.example.admin_ingresos.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Fondo glassmorphism con degradados animados y formas abstractas
 */
@Composable
fun GlassmorphismBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    // Animación infinita para el movimiento de las formas
    val infiniteTransition = rememberInfiniteTransition()
    
    val animatedOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val animatedOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val animatedOffset3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Fondo base con degradado principal
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            BackgroundStart,
                            BackgroundMid,
                            BackgroundEnd
                        ),
                        start = Offset(0f, 0f),
                        end = Offset.Infinite
                    )
                )
        )
        
        // Formas abstractas animadas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(60.dp)
        ) {
            drawAnimatedShapes(
                animatedOffset1,
                animatedOffset2,
                animatedOffset3,
                size
            )
        }
        
        // Overlay sutil para suavizar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.1f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.2f)
                        )
                    )
                )
        )
        
        content()
    }
}

private fun DrawScope.drawAnimatedShapes(
    offset1: Float,
    offset2: Float,
    offset3: Float,
    canvasSize: androidx.compose.ui.geometry.Size
) {
    val centerX = canvasSize.width / 2
    val centerY = canvasSize.height / 2
    
    // Forma 1 - Círculo vibrante grande
    val radius1 = 200f
    val x1 = centerX + cos(Math.toRadians(offset1.toDouble())).toFloat() * 100
    val y1 = centerY + sin(Math.toRadians(offset1.toDouble())).toFloat() * 150
    
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                AccentVibrantStart.copy(alpha = 0.3f),
                AccentVibrantEnd.copy(alpha = 0.1f),
                Color.Transparent
            ),
            center = Offset(x1, y1),
            radius = radius1
        ),
        radius = radius1,
        center = Offset(x1, y1)
    )
    
    // Forma 2 - Círculo secundario
    val radius2 = 150f
    val x2 = centerX + cos(Math.toRadians(offset2.toDouble())).toFloat() * 200
    val y2 = centerY + sin(Math.toRadians(offset2.toDouble())).toFloat() * 100
    
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                ExpenseRed.copy(alpha = 0.2f),
                CategoryHealth.copy(alpha = 0.1f),
                Color.Transparent
            ),
            center = Offset(x2, y2),
            radius = radius2
        ),
        radius = radius2,
        center = Offset(x2, y2)
    )
    
    // Forma 3 - Círculo pequeño
    val radius3 = 100f
    val x3 = centerX + cos(Math.toRadians(offset3.toDouble())).toFloat() * 250
    val y3 = centerY + sin(Math.toRadians(offset3.toDouble())).toFloat() * 200
    
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                CategoryEducation.copy(alpha = 0.25f),
                CategoryTravel.copy(alpha = 0.1f),
                Color.Transparent
            ),
            center = Offset(x3, y3),
            radius = radius3
        ),
        radius = radius3,
        center = Offset(x3, y3)
    )
    
    // Formas adicionales para mayor riqueza visual
    val radius4 = 80f
    val x4 = centerX + cos(Math.toRadians(-offset1.toDouble())).toFloat() * 180
    val y4 = centerY + sin(Math.toRadians(-offset1.toDouble())).toFloat() * 120
    
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                IncomeGreen.copy(alpha = 0.2f),
                CategoryShopping.copy(alpha = 0.1f),
                Color.Transparent
            ),
            center = Offset(x4, y4),
            radius = radius4
        ),
        radius = radius4,
        center = Offset(x4, y4)
    )
}

/**
 * Wrapper para aplicar fondo glassmorphism a cualquier pantalla
 */
@Composable
fun GlassmorphismScreen(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    GlassmorphismBackground(modifier = modifier) {
        content()
    }
}
