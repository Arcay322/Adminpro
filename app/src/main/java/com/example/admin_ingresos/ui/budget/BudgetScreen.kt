package com.example.admin_ingresos.ui.budget

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin_ingresos.AppDatabaseProvider
import com.example.admin_ingresos.data.*
import com.example.admin_ingresos.ui.theme.*
import com.example.admin_ingresos.ui.components.*
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen() {
    val context = LocalContext.current
    val database = remember { AppDatabaseProvider.getDatabase(context) }
    val viewModel: BudgetViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(
                database = database,
                notificationService = NotificationService(context),
                preferencesManager = PreferencesManager(context),
                context = context
            ) as T
        }
    })
    
    val uiState by viewModel.uiState.collectAsState()
    val budgetProgress by viewModel.budgetProgress.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "CO")) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BackgroundStart,
                        BackgroundEnd
                    )
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header moderno con glassmorphism
        item {
            ModernBudgetHeader(
                onCreateBudget = { viewModel.showCreateDialog() },
                totalBudgets = budgetProgress.size,
                activeBudgets = budgetProgress.count { it.budget.isActive },
                overBudgets = budgetProgress.count { it.isOverBudget }
            )
        }
        
        // Budget Progress Chart - RESTAURADO CON GLASSMORPHISM
        if (budgetProgress.isNotEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = GlassWhite,
                    cornerRadius = 20.dp
                ) {
                    BudgetProgressChart(
                        budgetProgress = budgetProgress,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // Budget Templates (nueva funcionalidad)
        item {
            BudgetTemplatesSection(
                categories = categories,
                onCreateFromTemplate = { categoryId, amount, period ->
                    viewModel.createBudget(categoryId, amount, period)
                }
            )
        }
        
        // Smart Insights (nueva funcionalidad)
        if (budgetProgress.isNotEmpty()) {
            item {
                SmartBudgetInsights(
                    budgetProgress = budgetProgress,
                    formatter = formatter
                )
            }
        }
        
        // Budget Progress Cards con glassmorphism
        if (budgetProgress.isEmpty()) {
            item {
                EmptyBudgetState(
                    onCreateBudget = { viewModel.showCreateDialog() }
                )
            }
        } else {
            items(budgetProgress) { progress ->
                EnhancedBudgetCard(
                    budgetProgress = progress,
                    formatter = formatter,
                    onEdit = { viewModel.showEditDialog(progress.budget) },
                    onDelete = { viewModel.deleteBudget(progress.budget) },
                    onToggleActive = { viewModel.deactivateBudget(progress.budget.id) }
                )
            }
        }
    }
    
    // Create Budget Dialog
    if (uiState.showCreateDialog) {
        ModernCreateBudgetDialog(
            categories = categories,
            onDismiss = { viewModel.hideCreateDialog() },
            onCreateBudget = { categoryId, amount, period ->
                viewModel.createBudget(categoryId, amount, period)
            },
            isLoading = uiState.isLoading
        )
    }
    
    // Edit Budget Dialog
    uiState.editingBudget?.let { budget ->
        ModernEditBudgetDialog(
            budget = budget,
            categoryName = "Categoría", // TODO: Get category name from budget
            onDismiss = { viewModel.hideEditDialog() },
            onConfirm = { newAmount ->
                viewModel.updateBudgetAmount(budget.id, newAmount)
            }
        )
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // TODO: Show snackbar
        }
    }
}

@Composable
fun BudgetProgressCard(
    budgetProgress: BudgetProgress,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val progressPercentage = if (budgetProgress.budget.amount > 0) {
        (budgetProgress.spent / budgetProgress.budget.amount * 100).coerceAtMost(100.0)
    } else 0.0
    
    // Determine progress color based on percentage
    val progressColor = when {
        progressPercentage >= 90 -> MaterialTheme.colorScheme.error
        progressPercentage >= 75 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFF4CAF50) // Green
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CashFlowPrimary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header with category and actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category icon with background
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    progressColor.copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = budgetProgress.category.icon,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = budgetProgress.category.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = budgetProgress.budget.period.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = CashFlowPrimary
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress section with modern design
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Gastado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "$${String.format("%.2f", budgetProgress.spent)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = progressColor
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Presupuesto",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "$${String.format("%.2f", budgetProgress.budget.amount)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Progress bar with rounded corners and gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(4.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = (progressPercentage / 100).toFloat())
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            progressColor,
                                            progressColor.copy(alpha = 0.8f)
                                        )
                                    ),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Progress percentage and remaining amount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${String.format("%.1f", progressPercentage)}% utilizado",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = progressColor
                        )
                        val remaining = budgetProgress.budget.amount - budgetProgress.spent
                        Text(
                            text = if (remaining >= 0) "Restante: $${String.format("%.2f", remaining)}" 
                                  else "Excedido: $${String.format("%.2f", -remaining)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = if (remaining >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBudgetDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onCreateBudget: (categoryId: Int, amount: Double, period: BudgetPeriod) -> Unit,
    isLoading: Boolean = false
) {
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var amount by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf(BudgetPeriod.MONTHLY) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showPeriodDropdown by remember { mutableStateOf(false) }
    
    // Validation
    val amountError = when {
        amount.isBlank() -> "El monto es requerido"
        amount.toDoubleOrNull() == null -> "Ingresa un monto válido"
        amount.toDoubleOrNull()!! <= 0 -> "El monto debe ser mayor a 0"
        else -> null
    }
    
    val categoryError = if (selectedCategoryId == null) "Selecciona una categoría" else null
    
    val isFormValid = amountError == null && categoryError == null && !isLoading
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Crear Presupuesto",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category selector
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = it }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.name ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                        isError = categoryError != null,
                        supportingText = categoryError?.let { { Text(it) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = category.icon,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(category.name)
                                    }
                                },
                                onClick = {
                                    selectedCategoryId = category.id
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
                
                // Amount field
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monto del presupuesto") },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Text("$", style = MaterialTheme.typography.titleMedium) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    isError = amountError != null,
                    supportingText = amountError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Period selector
                ExposedDropdownMenuBox(
                    expanded = showPeriodDropdown,
                    onExpandedChange = { showPeriodDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedPeriod.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Período") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPeriodDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showPeriodDropdown,
                        onDismissRequest = { showPeriodDropdown = false }
                    ) {
                        BudgetPeriod.values().forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period.displayName) },
                                onClick = {
                                    selectedPeriod = period
                                    showPeriodDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedCategoryId?.let { categoryId ->
                        amount.toDoubleOrNull()?.let { amountValue ->
                            onCreateBudget(categoryId, amountValue, selectedPeriod)
                        }
                    }
                },
                enabled = isFormValid
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Crear")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// =================== NUEVOS COMPONENTES CON GLASSMORPHISM ===================

@Composable
private fun ModernBudgetHeader(
    onCreateBudget: () -> Unit,
    totalBudgets: Int,
    activeBudgets: Int,
    overBudgets: Int
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = GlassWhiteStrong,
        cornerRadius = 24.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "💰 Presupuestos",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "Controla tus gastos inteligentemente",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
                
                VibrantFAB(
                    onClick = onCreateBudget,
                    icon = LucideIconMapper.Navigation.add,
                    size = 56.dp
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickStatCard(
                    title = "Total",
                    value = totalBudgets.toString(),
                    icon = LucideIconMapper.Navigation.transactions,
                    color = AccentVibrantStart,
                    modifier = Modifier.weight(1f)
                )
                
                QuickStatCard(
                    title = "Activos",
                    value = activeBudgets.toString(),
                    icon = LucideIconMapper.Navigation.success,
                    color = IncomeGreen,
                    modifier = Modifier.weight(1f)
                )
                
                QuickStatCard(
                    title = "Excedidos",
                    value = overBudgets.toString(),
                    icon = LucideIconMapper.Navigation.warning,
                    color = ExpenseRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        backgroundColor = GlassWhiteSubtle,
        cornerRadius = 16.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.2f))
                    .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Text(
                text = title,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun BudgetAnalyticsDashboard(
    budgetProgress: List<BudgetProgress>,
    formatter: NumberFormat
) {
    val totalBudgeted = budgetProgress.sumOf { it.budget.amount }
    val totalSpent = budgetProgress.sumOf { it.spent }
    val totalRemaining = budgetProgress.sumOf { it.remaining }
    val averageProgress = budgetProgress.map { it.percentage }.average().toFloat()
    
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = GlassWhite,
        cornerRadius = 20.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Analytics Dashboard",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Icon(
                    imageVector = LucideIconMapper.Navigation.reports,
                    contentDescription = null,
                    tint = AccentVibrantStart,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnalyticsMetricCard(
                    title = "Presupuestado",
                    amount = totalBudgeted,
                    formatter = formatter,
                    color = AccentVibrantStart,
                    modifier = Modifier.weight(1f)
                )
                
                AnalyticsMetricCard(
                    title = "Gastado",
                    amount = totalSpent,
                    formatter = formatter,
                    color = ExpenseRed,
                    modifier = Modifier.weight(1f)
                )
                
                AnalyticsMetricCard(
                    title = "Disponible",
                    amount = totalRemaining,
                    formatter = formatter,
                    color = IncomeGreen,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress general
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progreso General",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${(averageProgress * 100).toInt()}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            averageProgress >= 0.9f -> ExpenseRed
                            averageProgress >= 0.7f -> Color(0xFFFBBF24)
                            else -> IncomeGreen
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = averageProgress.coerceAtMost(1f),
                    modifier = Modifier.fillMaxWidth(),
                    color = when {
                        averageProgress >= 0.9f -> ExpenseRed
                        averageProgress >= 0.7f -> Color(0xFFFBBF24)
                        else -> IncomeGreen
                    },
                    trackColor = GlassWhiteSubtle
                )
            }
        }
    }
}

@Composable
private fun AnalyticsMetricCard(
    title: String,
    amount: Double,
    formatter: NumberFormat,
    color: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        backgroundColor = color.copy(alpha = 0.1f),
        borderColor = color.copy(alpha = 0.3f),
        cornerRadius = 12.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Text(
                text = formatter.format(amount),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BudgetTemplatesSection(
    categories: List<Category>,
    onCreateFromTemplate: (Int, Double, BudgetPeriod) -> Unit
) {
    val templates = remember {
        listOf(
            BudgetTemplate("Alimentación", LucideIconMapper.getIconFromCategoryName("alimentación"), 300000.0, BudgetPeriod.MONTHLY),
            BudgetTemplate("Transporte", LucideIconMapper.getIconFromCategoryName("transporte"), 150000.0, BudgetPeriod.MONTHLY),
            BudgetTemplate("Entretenimiento", LucideIconMapper.getIconFromCategoryName("entretenimiento"), 100000.0, BudgetPeriod.MONTHLY),
            BudgetTemplate("Compras", LucideIconMapper.getIconFromCategoryName("compras"), 200000.0, BudgetPeriod.MONTHLY)
        )
    }
    
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = GlassWhite,
        cornerRadius = 20.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Plantillas Rápidas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Icon(
                    imageVector = LucideIconMapper.Navigation.add,
                    contentDescription = null,
                    tint = AccentVibrantStart,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(templates) { template ->
                    BudgetTemplateCard(
                        template = template,
                        onUseTemplate = {
                            // Buscar categoria por nombre con mayor flexibilidad
                            val category = categories.find { category ->
                                category.name.contains(template.name, ignoreCase = true) ||
                                template.name.contains(category.name, ignoreCase = true) ||
                                when (template.name.lowercase()) {
                                    "alimentación" -> category.name.lowercase().contains("comida") || 
                                                    category.name.lowercase().contains("alimentación") ||
                                                    category.name.lowercase().contains("restaurant") ||
                                                    category.name.lowercase().contains("supermercado")
                                    "transporte" -> category.name.lowercase().contains("transporte") ||
                                                   category.name.lowercase().contains("gasolina") ||
                                                   category.name.lowercase().contains("taxi") ||
                                                   category.name.lowercase().contains("uber")
                                    "entretenimiento" -> category.name.lowercase().contains("entretenimiento") ||
                                                        category.name.lowercase().contains("diversión") ||
                                                        category.name.lowercase().contains("ocio") ||
                                                        category.name.lowercase().contains("cine")
                                    "compras" -> category.name.lowercase().contains("compras") ||
                                                category.name.lowercase().contains("shopping") ||
                                                category.name.lowercase().contains("ropa") ||
                                                category.name.lowercase().contains("tienda")
                                    else -> false
                                }
                            } ?: categories.firstOrNull() // Usar la primera categoría como fallback
                            
                            category?.let {
                                onCreateFromTemplate(it.id, template.suggestedAmount, template.period)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetTemplateCard(
    template: BudgetTemplate,
    onUseTemplate: () -> Unit
) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "CO")) }
    
    GlassCard(
        modifier = Modifier
            .width(140.dp)
            .clickable { onUseTemplate() },
        backgroundColor = GlassWhiteSubtle,
        cornerRadius = 16.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentVibrantStart.copy(alpha = 0.2f))
                    .border(1.dp, AccentVibrantStart.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = template.icon,
                    contentDescription = null,
                    tint = AccentVibrantStart,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Text(
                text = template.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = formatter.format(template.suggestedAmount),
                fontSize = 11.sp,
                color = AccentVibrantStart,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = template.period.displayName,
                fontSize = 10.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun SmartBudgetInsights(
    budgetProgress: List<BudgetProgress>,
    formatter: NumberFormat
) {
    val insights = remember(budgetProgress) {
        generateBudgetInsights(budgetProgress)
    }
    
    if (insights.isEmpty()) return
    
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = GlassWhite,
        cornerRadius = 20.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Insights Inteligentes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Icon(
                    imageVector = LucideIconMapper.Navigation.info,
                    contentDescription = null,
                    tint = AccentVibrantStart,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            insights.forEach { insight ->
                SmartInsightCard(insight = insight)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SmartInsightCard(insight: BudgetInsight) {
    val backgroundColor = when (insight.type) {
        InsightType.WARNING -> ExpenseRed.copy(alpha = 0.1f)
        InsightType.SUCCESS -> IncomeGreen.copy(alpha = 0.1f)
        InsightType.INFO -> AccentVibrantStart.copy(alpha = 0.1f)
    }
    
    val borderColor = when (insight.type) {
        InsightType.WARNING -> ExpenseRed.copy(alpha = 0.3f)
        InsightType.SUCCESS -> IncomeGreen.copy(alpha = 0.3f)
        InsightType.INFO -> AccentVibrantStart.copy(alpha = 0.3f)
    }
    
    val iconColor = when (insight.type) {
        InsightType.WARNING -> ExpenseRed
        InsightType.SUCCESS -> IncomeGreen
        InsightType.INFO -> AccentVibrantStart
    }
    
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        cornerRadius = 12.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = insight.icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = insight.description,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun EnhancedBudgetCard(
    budgetProgress: BudgetProgress,
    formatter: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    val progressColor = when {
        budgetProgress.percentage >= 1.0f -> ExpenseRed
        budgetProgress.percentage >= 0.8f -> Color(0xFFFBBF24)
        else -> IncomeGreen
    }
    
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = GlassWhite,
        cornerRadius = 20.dp
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(progressColor.copy(alpha = 0.2f))
                            .border(1.dp, progressColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIconMapper.getCategoryIcon(budgetProgress.category),
                            contentDescription = null,
                            tint = progressColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = budgetProgress.category.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = budgetProgress.budget.period.displayName,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                
                // Menu
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = LucideIconMapper.Navigation.menu,
                            contentDescription = "Opciones",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
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
                                Icon(LucideIconMapper.Navigation.edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (budgetProgress.budget.isActive) "Desactivar" else "Activar") },
                            onClick = {
                                showMenu = false
                                onToggleActive()
                            },
                            leadingIcon = {
                                Icon(
                                    if (budgetProgress.budget.isActive) LucideIconMapper.Navigation.close 
                                    else LucideIconMapper.Navigation.check,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(LucideIconMapper.Navigation.delete, contentDescription = null)
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Gastado",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = formatter.format(budgetProgress.spent),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Presupuesto",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = formatter.format(budgetProgress.budget.amount),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = budgetProgress.percentage.coerceAtMost(1f),
                modifier = Modifier.fillMaxWidth(),
                color = progressColor,
                trackColor = GlassWhiteSubtle
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Bottom info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(budgetProgress.percentage * 100).toInt()}% utilizado",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = progressColor
                )
                
                Text(
                    text = if (budgetProgress.remaining >= 0) 
                        "Restante: ${formatter.format(budgetProgress.remaining)}" 
                    else 
                        "Excedido: ${formatter.format(-budgetProgress.remaining)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (budgetProgress.remaining >= 0) IncomeGreen else ExpenseRed
                )
            }
            
            // Days remaining
            if (budgetProgress.daysRemaining > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = LucideIconMapper.Navigation.clock,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${budgetProgress.daysRemaining} días restantes",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyBudgetState(
    onCreateBudget: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = GlassWhite,
        cornerRadius = 24.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = LucideIconMapper.Navigation.reports,
                contentDescription = null,
                tint = AccentVibrantStart,
                modifier = Modifier.size(64.dp)
            )
            
            Text(
                text = "No tienes presupuestos activos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Crea tu primer presupuesto para controlar tus gastos y alcanzar tus metas financieras",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            
            Button(
                onClick = onCreateBudget,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentVibrantStart
                )
            ) {
                Icon(
                    imageVector = LucideIconMapper.Navigation.add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear Mi Primer Presupuesto")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernCreateBudgetDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onCreateBudget: (categoryId: Int, amount: Double, period: BudgetPeriod) -> Unit,
    isLoading: Boolean = false
) {
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var amount by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf(BudgetPeriod.MONTHLY) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showPeriodDropdown by remember { mutableStateOf(false) }
    
    // Validation
    val amountError = when {
        amount.isBlank() -> "El monto es requerido"
        amount.toDoubleOrNull() == null -> "Ingresa un monto válido"
        amount.toDoubleOrNull()!! <= 0 -> "El monto debe ser mayor a 0"
        else -> null
    }
    
    val categoryError = if (selectedCategoryId == null) "Selecciona una categoría" else null
    val isFormValid = amountError == null && categoryError == null && !isLoading
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Crear Presupuesto",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category selector
                var showCategoryDropdown by remember { mutableStateOf(false) }
                Column {
                    Text("Categoría", style = MaterialTheme.typography.labelMedium)
                    Button(
                        onClick = { showCategoryDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text(
                            text = categories.find { it.id == selectedCategoryId }?.name ?: "Selecciona una categoría",
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    
                    if (categoryError != null) {
                        Text(
                            text = categoryError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = category.icon,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(category.name)
                                    }
                                },
                                onClick = {
                                    selectedCategoryId = category.id
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
                
                // Amount field
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monto del presupuesto") },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Text("$", style = MaterialTheme.typography.titleMedium) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    isError = amountError != null,
                    supportingText = amountError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Period selector
                ExposedDropdownMenuBox(
                    expanded = showPeriodDropdown,
                    onExpandedChange = { showPeriodDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedPeriod.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Período") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPeriodDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showPeriodDropdown,
                        onDismissRequest = { showPeriodDropdown = false }
                    ) {
                        BudgetPeriod.values().forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period.displayName) },
                                onClick = {
                                    selectedPeriod = period
                                    showPeriodDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedCategoryId?.let { categoryId ->
                        amount.toDoubleOrNull()?.let { amountValue ->
                            onCreateBudget(categoryId, amountValue, selectedPeriod)
                        }
                    }
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentVibrantStart
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Crear")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}

// =================== DATA CLASSES ===================

data class BudgetTemplate(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val suggestedAmount: Double,
    val period: BudgetPeriod
)

data class BudgetInsight(
    val title: String,
    val description: String,
    val type: InsightType,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

enum class InsightType {
    WARNING, SUCCESS, INFO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernEditBudgetDialog(
    budget: Budget,
    categoryName: String = "Categoría",
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf(budget.amount.toString()) }
    val isFormValid = amount.toDoubleOrNull()?.let { it > 0 } == true

    Dialog(onDismissRequest = onDismiss) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Editar Presupuesto",
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextPrimary
                            )
                            Text(
                                text = categoryName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        
                        Icon(
                            imageVector = LucideIconMapper.getNavigationIcon("edit"),
                            contentDescription = null,
                            tint = AccentVibrantStart,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    HorizontalDivider(
                        color = TextSecondary.copy(alpha = 0.1f),
                    thickness = 1.dp
                )
                
                // Current info
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Presupuesto Actual",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = DecimalFormat("#,##0.00").format(budget.amount),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Text(
                            text = budget.period.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                // New amount input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            amount = it
                        }
                    },
                    label = { Text("Nuevo Monto") },
                    leadingIcon = {
                        Icon(
                            imageVector = LucideIconMapper.getNavigationIcon("dollarSign"),
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    isError = amount.isNotEmpty() && !isFormValid,
                    supportingText = if (amount.isNotEmpty() && !isFormValid) {
                        { Text("Ingresa un monto válido mayor a 0") }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                )
                
                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            amount.toDoubleOrNull()?.let { amountValue ->
                                onConfirm(amountValue)
                            }
                        },
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = LucideIconMapper.getNavigationIcon("check"),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Actualizar")
                    }
                }
            }
        }
    }
}

// =================== HELPER FUNCTIONS ===================

private fun generateBudgetInsights(budgetProgress: List<BudgetProgress>): List<BudgetInsight> {
    val insights = mutableListOf<BudgetInsight>()
    
    // Check for overspending
    val overBudgets = budgetProgress.filter { it.isOverBudget }
    if (overBudgets.isNotEmpty()) {
        insights.add(
            BudgetInsight(
                title = "Presupuestos Excedidos",
                description = "Tienes ${overBudgets.size} presupuesto${if (overBudgets.size > 1) "s" else ""} excedido${if (overBudgets.size > 1) "s" else ""}",
                type = InsightType.WARNING,
                icon = LucideIconMapper.Navigation.warning
            )
        )
    }
    
    // Check for good savings
    val wellManagedBudgets = budgetProgress.filter { it.percentage <= 0.7f && it.budget.isActive }
    if (wellManagedBudgets.isNotEmpty()) {
        insights.add(
            BudgetInsight(
                title = "Buen Control",
                description = "Mantienes ${wellManagedBudgets.size} presupuesto${if (wellManagedBudgets.size > 1) "s" else ""} bajo control",
                type = InsightType.SUCCESS,
                icon = LucideIconMapper.Navigation.success
            )
        )
    }
    
    // Check for approaching limits
    val approachingLimits = budgetProgress.filter { it.percentage >= 0.8f && it.percentage < 1.0f }
    if (approachingLimits.isNotEmpty()) {
        insights.add(
            BudgetInsight(
                title = "Cerca del Límite",
                description = "${approachingLimits.size} presupuesto${if (approachingLimits.size > 1) "s están" else " está"} cerca del límite",
                type = InsightType.INFO,
                icon = LucideIconMapper.Navigation.clock
            )
        )
    }
    
    return insights
}