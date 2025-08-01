package com.example.admin_ingresos.ui.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin_ingresos.AppDatabaseProvider
import com.example.admin_ingresos.ui.theme.*
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedDashboardScreen(
    onAddTransaction: () -> Unit,
    onViewHistory: () -> Unit,
    onViewReports: () -> Unit,
    onViewBudgets: () -> Unit = {},
    onViewCategories: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { AppDatabaseProvider.getDatabase(context) }
    
    // Estados simplificados para la demo
    val balance by remember { mutableDoubleStateOf(5420.75) }
    val ingresos by remember { mutableDoubleStateOf(8500.0) }
    val gastos by remember { mutableDoubleStateOf(3079.25) }
    val recientes by remember { mutableStateOf(emptyList<com.example.admin_ingresos.data.Transaction>()) }
    val categories by remember { mutableStateOf(emptyList<com.example.admin_ingresos.data.Category>()) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with CashFlow branding
        item {
            CashFlowDashboardHeader()
        }

        // Enhanced Balance Card
        item {
            EnhancedBalanceCard(
                balance = balance,
                monthlyIncome = ingresos,
                monthlyExpenses = gastos
            )
        }

        // Quick Actions with new design
        item {
            CashFlowQuickActions(
                onAddIncome = onAddTransaction,
                onAddExpense = onAddTransaction,
                onViewCategories = onViewCategories,
                onViewBudgets = onViewBudgets
            )
        }

        // Financial Overview Cards
        item {
            FinancialOverviewCards(
                income = ingresos,
                expenses = gastos,
                savingsRate = if (ingresos > 0) ((ingresos - gastos) / ingresos * 100) else 0.0
            )
        }

        // Recent Transactions with improved design
        item {
            EnhancedRecentTransactions(
                transactions = recientes,
                categories = categories,
                onViewAllTransactions = onViewHistory
            )
        }

        // Smart insights section
        item {
            SmartInsightsCard(
                income = ingresos,
                expenses = gastos,
                balance = balance
            )
        }

        // Bottom spacing for FAB
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun CashFlowDashboardHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "CashFlow",
                style = MaterialTheme.typography.headlineMedium,
                color = CashFlowPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tu dashboard financiero",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        IconButton(
            onClick = { /* TODO: Settings */ }
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configuración",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EnhancedBalanceCard(
    balance: Double,
    monthlyIncome: Double,
    monthlyExpenses: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CashFlowPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Balance Total",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "$${String.format("%.2f", balance)}",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BalanceIndicator(
                    label = "Ingresos",
                    amount = monthlyIncome,
                    color = Success
                )
                BalanceIndicator(
                    label = "Gastos",
                    amount = monthlyExpenses,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun BalanceIndicator(
    label: String,
    amount: Double,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = "$${String.format("%.2f", amount)}",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = color
        )
    }
}

@Composable
private fun CashFlowQuickActions(
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onViewCategories: () -> Unit,
    onViewBudgets: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Acciones Rápidas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EnhancedQuickActionButton(
                    icon = Icons.Default.Add,
                    label = "Ingreso",
                    color = Success,
                    onClick = onAddIncome,
                    modifier = Modifier.weight(1f)
                )
                EnhancedQuickActionButton(
                    icon = Icons.Default.Remove,
                    label = "Gasto",
                    color = MaterialTheme.colorScheme.error,
                    onClick = onAddExpense,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EnhancedQuickActionButton(
                    icon = Icons.Default.Category,
                    label = "Categorías",
                    color = TertiaryPurple,
                    onClick = onViewCategories,
                    modifier = Modifier.weight(1f)
                )
                EnhancedQuickActionButton(
                    icon = Icons.Default.AccountBalance,
                    label = "Presupuestos",
                    color = CashFlowSecondary,
                    onClick = onViewBudgets,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EnhancedQuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FinancialOverviewCards(
    income: Double,
    expenses: Double,
    savingsRate: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FinancialMetricCard(
            title = "Ingresos",
            value = "$${String.format("%.2f", income)}",
            icon = Icons.Default.TrendingUp,
            color = Success,
            modifier = Modifier.weight(1f)
        )
        FinancialMetricCard(
            title = "Gastos",
            value = "$${String.format("%.2f", expenses)}",
            icon = Icons.Default.TrendingDown,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
        FinancialMetricCard(
            title = "Ahorro",
            value = "${String.format("%.1f", savingsRate)}%",
            icon = Icons.Default.Savings,
            color = CashFlowSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FinancialMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EnhancedRecentTransactions(
    transactions: List<com.example.admin_ingresos.data.Transaction>,
    categories: List<com.example.admin_ingresos.data.Category>,
    onViewAllTransactions: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transacciones Recientes",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onViewAllTransactions) {
                    Text("Ver todas")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (transactions.isNotEmpty()) {
                transactions.take(3).forEach { transaction ->
                    EnhancedTransactionItem(
                        transaction = transaction,
                        category = categories.find { it.id == transaction.categoryId }
                    )
                    if (transaction != transactions.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay transacciones recientes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedTransactionItem(
    transaction: com.example.admin_ingresos.data.Transaction,
    category: com.example.admin_ingresos.data.Category?
) {
    val isIncome = transaction.type == "Ingreso"
    val color = if (isIncome) Success else MaterialTheme.colorScheme.error
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category?.icon ?: "💰",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = category?.name ?: "Sin categoría",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "${if (isIncome) "+" else "-"}$${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SmartInsightsCard(
    income: Double,
    expenses: Double,
    balance: Double
) {
    val insights = generateSmartInsights(income, expenses, balance)
    
    if (insights.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Insights",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Insights Inteligentes",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                insights.forEach { insight ->
                    Text(
                        text = "• $insight",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

private fun generateSmartInsights(
    income: Double,
    expenses: Double,
    balance: Double
): List<String> {
    val insights = mutableListOf<String>()
    
    if (income > 0) {
        val savingsRate = ((income - expenses) / income * 100)
        when {
            savingsRate > 20 -> insights.add("¡Excelente! Estás ahorrando ${String.format("%.1f", savingsRate)}% de tus ingresos")
            savingsRate > 10 -> insights.add("Buen trabajo ahorrando ${String.format("%.1f", savingsRate)}% de tus ingresos")
            savingsRate > 0 -> insights.add("Intenta aumentar tu tasa de ahorro actual del ${String.format("%.1f", savingsRate)}%")
            else -> insights.add("Considera reducir gastos para poder ahorrar")
        }
    }
    
    if (expenses > income) {
        insights.add("Tus gastos superan tus ingresos este mes")
    }
    
    if (balance < 0) {
        insights.add("Tu balance es negativo. Revisa tus gastos prioritarios")
    }
    
    return insights
}
