package com.example.admin_ingresos.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
// Pull to refresh removed for compatibility
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.abs
@Composable
fun ReportsScreen() {
    val context = LocalContext.current
    val db = remember { com.example.admin_ingresos.AppDatabaseProvider.getDatabase(context) }
    val viewModel: ReportsViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ReportsViewModel(db) as T
        }
    })
    
    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val monthlyTrends by viewModel.monthlyTrends.collectAsState()
    val categoryTrends by viewModel.categoryTrends.collectAsState()
    val spendingPatterns by viewModel.spendingPatterns.collectAsState()
    val financialInsights by viewModel.financialInsights.collectAsState()
    val financialProjections by viewModel.financialProjections.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Pull to refresh removed for compatibility

    // Cálculos financieros
    val ingresos = transactions.filter { it.type == "Ingreso" }.sumOf { it.amount }
    val gastos = transactions.filter { it.type == "Gasto" }.sumOf { it.amount }
    val balance = ingresos - gastos
    val totalTransactions = transactions.size
    
    fun getCategoryName(catId: Int?): String {
        return categories.find { it.id == catId }?.name ?: "Sin categoría"
    }
    
    // Análisis por categoría
    val categoriasMap = transactions.groupBy { it.categoryId }
        .mapValues { (_, transList) -> transList.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Reportes y Análisis",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Resumen de tus finanzas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        // Balance y métricas principales
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Balance total
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (balance >= 0) 
                            MaterialTheme.colorScheme.secondaryContainer
                        else 
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (balance >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (balance >= 0) 
                                MaterialTheme.colorScheme.onSecondaryContainer
                            else 
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Balance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (balance >= 0) 
                                MaterialTheme.colorScheme.onSecondaryContainer
                            else 
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "$${String.format("%.2f", balance)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (balance >= 0) 
                                MaterialTheme.colorScheme.onSecondaryContainer
                            else 
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                
                // Total de transacciones
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$totalTransactions",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Transacciones",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        
        // Cards de ingresos y gastos
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportCard(
                    title = "Ingresos",
                    amount = ingresos,
                    color = MaterialTheme.colorScheme.secondary,
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                ReportCard(
                    title = "Gastos",
                    amount = gastos,
                    color = MaterialTheme.colorScheme.error,
                    icon = Icons.Default.TrendingDown,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Tendencias mensuales
        if (monthlyTrends.isNotEmpty()) {
            item {
                IncomeExpenseBarChart(
                    monthlyTrends = monthlyTrends,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Proyecciones financieras
        financialProjections?.let { projections ->
            item {
                FinancialProjectionsCard(
                    projections = projections,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Insights financieros
        if (financialInsights.isNotEmpty()) {
            item {
                Text(
                    text = "Insights Financieros",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(financialInsights) { insight ->
                FinancialInsightCard(insight = insight)
            }
        }
        
        // Análisis por categoría
        if (categoriasMap.isNotEmpty()) {
            item {
                Text(
                    text = "Análisis por Categoría",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(categoriasMap) { (categoryId, total) ->
                CategoryAnalysisItem(
                    categoryName = getCategoryName(categoryId),
                    amount = total,
                    percentage = if (ingresos + gastos > 0) (total / (ingresos + gastos)) * 100 else 0.0
                )
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay datos suficientes para generar reportes\nAgrega algunas transacciones para ver el análisis",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
        
        // Espaciado adicional para el FAB
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
        
        // Pull to refresh removed for compatibility
    }
}

@Composable
fun ReportCard(
    title: String,
    amount: Double,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$${String.format("%.2f", amount)}",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

@Composable
fun CategoryAnalysisItem(
    categoryName: String,
    amount: Double,
    percentage: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$${String.format("%.2f", amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            // Barra de progreso
            LinearProgressIndicator(
                progress = { (percentage / 100).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${String.format("%.1f", percentage)}% del total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
@Composable
fun FinancialInsightCard(
    insight: com.example.admin_ingresos.data.FinancialInsight
) {
    val containerColor = when (insight.type) {
        com.example.admin_ingresos.data.InsightType.INFO -> MaterialTheme.colorScheme.primaryContainer
        com.example.admin_ingresos.data.InsightType.TIP -> MaterialTheme.colorScheme.secondaryContainer
        com.example.admin_ingresos.data.InsightType.WARNING -> Color(0xFFFF9800).copy(alpha = 0.2f)
        com.example.admin_ingresos.data.InsightType.ALERT -> MaterialTheme.colorScheme.errorContainer
    }
    
    val contentColor = when (insight.type) {
        com.example.admin_ingresos.data.InsightType.INFO -> MaterialTheme.colorScheme.onPrimaryContainer
        com.example.admin_ingresos.data.InsightType.TIP -> MaterialTheme.colorScheme.onSecondaryContainer
        com.example.admin_ingresos.data.InsightType.WARNING -> Color(0xFFFF9800)
        com.example.admin_ingresos.data.InsightType.ALERT -> MaterialTheme.colorScheme.onErrorContainer
    }
    
    val icon = when (insight.type) {
        com.example.admin_ingresos.data.InsightType.INFO -> "ℹ️"
        com.example.admin_ingresos.data.InsightType.TIP -> "💡"
        com.example.admin_ingresos.data.InsightType.WARNING -> "⚠️"
        com.example.admin_ingresos.data.InsightType.ALERT -> "🚨"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(end = 12.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = contentColor
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "💡 ${insight.recommendation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}