package com.example.admin_ingresos.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin_ingresos.R

@Composable
fun DashboardScreen(onAddTransaction: () -> Unit, onViewHistory: () -> Unit, onViewReports: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { com.example.admin_ingresos.AppDatabaseProvider.getDatabase(context) }
    val viewModel: DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(com.example.admin_ingresos.data.TransactionRepository(db)) as T
        }
    })
    
    val transactions by viewModel.transactions.collectAsState(initial = emptyList())
    val categorias by produceState(initialValue = emptyList<com.example.admin_ingresos.data.Category>(), db) {
        value = db.categoryDao().getAll()
    }
    
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showCustomizeDialog by remember { mutableStateOf(false) }
    
    // Pull-to-refresh state
    val pullToRefreshState = rememberPullToRefreshState()
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) { 
        viewModel.loadTransactions() 
    }
    
    // Handle pull-to-refresh
    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            viewModel.refreshData()
        }
    }
    
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && pullToRefreshState.isRefreshing) {
            pullToRefreshState.endRefresh()
        }
    }

    // Cálculos financieros
    val ingresos = transactions.filter { it.type == "Ingreso" }.sumOf { it.amount }
    val gastos = transactions.filter { it.type == "Gasto" }.sumOf { it.amount }
    val balance = ingresos - gastos
    val recientes = transactions.take(5)
    
    fun getCategoryName(catId: Int?): String {
        return categorias.find { it.id == catId }?.name ?: "Sin categoría"
    }
    
    // Generate smart suggestions based on transaction history
    val smartSuggestions = remember(transactions, categorias) {
        generateSmartSuggestions(transactions, categorias)
    }
    
    // Generate quick action shortcuts based on frequent transactions
    val quickShortcuts = remember(transactions, categorias) {
        generateQuickShortcuts(transactions, categorias)
    }
    
    // Widget configuration from preferences
    val widgetConfigs by DashboardPreferences.widgetConfigs

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Dashboard header with customization
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showCustomizeDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Personalizar dashboard",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Balance Widget
        widgetConfigs.find { it.type == WidgetType.BALANCE }?.let { config ->
            item {
                DashboardWidget(
                    config = config,
                    content = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Balance Total",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$${String.format("%.2f", balance)}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (balance >= 0) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        }
        
        // Income and Expenses Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardCard(
                    title = "Ingresos", 
                    amount = "${String.format("%.2f", ingresos)}", 
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                DashboardCard(
                    title = "Gastos", 
                    amount = "${String.format("%.2f", gastos)}", 
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Actions Widget
        widgetConfigs.find { it.type == WidgetType.QUICK_ACTIONS }?.let { config ->
            item {
                QuickActionWidget(
                    onAddIncome = { 
                        // Navigate to add transaction with income pre-selected
                        onAddTransaction()
                    },
                    onAddExpense = { 
                        // Navigate to add transaction with expense pre-selected
                        onAddTransaction()
                    },
                    onViewReports = onViewReports,
                    onViewHistory = onViewHistory
                )
            }
        }
        
        // Smart Suggestions Widget
        if (smartSuggestions.isNotEmpty()) {
            item {
                SmartSuggestionsWidget(
                    suggestions = smartSuggestions,
                    onSuggestionClick = { suggestion ->
                        // Navigate to add transaction with pre-filled data
                        onAddTransaction()
                    },
                    onDismissSuggestion = { suggestion ->
                        // Handle suggestion dismissal
                    }
                )
            }
        }
        
        // Quick Action Shortcuts Widget
        item {
            QuickActionShortcutsWidget(
                shortcuts = quickShortcuts,
                onShortcutClick = { shortcut ->
                    // Navigate to add transaction with pre-filled data from shortcut
                    onAddTransaction()
                },
                onAddShortcut = {
                    // Navigate to shortcut management screen
                }
            )
        }
        
        // Interactive Expense Chart
        if (gastos > 0) {
            widgetConfigs.find { it.type == WidgetType.EXPENSE_CHART }?.let { config ->
                item {
                    val gastosPorCategoria: Map<String, Double> = transactions
                        .filter { it.type == "Gasto" }
                        .groupBy { getCategoryName(it.categoryId) }
                        .mapValues { entry -> entry.value.sumOf { it.amount } }
                    
                    DashboardWidget(
                        config = config,
                        content = {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Gastos por Categoría",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                InteractiveExpensePieChart(
                                    context = context,
                                    expensesByCategory = gastosPorCategoria,
                                    onCategorySelected = { category, amount ->
                                        selectedCategory = category
                                        // Could navigate to filtered transaction list
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        onWidgetClick = {
                            // Navigate to detailed analytics
                            onViewReports()
                        }
                    )
                }
            }
        }
        
        // Recent Transactions Widget
        widgetConfigs.find { it.type == WidgetType.RECENT_TRANSACTIONS }?.let { config ->
            item {
                DashboardWidget(
                    config = config,
                    content = {
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
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                TextButton(onClick = onViewHistory) {
                                    Text("Ver todas")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (recientes.isNotEmpty()) {
                                recientes.forEach { tx ->
                                    RecentTransactionItem(
                                        category = tx.description,
                                        type = tx.type,
                                        amount = if (tx.type == "Ingreso") tx.amount else -tx.amount,
                                        description = getCategoryName(tx.categoryId),
                                        color = if (tx.type == "Ingreso") 
                                            MaterialTheme.colorScheme.secondary
                                        else 
                                            MaterialTheme.colorScheme.error
                                    )
                                    if (tx != recientes.last()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No hay transacciones recientes",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    },
                    onWidgetClick = onViewHistory
                )
            }
        }
        
        // Espaciado adicional para el FAB
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
        
        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = pullToRefreshState,
        )
    }

    // Dashboard customization dialog
    if (showCustomizeDialog) {
        DashboardCustomizationDialog(
            widgetConfigs = widgetConfigs,
            onConfigsChanged = { newConfigs ->
                DashboardPreferences.updateWidgetConfigs(newConfigs)
            },
            onDismiss = { showCustomizeDialog = false }
        )
    }
}

@Composable
fun RecentTransactionItem(
    category: String,
    type: String,
    amount: Double,
    description: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono minimalista
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.take(1).uppercase(),
                    color = color,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = if (amount > 0) "+${String.format("%.2f", amount)}" else "-${String.format("%.2f", kotlin.math.abs(amount))}",
                color = color,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun DashboardCard(title: String, amount: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .height(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${amount}",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}