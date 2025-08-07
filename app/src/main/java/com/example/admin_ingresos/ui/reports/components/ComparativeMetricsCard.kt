package com.example.admin_ingresos.ui.reports.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.ui.components.GlassCard
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import java.text.NumberFormat
import java.util.*

@Composable
fun ComparativeMetricsCard(
    currentIngresos: Double,
    currentGastos: Double,
    currentBalance: Double,
    ingresosChange: Double,
    gastosChange: Double,
    balanceChange: Double,
    periodLabel: String,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = remember { 
        NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    }
    
    GlassCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF667eea).copy(alpha = 0.3f),
                                    Color(0xFF667eea).copy(alpha = 0.1f)
                                ),
                                radius = 50f
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LucideIconMapper.getNavigationIcon("TrendingUp"),
                        contentDescription = "Métricas comparativas",
                        tint = Color(0xFF667eea),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Column {
                    Text(
                        text = "Análisis Comparativo",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = periodLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Metrics
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricRow(
                    label = "Ingresos",
                    amount = currentIngresos,
                    change = ingresosChange,
                    icon = "TrendingUp",
                    currencyFormatter = currencyFormatter
                )
                
                MetricRow(
                    label = "Gastos",
                    amount = currentGastos,
                    change = gastosChange,
                    icon = "TrendingDown",
                    currencyFormatter = currencyFormatter
                )
                
                Divider(
                    color = Color.White.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
                
                MetricRow(
                    label = "Balance",
                    amount = currentBalance,
                    change = balanceChange,
                    icon = "Wallet",
                    currencyFormatter = currencyFormatter,
                    isBalance = true
                )
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    amount: Double,
    change: Double,
    icon: String,
    currencyFormatter: NumberFormat,
    isBalance: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "metric_animation")
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_animation"
    )
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.White.copy(alpha = 0.05f)
                            ),
                            radius = 30f
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LucideIconMapper.getNavigationIcon(icon),
                    contentDescription = label,
                    tint = Color.White.copy(alpha = animatedAlpha),
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = currencyFormatter.format(amount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isBalance) {
                        when {
                            amount > 0 -> Color(0xFF4CAF50)
                            amount < 0 -> Color(0xFFE53E3E)
                            else -> Color.White
                        }
                    } else Color.White
                )
            }
        }
        
        // Change indicator
        if (change != 0.0) {
            ChangeIndicator(
                change = change,
                isPositiveGood = when (label) {
                    "Gastos" -> false
                    else -> true
                }
            )
        }
    }
}

@Composable
private fun ChangeIndicator(
    change: Double,
    isPositiveGood: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isPositive = change > 0
    val isGood = if (isPositiveGood) isPositive else !isPositive
    
    val color = when {
        change == 0.0 -> Color.White.copy(alpha = 0.6f)
        isGood -> Color(0xFF4CAF50)
        else -> Color(0xFFE53E3E)
    }
    
    val icon = when {
        change == 0.0 -> "Minus"
        isPositive -> "ArrowUp"
        else -> "ArrowDown"
    }
    
    Row(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = LucideIconMapper.getNavigationIcon(icon),
            contentDescription = "Cambio",
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        
        Text(
            text = "${if (change > 0) "+" else ""}${String.format("%.1f", change)}%",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = color
        )
    }
}
