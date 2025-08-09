package com.example.admin_ingresos.ui.transaction

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin_ingresos.ui.components.*
import com.example.admin_ingresos.data.Transaction
import com.example.admin_ingresos.data.Category
import com.example.admin_ingresos.data.PaymentMethod
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreenNew() {
    val context = LocalContext.current
    val db = remember { com.example.admin_ingresos.AppDatabaseProvider.getDatabase(context) }
    // ViewModel eliminado: lógica local
    
    // State management
    var selectedFilter by remember { mutableStateOf("Todos") }
    var selectedPeriod by remember { mutableStateOf("Este mes") }
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    
    // Data loading
    val transactions by produceState(initialValue = emptyList<Transaction>(), db, selectedFilter, selectedPeriod) {
        value = withContext(Dispatchers.IO) {
            when (selectedFilter) {
                "Ingresos" -> db.transactionDao().getAll().filter { it.type == "Ingreso" }
                "Gastos" -> db.transactionDao().getAll().filter { it.type == "Gasto" }
                else -> db.transactionDao().getAll()
            }.let { list ->
                when (selectedPeriod) {
                    "Hoy" -> filterByToday(list)
                    "Esta semana" -> filterByThisWeek(list)
                    "Este mes" -> filterByThisMonth(list)
                    "Este año" -> filterByThisYear(list)
                    else -> list
                }
            }
        }
    }
    val categories by produceState(initialValue = emptyList<Category>(), db) {
        value = withContext(Dispatchers.IO) {
            db.categoryDao().getAllCategories().first()
        }
    }
    val paymentMethods by produceState(initialValue = emptyList<PaymentMethod>(), db) {
        value = withContext(Dispatchers.IO) {
            db.paymentMethodDao().getAll()
        }
    }
    
    // Filtered transactions based on search
    val filteredTransactions = remember(transactions, searchQuery) {
        if (searchQuery.isBlank()) {
            transactions
        } else {
            transactions.filter { transaction ->
                transaction.description.contains(searchQuery, ignoreCase = true) ||
                categories.find { it.id == transaction.categoryId }?.name?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }
    
    // Calculate summary stats
    val totalIncome = filteredTransactions.filter { it.type == "Ingreso" }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type == "Gasto" }.sumOf { it.amount }
    val netAmount = totalIncome - totalExpense

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Enhanced Header with search
        CashFlowHeader(
            title = "Historial",
            subtitle = "${filteredTransactions.size} transacciones",
            actions = {
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(
                        imageVector = if (showFilters) Icons.Default.FilterListOff else Icons.Default.FilterList,
                        contentDescription = "Filtros",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Buscar transacciones..."
            )
            
            // Filters section (collapsible)
            AnimatedVisibility(
                visible = showFilters,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                FiltersSection(
                    selectedFilter = selectedFilter,
                    onFilterChange = { selectedFilter = it },
                    selectedPeriod = selectedPeriod,
                    onPeriodChange = { selectedPeriod = it }
                )
            }
            
            // Summary stats
            SummaryStatsCard(
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                netAmount = netAmount,
                transactionCount = filteredTransactions.size
            )
            
            // Transaction list
            if (filteredTransactions.isEmpty()) {
                EmptyTransactionsState(hasFilters = selectedFilter != "Todos" || selectedPeriod != "Todos" || searchQuery.isNotBlank())
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredTransactions.sortedByDescending { it.date },
                        key = { it.id }
                    ) { transaction ->
                        EnhancedTransactionItem(
                            transaction = transaction,
                            category = categories.find { it.id == transaction.categoryId },
                            paymentMethod = paymentMethods.find { it.id == transaction.paymentMethodId },
                            onEdit = { /* TODO: Implement edit */ },
                            onDelete = { /* TODO: Implement delete */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar"
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar"
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
private fun FiltersSection(
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    selectedPeriod: String,
    onPeriodChange: (String) -> Unit
) {
    CashFlowCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Filtros",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Type filter
            Column {
                Text(
                    text = "Tipo",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Todos", "Ingresos", "Gastos").forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { onFilterChange(filter) },
                            label = { Text(filter) }
                        )
                    }
                }
            }
            
            // Period filter
            Column {
                Text(
                    text = "Período",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Hoy", "Esta semana", "Este mes", "Este año").forEach { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { onPeriodChange(period) },
                            label = { Text(period) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStatsCard(
    totalIncome: Double,
    totalExpense: Double,
    netAmount: Double,
    transactionCount: Int
) {
    CashFlowCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryStatItem(
                title = "Ingresos",
                value = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(totalIncome),
                color = Color(0xFF4CAF50),
                icon = Icons.Default.TrendingUp
            )
            
            SummaryStatItem(
                title = "Gastos",
                value = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(totalExpense),
                color = Color(0xFFE57373),
                icon = Icons.Default.TrendingDown
            )
            
            SummaryStatItem(
                title = "Balance",
                value = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(netAmount),
                color = if (netAmount >= 0) Color(0xFF4CAF50) else Color(0xFFE57373),
                icon = if (netAmount >= 0) Icons.Default.AccountBalanceWallet else Icons.Default.Warning
            )
        }
    }
}

@Composable
private fun SummaryStatItem(
    title: String,
    value: String,
    color: Color,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = color
        )
    }
}

@Composable
private fun EmptyTransactionsState(hasFilters: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (hasFilters) Icons.Default.FilterListOff else Icons.Default.Receipt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (hasFilters) "Sin resultados" else "Sin transacciones",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (hasFilters) 
                "No se encontraron transacciones con los filtros aplicados" 
            else 
                "Aún no has registrado ninguna transacción",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedTransactionItem(
    transaction: Transaction,
    category: Category?,
    paymentMethod: PaymentMethod?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    CashFlowCard(
        onClick = { /* TODO: Show details */ }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (transaction.type == "Ingreso") 
                        Color(0xFF4CAF50).copy(alpha = 0.1f) 
                    else 
                        Color(0xFFE57373).copy(alpha = 0.1f)
                )
            ) {
                Icon(
                    imageVector = getCategoryIcon(category?.name ?: ""),
                    contentDescription = null,
                    tint = if (transaction.type == "Ingreso") Color(0xFF4CAF50) else Color(0xFFE57373),
                    modifier = Modifier
                        .size(40.dp)
                        .padding(8.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Transaction details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category?.name ?: "Sin categoría",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (paymentMethod != null) {
                        Text(
                            text = " • ${paymentMethod.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(Date(transaction.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Amount and actions
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
                        .format(transaction.amount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (transaction.type == "Ingreso") Color(0xFF4CAF50) else Color(0xFFE57373)
                )
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        )
                    }
                }
            }
        }
    }
}

// Helper functions for filtering
private fun filterByToday(transactions: List<Transaction>): List<Transaction> {
    val today = Calendar.getInstance()
    return transactions.filter {
        val transactionDate = Calendar.getInstance().apply { timeInMillis = it.date }
        today.get(Calendar.DAY_OF_YEAR) == transactionDate.get(Calendar.DAY_OF_YEAR) &&
        today.get(Calendar.YEAR) == transactionDate.get(Calendar.YEAR)
    }
}

private fun filterByThisWeek(transactions: List<Transaction>): List<Transaction> {
    val today = Calendar.getInstance()
    val startOfWeek = Calendar.getInstance().apply {
        timeInMillis = today.timeInMillis
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    
    return transactions.filter { it.date >= startOfWeek.timeInMillis }
}

private fun filterByThisMonth(transactions: List<Transaction>): List<Transaction> {
    val today = Calendar.getInstance()
    return transactions.filter {
        val transactionDate = Calendar.getInstance().apply { timeInMillis = it.date }
        today.get(Calendar.MONTH) == transactionDate.get(Calendar.MONTH) &&
        today.get(Calendar.YEAR) == transactionDate.get(Calendar.YEAR)
    }
}

private fun filterByThisYear(transactions: List<Transaction>): List<Transaction> {
    val today = Calendar.getInstance()
    return transactions.filter {
        val transactionDate = Calendar.getInstance().apply { timeInMillis = it.date }
        today.get(Calendar.YEAR) == transactionDate.get(Calendar.YEAR)
    }
}

// Helper function for category icons (reuse from TransactionSelectors.kt)
private fun getCategoryIcon(categoryName: String): ImageVector {
    return when (categoryName.lowercase()) {
        "comida", "alimentación", "supermercado" -> Icons.Default.Restaurant
        "transporte" -> Icons.Default.DirectionsCar
        "entretenimiento", "ocio" -> Icons.Default.Movie
        "salud" -> Icons.Default.LocalHospital
        "ropa", "vestimenta" -> Icons.Default.Checkroom
        "hogar", "casa" -> Icons.Default.Home
        "educación", "estudio" -> Icons.Default.School
        "trabajo", "salario" -> Icons.Default.Work
        "servicios" -> Icons.Default.Build
        "otros" -> Icons.Default.Category
        else -> Icons.Default.Category
    }
}
