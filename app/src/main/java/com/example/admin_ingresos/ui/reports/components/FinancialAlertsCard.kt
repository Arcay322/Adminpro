package com.example.admin_ingresos.ui.reports.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.data.Transaction
import com.example.admin_ingresos.ui.components.GlassCard
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import java.text.NumberFormat
import java.util.*

data class FinancialAlert(
    val type: AlertType,
    val title: String,
    val description: String,
    val severity: AlertSeverity,
    val actionRequired: Boolean = false
)

enum class AlertType {
    BUDGET_EXCEEDED,
    UNUSUAL_SPENDING,
    SAVINGS_OPPORTUNITY,
    TREND_WARNING,
    PREDICTION
}

enum class AlertSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

@Composable
fun FinancialAlertsCard(
    transactions: List<Transaction>,
    totalIngresos: Double,
    totalGastos: Double,
    modifier: Modifier = Modifier
) {
    val alerts = remember(transactions, totalIngresos, totalGastos) {
        generateAlerts(transactions, totalIngresos, totalGastos)
    }
    
    if (alerts.isNotEmpty()) {
        GlassCard(
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                        Color(0xFFE53E3E).copy(alpha = 0.3f),
                                        Color(0xFFE53E3E).copy(alpha = 0.1f)
                                    ),
                                    radius = 50f
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIconMapper.getNavigationIcon("AlertTriangle"),
                            contentDescription = "Alertas financieras",
                            tint = Color(0xFFE53E3E),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Alertas Inteligentes",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "${alerts.size} alertas detectadas",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Alerts list
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alerts) { alert ->
                        AlertItem(alert = alert)
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertItem(
    alert: FinancialAlert,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "alert_animation")
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = if (alert.severity == AlertSeverity.CRITICAL) 0.5f else 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (alert.severity == AlertSeverity.CRITICAL) 1000 else 2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_animation"
    )
    
    val (backgroundColor, iconColor, icon) = when (alert.severity) {
        AlertSeverity.LOW -> Triple(
            Color(0xFF3B82F6).copy(alpha = 0.1f),
            Color(0xFF3B82F6),
            "Info"
        )
        AlertSeverity.MEDIUM -> Triple(
            Color(0xFFF59E0B).copy(alpha = 0.1f),
            Color(0xFFF59E0B),
            "AlertCircle"
        )
        AlertSeverity.HIGH -> Triple(
            Color(0xFFE53E3E).copy(alpha = 0.1f),
            Color(0xFFE53E3E),
            "AlertTriangle"
        )
        AlertSeverity.CRITICAL -> Triple(
            Color(0xFFDC2626).copy(alpha = 0.2f),
            Color(0xFFDC2626),
            "XCircle"
        )
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = iconColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LucideIconMapper.getNavigationIcon(icon),
                    contentDescription = alert.type.name,
                    tint = iconColor.copy(alpha = animatedAlpha),
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                
                Text(
                    text = alert.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
                
                if (alert.actionRequired) {
                    Text(
                        text = "🎯 Acción recomendada",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = iconColor
                    )
                }
            }
        }
    }
}

private fun generateAlerts(
    transactions: List<Transaction>,
    totalIngresos: Double,
    totalGastos: Double
): List<FinancialAlert> {
    val alerts = mutableListOf<FinancialAlert>()
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    
    // 1. Balance alert
    val balance = totalIngresos - totalGastos
    if (balance < 0) {
        alerts.add(
            FinancialAlert(
                type = AlertType.BUDGET_EXCEEDED,
                title = "Balance Negativo",
                description = "Tus gastos superan tus ingresos por ${currencyFormatter.format(kotlin.math.abs(balance))}. Considera revisar tus gastos.",
                severity = if (kotlin.math.abs(balance) > totalIngresos * 0.1) AlertSeverity.HIGH else AlertSeverity.MEDIUM,
                actionRequired = true
            )
        )
    }
    
    // 2. Unusual spending pattern
    val expenseTransactions = transactions.filter { it.type == "Gasto" }
    if (expenseTransactions.isNotEmpty()) {
        val avgExpense = expenseTransactions.map { it.amount }.average()
        val largeExpenses = expenseTransactions.filter { it.amount > avgExpense * 2 }
        
        if (largeExpenses.isNotEmpty()) {
            alerts.add(
                FinancialAlert(
                    type = AlertType.UNUSUAL_SPENDING,
                    title = "Gastos Inusuales Detectados",
                    description = "Se detectaron ${largeExpenses.size} gastos significativamente altos. El mayor fue de ${currencyFormatter.format(largeExpenses.maxOf { it.amount })}.",
                    severity = AlertSeverity.MEDIUM
                )
            )
        }
    }
    
    // 3. Savings opportunity
    if (balance > totalIngresos * 0.3) {
        alerts.add(
            FinancialAlert(
                type = AlertType.SAVINGS_OPPORTUNITY,
                title = "Oportunidad de Ahorro",
                description = "Tienes un balance positivo del ${((balance / totalIngresos) * 100).toInt()}%. Considera invertir o ahorrar parte de este excedente.",
                severity = AlertSeverity.LOW,
                actionRequired = true
            )
        )
    }
    
    // 4. High spending warning
    if (totalGastos > totalIngresos * 0.8) {
        alerts.add(
            FinancialAlert(
                type = AlertType.TREND_WARNING,
                title = "Alto Nivel de Gastos",
                description = "Estás gastando el ${((totalGastos / totalIngresos) * 100).toInt()}% de tus ingresos. Mantén un control para evitar sobreendeudamiento.",
                severity = AlertSeverity.MEDIUM,
                actionRequired = true
            )
        )
    }
    
    // 5. Future prediction
    if (transactions.isNotEmpty()) {
        val trend = if (totalGastos > totalIngresos * 0.9) "ascendente" else "controlada"
        alerts.add(
            FinancialAlert(
                type = AlertType.PREDICTION,
                title = "Proyección del Próximo Mes",
                description = "Basado en tus patrones actuales, se espera una tendencia de gastos $trend. ${if (trend == "ascendente") "Considera ajustar tu presupuesto." else "Mantén estos buenos hábitos."}",
                severity = if (trend == "ascendente") AlertSeverity.MEDIUM else AlertSeverity.LOW
            )
        )
    }
    
    return alerts.sortedByDescending { it.severity.ordinal }
}
