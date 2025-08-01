package com.example.admin_ingresos.ui.budget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.data.BudgetProgress
import com.example.admin_ingresos.data.BudgetStatus
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BudgetProgressChart(
    budgetProgress: List<BudgetProgress>,
    modifier: Modifier = Modifier
) {
    if (budgetProgress.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay datos de presupuesto",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Resumen de Presupuestos",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Summary statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BudgetSummaryItem(
                    title = "Total Presupuestado",
                    value = "$${String.format("%.2f", budgetProgress.sumOf { it.budget.amount })}",
                    color = MaterialTheme.colorScheme.primary
                )
                
                BudgetSummaryItem(
                    title = "Total Gastado",
                    value = "$${String.format("%.2f", budgetProgress.sumOf { it.spent })}",
                    color = MaterialTheme.colorScheme.secondary
                )
                
                BudgetSummaryItem(
                    title = "Restante",
                    value = "$${String.format("%.2f", budgetProgress.sumOf { it.remaining })}",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Circular progress chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                BudgetCircularChart(budgetProgress = budgetProgress)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status breakdown
            BudgetStatusBreakdown(budgetProgress = budgetProgress)
        }
    }
}

@Composable
fun BudgetSummaryItem(
    title: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BudgetCircularChart(
    budgetProgress: List<BudgetProgress>,
    modifier: Modifier = Modifier
) {
    val totalBudget = budgetProgress.sumOf { it.budget.amount }
    val totalSpent = budgetProgress.sumOf { it.spent }
    val overallPercentage = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
    
    Box(
        modifier = modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val strokeWidth = 20.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)
            
            // Background circle
            drawCircle(
                color = Color.Gray.copy(alpha = 0.2f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            
            // Progress arc
            val sweepAngle = 360f * overallPercentage.coerceAtMost(1f)
            val progressColor = when {
                overallPercentage >= 1.2f -> Color(0xFF8B0000)
                overallPercentage >= 1.0f -> Color.Red
                overallPercentage >= 0.8f -> Color(0xFFFF9800)
                else -> Color(0xFF4CAF50)
            }
            
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth),
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2)
            )
        }
        
        // Center text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${(overallPercentage * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Usado",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun BudgetStatusBreakdown(
    budgetProgress: List<BudgetProgress>
) {
    val statusCounts = budgetProgress.groupBy { it.status }.mapValues { it.value.size }
    
    Column {
        Text(
            text = "Estado de Presupuestos",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        BudgetStatus.values().forEach { status ->
            val count = statusCounts[status] ?: 0
            if (count > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (status) {
                                        BudgetStatus.ON_TRACK -> Color(0xFF4CAF50)
                                        BudgetStatus.WARNING -> Color(0xFFFF9800)
                                        BudgetStatus.EXCEEDED -> Color.Red
                                        BudgetStatus.OVER_BUDGET -> Color(0xFF8B0000)
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = status.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}