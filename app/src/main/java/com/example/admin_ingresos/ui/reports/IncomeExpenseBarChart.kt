package com.example.admin_ingresos.ui.reports

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.data.MonthlyTrend
import kotlin.math.max

@Composable
fun IncomeExpenseBarChart(
    monthlyTrends: List<MonthlyTrend>,
    modifier: Modifier = Modifier
) {
    if (monthlyTrends.isEmpty()) {
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
                    text = "No hay datos suficientes para mostrar tendencias",
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
                text = "Tendencias Mensuales",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                LegendItem(
                    color = Color(0xFF4CAF50),
                    label = "Ingresos"
                )
                Spacer(modifier = Modifier.width(16.dp))
                LegendItem(
                    color = MaterialTheme.colorScheme.error,
                    label = "Gastos"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                BarChart(
                    monthlyTrends = monthlyTrends,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Summary
            MonthlyTrendSummary(monthlyTrends = monthlyTrends)
        }
    }
}

@Composable
fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BarChart(
    monthlyTrends: List<MonthlyTrend>,
    modifier: Modifier = Modifier
) {
    val incomeColor = Color(0xFF4CAF50)
    val expenseColor = MaterialTheme.colorScheme.error
    
    Canvas(modifier = modifier) {
        if (monthlyTrends.isEmpty()) return@Canvas
        
        val maxValue = monthlyTrends.maxOfOrNull { 
            max(it.monthlyData.income, it.monthlyData.expenses) 
        } ?: 1.0
        
        val barWidth = size.width / (monthlyTrends.size * 2.5f)
        val chartHeight = size.height - 60.dp.toPx() // Leave space for labels
        
        monthlyTrends.forEachIndexed { index, trend ->
            val x = (index * 2.5f + 0.5f) * barWidth
            
            // Income bar
            val incomeHeight = (trend.monthlyData.income / maxValue * chartHeight).toFloat()
            drawRect(
                color = incomeColor,
                topLeft = Offset(x, chartHeight - incomeHeight),
                size = Size(barWidth * 0.4f, incomeHeight)
            )
            
            // Expense bar
            val expenseHeight = (trend.monthlyData.expenses / maxValue * chartHeight).toFloat()
            drawRect(
                color = expenseColor,
                topLeft = Offset(x + barWidth * 0.5f, chartHeight - expenseHeight),
                size = Size(barWidth * 0.4f, expenseHeight)
            )
        }
    }
}

@Composable
fun MonthlyTrendSummary(
    monthlyTrends: List<MonthlyTrend>
) {
    if (monthlyTrends.size < 2) return
    
    val latestTrend = monthlyTrends.last()
    
    Column {
        Text(
            text = "Cambios respecto al mes anterior",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TrendIndicator(
                label = "Ingresos",
                change = latestTrend.incomeChange,
                isPositive = latestTrend.incomeChange >= 0
            )
            
            TrendIndicator(
                label = "Gastos",
                change = latestTrend.expenseChange,
                isPositive = latestTrend.expenseChange <= 0 // For expenses, decrease is positive
            )
            
            TrendIndicator(
                label = "Balance",
                change = latestTrend.balanceChange,
                isPositive = latestTrend.balanceChange >= 0
            )
        }
    }
}

@Composable
fun TrendIndicator(
    label: String,
    change: Double,
    isPositive: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Text(
            text = "${if (change >= 0) "+" else ""}${String.format("%.1f", change)}%",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = if (isPositive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
        )
        
        Text(
            text = when {
                change > 10 -> if (isPositive) "📈" else "📉"
                change < -10 -> if (isPositive) "📉" else "📈"
                else -> "➡️"
            },
            style = MaterialTheme.typography.bodySmall
        )
    }
}