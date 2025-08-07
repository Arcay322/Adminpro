package com.example.admin_ingresos.ui.history

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin_ingresos.AppDatabaseProvider
import com.example.admin_ingresos.data.Transaction
import com.example.admin_ingresos.ui.components.*
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import com.example.admin_ingresos.ui.theme.CashFlowPrimary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabaseProvider.getDatabase(context) }
    val coroutineScope = rememberCoroutineScope()
    
    // Estados para búsqueda y filtros
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Todos") }
    var sortOrder by remember { mutableStateOf("Fecha (Reciente)") }
    var showFilters by remember { mutableStateOf(false) }
    var showAnalytics by remember { mutableStateOf(false) }
    
    // Estados para diálogos
    var showDeleteDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }
    
    // Estados para el formulario de edición
    var editDescription by remember { mutableStateOf("") }
    var editAmount by remember { mutableStateOf("") }
    var editType by remember { mutableStateOf("Ingreso") }
    
    // Estado para forzar recarga de transacciones
    var reloadTrigger by remember { mutableStateOf(0) }
    
    // Cargar transacciones de forma reactiva
    val transactions by produceState(initialValue = emptyList<Transaction>(), db, reloadTrigger) {
        value = db.transactionDao().getAll()
    }
    
    // Filtrar y ordenar transacciones
    val filteredTransactions = remember(transactions, searchQuery, selectedType, sortOrder) {
        var filtered = transactions
        
        // Filtrar por tipo
        if (selectedType != "Todos") {
            filtered = filtered.filter { it.type == selectedType }
        }
        
        // Filtrar por búsqueda
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { transaction ->
                transaction.description.contains(searchQuery, ignoreCase = true) ||
                transaction.amount.toString().contains(searchQuery)
            }
        }
        
        // Ordenar
        when (sortOrder) {
            "Fecha (Reciente)" -> filtered.sortedByDescending { it.date }
            "Fecha (Antiguo)" -> filtered.sortedBy { it.date }
            "Monto (Mayor)" -> filtered.sortedByDescending { it.amount }
            "Monto (Menor)" -> filtered.sortedBy { it.amount }
            "Nombre (A-Z)" -> filtered.sortedBy { it.description }
            "Nombre (Z-A)" -> filtered.sortedByDescending { it.description }
            else -> filtered.sortedByDescending { it.date }
        }
    }
    
    // Agrupar transacciones por fecha
    val groupedTransactions = remember(filteredTransactions) {
        filteredTransactions.groupBy { transaction ->
            val date = Date(transaction.date)
            SimpleDateFormat("dd MMMM yyyy", Locale("es", "ES")).format(date)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF667eea).copy(alpha = 0.3f),
                        Color(0xFF764ba2).copy(alpha = 0.2f),
                        Color(0xFF1a1a2e).copy(alpha = 0.9f)
                    ),
                    radius = 1200f
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header moderno
            item {
                ModernHistoryHeader()
            }
            
            // Estadísticas de transacciones
            if (filteredTransactions.isNotEmpty()) {
                item {
                    ModernTransactionStats(
                        transactions = filteredTransactions,
                        onAnalyticsClick = { showAnalytics = true }
                    )
                }
            }
            
            // Búsqueda y filtros con glassmorphism
            item {
                ModernSearchAndFilters(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedType = selectedType,
                    onTypeChange = { selectedType = it },
                    sortOrder = sortOrder,
                    onSortOrderChange = { sortOrder = it },
                    showFilters = showFilters,
                    onToggleFilters = { showFilters = !showFilters }
                )
            }
            
            // Indicador de resultados
            if (filteredTransactions.isNotEmpty()) {
                item {
                    ModernResultsIndicator(
                        totalResults = filteredTransactions.size,
                        hasFilters = selectedType != "Todos" || searchQuery.isNotBlank()
                    )
                }
            }
            
            // Lista de transacciones agrupadas
            if (groupedTransactions.isNotEmpty()) {
                groupedTransactions.forEach { (date, dayTransactions) ->
                    item {
                        ModernDateHeader(date = date, transactionCount = dayTransactions.size)
                    }
                    
                    items(dayTransactions) { transaction ->
                        ModernTransactionItem(
                            transaction = transaction,
                            onEdit = { 
                                println("🔧 Preparando edición de: ${transaction.description}")
                                transactionToEdit = transaction
                                // Cargar datos en el formulario
                                editDescription = transaction.description
                                editAmount = transaction.amount.toString()
                                editType = transaction.type
                                showEditDialog = true
                            },
                            onDelete = { 
                                println("🗑️ Confirmando eliminación de: ${transaction.description}")
                                transactionToDelete = transaction
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            } else {
                item {
                    ModernEmptyHistoryState(
                        hasSearch = searchQuery.isNotBlank(),
                        onClearSearch = { searchQuery = "" }
                    )
                }
            }
        }
        
        // Analytics Modal
        if (showAnalytics) {
            ModernAnalyticsModal(
                transactions = filteredTransactions,
                onClose = { showAnalytics = false }
            )
        }
        
        // Delete Confirmation Dialog
        if (showDeleteDialog && transactionToDelete != null) {
            AlertDialog(
                onDismissRequest = { 
                    showDeleteDialog = false
                    transactionToDelete = null
                },
                title = {
                    Text(
                        text = "Confirmar Eliminación",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "¿Estás seguro de que deseas eliminar la transacción '${transactionToDelete!!.description}'?\n\nEsta acción no se puede deshacer.",
                        color = Color.White.copy(alpha = 0.9f)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            transactionToDelete?.let { transaction ->
                                println("✅ Eliminando transacción: ${transaction.description}")
                                // Eliminar de la base de datos usando corrutina
                                coroutineScope.launch {
                                    try {
                                        db.transactionDao().delete(transaction)
                                        println("✅ Transacción eliminada exitosamente")
                                        // Forzar recarga de transacciones
                                        reloadTrigger++
                                    } catch (e: Exception) {
                                        println("❌ Error al eliminar: ${e.message}")
                                    }
                                }
                            }
                            showDeleteDialog = false
                            transactionToDelete = null
                        }
                    ) {
                        Text(
                            text = "Eliminar",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            transactionToDelete = null
                        }
                    ) {
                        Text(
                            text = "Cancelar",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                containerColor = Color(0xFF1a1a2e).copy(alpha = 0.95f),
                iconContentColor = Color.White,
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }
        
        // Edit Dialog
        if (showEditDialog && transactionToEdit != null) {
            AlertDialog(
                onDismissRequest = { 
                    showEditDialog = false
                    transactionToEdit = null
                },
                title = {
                    Text(
                        text = "Editar Transacción",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Campo descripción
                        OutlinedTextField(
                            value = editDescription,
                            onValueChange = { editDescription = it },
                            label = { 
                                Text(
                                    text = "Descripción",
                                    color = Color.White.copy(alpha = 0.7f)
                                ) 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CashFlowPrimary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                cursorColor = CashFlowPrimary
                            ),
                            singleLine = true
                        )
                        
                        // Campo monto
                        OutlinedTextField(
                            value = editAmount,
                            onValueChange = { newValue ->
                                // Solo permitir números y punto decimal
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    editAmount = newValue
                                }
                            },
                            label = { 
                                Text(
                                    text = "Monto",
                                    color = Color.White.copy(alpha = 0.7f)
                                ) 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CashFlowPrimary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                cursorColor = CashFlowPrimary
                            ),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Decimal
                            ),
                            singleLine = true
                        )
                        
                        // Selector de tipo
                        Text(
                            text = "Tipo de transacción",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Ingreso", "Gasto").forEach { type ->
                                FilterChip(
                                    selected = editType == type,
                                    onClick = { editType = type },
                                    label = { 
                                        Text(
                                            text = type,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CashFlowPrimary,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color.White.copy(alpha = 0.1f),
                                        labelColor = Color.White.copy(alpha = 0.8f)
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            transactionToEdit?.let { originalTransaction ->
                                val updatedAmount = editAmount.toDoubleOrNull()
                                if (editDescription.isNotBlank() && updatedAmount != null && updatedAmount > 0) {
                                    val updatedTransaction = originalTransaction.copy(
                                        description = editDescription.trim(),
                                        amount = updatedAmount,
                                        type = editType
                                    )
                                    
                                    coroutineScope.launch {
                                        try {
                                            db.transactionDao().update(updatedTransaction)
                                            println("✅ Transacción actualizada exitosamente: ${updatedTransaction.description}")
                                            // Forzar recarga de transacciones
                                            reloadTrigger++
                                        } catch (e: Exception) {
                                            println("❌ Error al actualizar: ${e.message}")
                                        }
                                    }
                                    
                                    showEditDialog = false
                                    transactionToEdit = null
                                } else {
                                    println("❌ Por favor completa todos los campos correctamente")
                                }
                            }
                        },
                        enabled = editDescription.isNotBlank() && 
                                editAmount.isNotBlank() && 
                                editAmount.toDoubleOrNull() != null &&
                                editAmount.toDoubleOrNull()!! > 0
                    ) {
                        Text(
                            text = "Guardar",
                            color = if (editDescription.isNotBlank() && 
                                      editAmount.isNotBlank() && 
                                      editAmount.toDoubleOrNull() != null &&
                                      editAmount.toDoubleOrNull()!! > 0) 
                                CashFlowPrimary else Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showEditDialog = false
                            transactionToEdit = null
                        }
                    ) {
                        Text(
                            text = "Cancelar",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                containerColor = Color(0xFF1a1a2e).copy(alpha = 0.95f),
                iconContentColor = Color.White,
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }
    }
}

@Composable
fun ModernTransactionItem(
    transaction: Transaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = transaction.type == "Ingreso"
    val transactionColor = if (isIncome) Color(0xFF4CAF50) else Color(0xFFE57373)
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de transacción con glassmorphism
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                transactionColor.copy(alpha = 0.2f),
                                transactionColor.copy(alpha = 0.05f)
                            ),
                            radius = 60f
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) 
                        LucideIconMapper.getTransactionTypeIcon("Ingreso")
                    else 
                        LucideIconMapper.getTransactionTypeIcon("Gasto"),
                    contentDescription = transaction.type,
                    tint = transactionColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información de la transacción
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = SimpleDateFormat("dd MMM, HH:mm", Locale("es", "ES"))
                        .format(Date(transaction.date)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                
                // Monto con estilo mejorado
                Text(
                    text = "${if (isIncome) "+" else "-"}${currencyFormat.format(transaction.amount)}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = transactionColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Botones de acción con glassmorphism
            Column {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = LucideIconMapper.getNavigationIcon("Edit"),
                            contentDescription = "Editar",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = LucideIconMapper.getNavigationIcon("Delete"),
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernHistoryHeader() {
    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono principal con glassmorphism
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF667eea).copy(alpha = 0.3f),
                                Color(0xFF764ba2).copy(alpha = 0.1f)
                            ),
                            radius = 60f
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LucideIconMapper.getNavigationIcon("History"),
                    contentDescription = "Historial",
                    tint = Color(0xFF667eea),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            // Título y descripción
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Historial de Transacciones",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Text(
                    text = "Visualiza y gestiona todas tus transacciones",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ModernTransactionStats(
    transactions: List<Transaction>,
    onClose: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {}
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    
    // Calcular estadísticas
    val totalIncome = transactions.filter { it.type == "Ingreso" }.sumOf { it.amount }
    val totalExpenses = transactions.filter { it.type == "Gasto" }.sumOf { it.amount }
    val netAmount = totalIncome - totalExpenses
    
    GlassCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header del resumen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Resumen Financiero",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Balance actual de tus transacciones",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = LucideIconMapper.getNavigationIcon("Close"),
                        contentDescription = "Cerrar",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Tarjetas principales de resumen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Ingresos
                ImprovedStatsCard(
                    title = "Ingresos",
                    value = currencyFormat.format(totalIncome),
                    count = transactions.count { it.type == "Ingreso" },
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                
                // Gastos
                ImprovedStatsCard(
                    title = "Gastos", 
                    value = currencyFormat.format(totalExpenses),
                    count = transactions.count { it.type == "Gasto" },
                    color = Color(0xFFE57373),
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Balance total
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = if (netAmount >= 0) {
                                listOf(
                                    Color(0xFF4CAF50).copy(alpha = 0.2f),
                                    Color(0xFF81C784).copy(alpha = 0.1f)
                                )
                            } else {
                                listOf(
                                    Color(0xFFE57373).copy(alpha = 0.2f),
                                    Color(0xFFEF5350).copy(alpha = 0.1f)
                                )
                            }
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Balance Total",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = currencyFormat.format(netAmount),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (netAmount >= 0) Color(0xFF4CAF50) else Color(0xFFE57373),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Icon(
                        imageVector = if (netAmount >= 0) 
                            LucideIconMapper.getTransactionTypeIcon("Ingreso") 
                        else 
                            LucideIconMapper.getTransactionTypeIcon("Gasto"),
                        contentDescription = null,
                        tint = if (netAmount >= 0) Color(0xFF4CAF50) else Color(0xFFE57373),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            }
        }
    }


@Composable
fun ImprovedStatsCard(
    title: String,
    value: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.15f),
                        color.copy(alpha = 0.05f)
                    ),
                    radius = 100f
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "$count transacciones",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.15f),
                        color.copy(alpha = 0.05f)
                    ),
                    radius = 100f
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable 
fun DetailedStatItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = Color.White
        )
    }
}

@Composable
fun ModernSearchAndFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedType: String,
    onTypeChange: (String) -> Unit,
    sortOrder: String,
    onSortOrderChange: (String) -> Unit,
    showFilters: Boolean,
    onToggleFilters: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search bar with glassmorphism
        GlassCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = LucideIconMapper.getNavigationIcon("Search"),
                    contentDescription = "Buscar",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Buscar transacciones...",
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White.copy(alpha = 0.3f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        cursorColor = CashFlowPrimary
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { keyboardController?.hide() }
                    ),
                    singleLine = true
                )
                
                IconButton(
                    onClick = { onToggleFilters() }
                ) {
                    Icon(
                        imageVector = LucideIconMapper.getNavigationIcon("Filter"),
                        contentDescription = "Filtros",
                        tint = if (selectedType != "Todos" || sortOrder != "Fecha (Reciente)") 
                            CashFlowPrimary else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        // Filters section
        AnimatedVisibility(
            visible = showFilters,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            GlassCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Type filter
                    Text(
                        text = "Tipo de Transacción",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf("Todos", "Ingreso", "Gasto")) { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { onTypeChange(type) },
                                label = { 
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (type) {
                                                "Todos" -> LucideIconMapper.getNavigationIcon("List")
                                                "Ingreso" -> LucideIconMapper.getTransactionTypeIcon("Ingreso")
                                                "Gasto" -> LucideIconMapper.getTransactionTypeIcon("Gasto")
                                                else -> LucideIconMapper.getNavigationIcon("List")
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = type,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CashFlowPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White.copy(alpha = 0.1f),
                                    labelColor = Color.White.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                    
                    // Sort order
                    Text(
                        text = "Ordenar por",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White
                    )
                    
                    val sortOptions = listOf(
                        "Fecha (Reciente)", "Fecha (Antiguo)",
                        "Monto (Mayor)", "Monto (Menor)",
                        "Nombre (A-Z)", "Nombre (Z-A)"
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sortOptions) { option ->
                            FilterChip(
                                selected = sortOrder == option,
                                onClick = { onSortOrderChange(option) },
                                label = { 
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (option) {
                                                "Fecha (Reciente)" -> LucideIconMapper.getNavigationIcon("CalendarDown")
                                                "Fecha (Antiguo)" -> LucideIconMapper.getNavigationIcon("CalendarUp")
                                                "Monto (Mayor)" -> LucideIconMapper.getNavigationIcon("ArrowUp")
                                                "Monto (Menor)" -> LucideIconMapper.getNavigationIcon("ArrowDown")
                                                "Nombre (A-Z)" -> LucideIconMapper.getNavigationIcon("ArrowUp")
                                                "Nombre (Z-A)" -> LucideIconMapper.getNavigationIcon("ArrowDown")
                                                else -> LucideIconMapper.getNavigationIcon("Sort")
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = option, 
                                            maxLines = 1,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CashFlowPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White.copy(alpha = 0.1f),
                                    labelColor = Color.White.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernResultsIndicator(
    totalResults: Int,
    hasFilters: Boolean
) {
    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = LucideIconMapper.getNavigationIcon("History"),
                    contentDescription = "Resultados",
                    tint = Color(0xFF667eea),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "$totalResults ${if (totalResults == 1) "transacción" else "transacciones"}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (hasFilters) {
                    Text(
                        text = "• Filtrado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CashFlowPrimary
                    )
                }
            }
            
            // Export button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color.White.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LucideIconMapper.getNavigationIcon("Download"),
                    contentDescription = "Exportar",
                    tint = Color(0xFF667eea),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ModernEmptyHistoryState(
    hasSearch: Boolean,
    onClearSearch: () -> Unit
) {
    GlassCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated icon
            val infiniteTransition = rememberInfiniteTransition(label = "empty_animation")
            val animatedAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha_animation"
            )
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF667eea).copy(alpha = 0.2f),
                                Color(0xFF667eea).copy(alpha = 0.05f)
                            ),
                            radius = 100f
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (hasSearch) LucideIconMapper.getNavigationIcon("Search") else LucideIconMapper.getNavigationIcon("Receipt"),
                    contentDescription = if (hasSearch) "Sin resultados" else "Sin transacciones",
                    tint = Color(0xFF667eea).copy(alpha = animatedAlpha),
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Text(
                text = if (hasSearch) "Sin resultados" else "No hay transacciones",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = if (hasSearch) 
                    "No encontramos transacciones que coincidan con tu búsqueda" 
                else 
                    "Aún no tienes transacciones registradas. ¡Comienza añadiendo tu primera transacción!",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
            
            if (hasSearch) {
                OutlinedButton(
                    onClick = onClearSearch,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = LucideIconMapper.getNavigationIcon("Close"),
                        contentDescription = "Limpiar búsqueda",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Limpiar búsqueda")
                }
            }
        }
    }
}

@Composable 
fun ModernDateHeader(
    date: String,
    transactionCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = Color.White.copy(alpha = 0.9f)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "($transactionCount)",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Box(
            modifier = Modifier
                .height(1.dp)
                .weight(1f)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun ModernAnalyticsModal(
    transactions: List<Transaction>,
    onClose: () -> Unit
) {
    // Analytics modal content would go here
    // For now, just show a placeholder
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Analytics Detallado",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = LucideIconMapper.getNavigationIcon("Close"),
                            contentDescription = "Cerrar",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Próximamente: Analytics detallado con gráficos y estadísticas avanzadas",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
