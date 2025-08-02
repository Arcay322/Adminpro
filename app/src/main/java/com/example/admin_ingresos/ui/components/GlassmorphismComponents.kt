package com.example.admin_ingresos.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin_ingresos.ui.theme.*

/**
 * Modificador base para efectos Glassmorphism siguiendo la guía de diseño
 */
fun Modifier.glassmorphism(
    blurRadius: androidx.compose.ui.unit.Dp = 12.dp,
    backgroundColor: Color = GlassWhite,
    borderColor: Color = GlassBorder,
    cornerRadius: androidx.compose.ui.unit.Dp = 16.dp,
    borderWidth: androidx.compose.ui.unit.Dp = 1.dp
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor)
    .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))

/**
 * Tarjeta de vidrio base siguiendo el patrón Glassmorphism
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 20.dp,
    blurRadius: androidx.compose.ui.unit.Dp = 12.dp,
    backgroundColor: Color = GlassWhite,
    borderColor: Color = GlassBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else Modifier

    Box(
        modifier = modifier
            .then(clickableModifier)
            .glassmorphism(
                blurRadius = blurRadius,
                backgroundColor = backgroundColor,
                borderColor = borderColor,
                cornerRadius = cornerRadius
            )
            .padding(20.dp)
    ) {
        content()
    }
}

/**
 * Tarjeta de balance con efecto glassmorphism avanzado
 */
@Composable 
fun AdvancedBalanceCard(
    title: String,
    amount: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(AccentVibrantStart, AccentVibrantEnd),
    onClick: (() -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    GlassCard(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth(),
        cornerRadius = 24.dp,
        backgroundColor = GlassWhiteStrong,
        onClick = {
            isPressed = !isPressed
            onClick?.invoke()
        }
    ) {
        // Fondo con degradado sutil
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors.map { it.copy(alpha = 0.1f) }
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header con icono
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = AccentVibrantStart,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Monto principal
            Text(
                text = amount,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                lineHeight = 36.sp
            )
            
            // Subtítulo
            subtitle?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }
        
        // Efecto de brillo en la esquina
        Box(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = (-20).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        radius = 60f
                    )
                )
        )
    }
}

/**
 * Tarjeta de métrica con diseño glassmorphism
 */
@Composable
fun AdvancedMetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    color: Color = AccentVibrantStart,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    var isHovered by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (isHovered) color.copy(alpha = 0.6f) else GlassBorder,
        animationSpec = tween(300)
    )

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = borderColor,
        backgroundColor = GlassWhite,
        onClick = {
            isHovered = !isHovered
            onClick?.invoke()
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    maxLines = 1,
                    softWrap = false
                )
                
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    softWrap = false
                )
                
                subtitle?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
            
            // Icono con fondo glassmorphism - tamaño fijo para simetría
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color.copy(alpha = 0.2f))
                    .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Botón flotante de acción con degradado vibrante
 */
@Composable
fun VibrantFAB(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 64.dp
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    FloatingActionButton(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier
            .size(size)
            .scale(scale),
        containerColor = Color.Transparent,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 12.dp,
            pressedElevation = 16.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(AccentVibrantStart, AccentVibrantEnd)
                    ),
                    shape = FloatingActionButtonDefaults.largeShape
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = FloatingActionButtonDefaults.largeShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextOnAccent,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Reset pressed state
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}

/**
 * Segmented Button para tipo de transacción (Ingreso/Gasto)
 */
@Composable
fun ModernSegmentedButton(
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    optionColors: Map<String, Color> = mapOf(
        "Ingreso" to IncomeGreen,
        "Gasto" to ExpenseRed
    )
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassWhiteSubtle)
            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(20.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) {
                    optionColors[option]?.copy(alpha = 0.9f) ?: AccentVibrantStart
                } else Color.Transparent,
                animationSpec = tween(300)
            )
            
            val textColor by animateColorAsState(
                targetValue = if (isSelected) TextOnAccent else TextSecondary,
                animationSpec = tween(300)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor)
                    .clickable { onOptionSelected(option) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }
        }
    }
}
