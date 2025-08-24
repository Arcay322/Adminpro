package com.example.admin_ingresos.ui.dashboard
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin_ingresos.data.model.SavingsGoal
import com.example.admin_ingresos.ui.components.*
import com.example.admin_ingresos.ui.icons.LucideIconMapper
import com.example.admin_ingresos.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToTransactions: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToReportsSection: (String) -> Unit
) {
    val context = LocalContext.current
    val db = remember { com.example.admin_ingresos.AppDatabaseProvider.getDatabase(context) }
    val repository = remember { com.example.admin_ingresos.data.TransactionRepository(db) }

    val dashboardViewModel: DashboardViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository, db) as T
        }
    })

    val uiState by dashboardViewModel.uiState.collectAsState()
    val weeklyData by dashboardViewModel.weeklyData.collectAsState()
    // Create SavingsGoalViewModel once at screen level to avoid creating it inside nested composables
    val savingsGoalViewModel: com.example.admin_ingresos.viewmodel.SavingsGoalViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return com.example.admin_ingresos.viewmodel.SavingsGoalViewModel(db) as T
        }
    })

    // State to show the add-savings-goal dialog from the Quick Actions
    var showAddGoalTop by remember { mutableStateOf(false) }

    var userName by remember { mutableStateOf("Usuario") }

    var showNotificationsDialog by remember { mutableStateOf(false) }

    var isVisible by remember { mutableStateOf(false) }
    val animatedBalance by animateFloatAsState(
        targetValue = if (isVisible) uiState.currentBalance.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "balance_animation"
    )

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isVisible = true
        }
    }

    // Show Add Savings Goal dialog when triggered from Quick Actions
    if (showAddGoalTop) {
        com.example.admin_ingresos.ui.savings.AddEditSavingsGoalDialog(
            onConfirm = { name, amount, icon, color ->
                savingsGoalViewModel.addSavingsGoal(
                    name = name,
                    targetAmount = amount,
                    emoji = icon,
                    color = color,
                    description = ""
                )
                showAddGoalTop = false
            },
            onDismiss = { showAddGoalTop = false }
        )
    }

    // Notifications dialog
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = { Text("Notificaciones") },
            text = { Text("No hay notificaciones nuevas.") },
            confirmButton = {
                TextButton(onClick = { showNotificationsDialog = false }) { Text("Cerrar") }
            }
        )
    }

    // Profile now has its own screen (navigates to "profile").
    // showProfileDialog removed in favor of navigation.

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .background(color = MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp)
    ) {
        // Encabezado del Dashboard
        item {
            DashboardHeader(
                userName = userName,
                onNotificationClick = { showNotificationsDialog = true },
                onProfileClick = onNavigateToSettings
            )
        }

        // Manejo de estados de carga y error
        if (uiState.isLoading) {
            item {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.Center)
                )
            }
        } else if (uiState.error != null) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = ExpenseRed,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Error al cargar datos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = uiState.error ?: "Error desconocido",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { dashboardViewModel.refreshData() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentVibrantStart
                        )
                    ) {
                        Text("Reintentar")
                    }
                }
            }
        } else {
            // Tarjetas de balance principal
            item {
                AnimatedVisibility(
                    visible = !uiState.isLoading,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn()
                ) {
                                MainBalanceCards(
                                    currentBalance = animatedBalance.toDouble(),
                                    monthlyIncome = uiState.monthlyIncome,
                                    monthlyExpenses = uiState.monthlyExpenses,
                                    monthlyTransfers = uiState.monthlyTransfers,
                                    onViewDetails = { onNavigateToReports() }
                                )
                }
            }

            // Acciones rápidas
            item {
                AnimatedVisibility(
                    visible = !uiState.isLoading,
                    enter = slideInVertically(initialOffsetY = { it / 3 }) + fadeIn()
                ) {
                    QuickActionsSection(
                        onAddSavingsGoal = { showAddGoalTop = true },
                        onViewTransactions = onNavigateToTransactions,
                        onViewReports = onNavigateToReports,
                        onViewBudget = onNavigateToBudget
                    )
                }
            }

            // Gráfico de gastos por categoría (solo si hay datos)
                    if (uiState.categoryExpenses.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = !uiState.isLoading,
                        enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn()
                    ) {
                        ExpensesByCategoryChart(categories = uiState.categoryExpenses, onNavigateToReportsSection = onNavigateToReportsSection)
                    }
                }
            }

            // Tendencias y comparaciones
            item {
                AnimatedVisibility(
                    visible = !uiState.isLoading,
                    enter = slideInVertically(initialOffsetY = { it / 5 }) + fadeIn()
                ) {
                    TrendsAndInsights(
                            monthlyIncome = uiState.monthlyIncome,
                            monthlyExpenses = uiState.monthlyExpenses,
                            incomeChangePercent = uiState.incomeChangePercent,
                            expenseChangePercent = uiState.expenseChangePercent,
                            categoryExpenses = uiState.categoryExpenses
                        )
                }
            }

            // Weekly Flow chart (Ingresos / Gastos / Ahorro)
            if (weeklyData.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = !uiState.isLoading,
                        enter = slideInVertically(initialOffsetY = { it / 5 }) + fadeIn()
                    ) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = GlassWhiteSubtle,
                            cornerRadius = 20.dp
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Header placed at top-left: title then horizontal legend below it
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(
                                            text = "Flujo Semanal",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            LegendItem(color = IncomeGreen, label = "Ingreso")
                                            LegendItem(color = ExpenseRed, label = "Gasto")
                                            LegendItem(color = Color(0xFF42A5F5), label = "Ahorro")
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Compute max value across income/expense/transfers to scale bars
                                val maxVal = weeklyData.maxOfOrNull { maxOf(it.income, it.expense, it.transfers) } ?: 0.0

                                // Chart rows: distribute day groups evenly and keep bars compact
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    weeklyData.forEach { day ->
                                        WeeklyBarChart(day = day, maxValue = if (maxVal > 0) maxVal else 1.0)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Metas de Ahorro (sección)
            item {
                AnimatedVisibility(
                    visible = !uiState.isLoading,
                    enter = slideInVertically(initialOffsetY = { it / 6 }) + fadeIn()
                ) {
                    // Use the SavingsGoalViewModel created at screen level
                    SavingsGoalsSection(savingsGoalViewModel)
                }
            }

            // (Dialog moved below LazyColumn to keep composable invocation in a @Composable scope)

            // Transacciones recientes (solo si hay datos)
            if (uiState.recentTransactions.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = !uiState.isLoading,
                        enter = slideInVertically(initialOffsetY = { it / 6 }) + fadeIn()
                    ) {
                        RecentTransactionsSection(
                            transactions = uiState.recentTransactions,
                            onViewAll = onNavigateToTransactions
                        )
                    }
                }
            } else {
                // Mostrar mensaje cuando no hay transacciones
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = GlassWhiteSubtle
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Sin transacciones",
                                tint = TextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "¡Comienza a registrar transacciones!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Agrega tu primera transacción para ver estadísticas y gráficos",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = onNavigateToAddTransaction,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentVibrantStart
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Agregar Transacción")
                            }
                        }
                    }
                }
            }
        }

        // Spacer inferior
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DashboardHeader(
    userName: String,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (currentHour) {
        in 5..11 -> "Buenos días"
        in 12..17 -> "Buenas tardes"
        else -> "Buenas noches"
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = GlassWhiteSubtle,
        cornerRadius = 24.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greeting,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = userName,
                    fontSize = 24.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES")).format(Date()),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                                GlassWhiteSubtle,
                                CircleShape
                            )
                ) {
                    Icon(
                        imageVector = LucideIconMapper.Navigation.notifications,
                        contentDescription = "Notificaciones",
            tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AccentVibrantStart, AccentVibrantEnd)
                            ),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = LucideIconMapper.Navigation.profile,
                        contentDescription = "Perfil",
                        tint = TextOnAccent
                    )
                }
            }
        }
    }
}


@Composable
private fun QuickActionsSection(
    onAddSavingsGoal: () -> Unit,
    onViewTransactions: () -> Unit,
    onViewReports: () -> Unit,
    onViewBudget: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        // Use the subtle glass tint to match other cards in dark mode
        backgroundColor = GlassWhiteSubtle,
        cornerRadius = 20.dp
    ) {
        Column {
            Text(
                text = "Acciones Rápidas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                            QuickActionButton(
                                icon = LucideIconMapper.Navigation.add,
                                title = "Agregar",
                                subtitle = "Meta",
                                onClick = onAddSavingsGoal,
                                gradient = listOf(AccentVibrantStart, AccentVibrantEnd)
                            )

                            QuickActionButton(
                                icon = LucideIconMapper.Navigation.transactions,
                                title = "Ver",
                                subtitle = "Historial",
                                onClick = onViewTransactions,
                                gradient = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                            )

                            QuickActionButton(
                                icon = LucideIconMapper.Navigation.reports,
                                title = "Reportes",
                                subtitle = "Análisis",
                                onClick = onViewReports,
                                gradient = listOf(Color(0xFFFF6B6B), Color(0xFFFFE66D))
                            )

                            QuickActionButton(
                                icon = LucideIconMapper.getNavigationIcon("DollarSign"),
                                title = "Presupuesto",
                                subtitle = "Metas",
                                onClick = onViewBudget,
                                gradient = listOf(Color(0xFF4ECDC4), Color(0xFF44A08D))
                            )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    gradient: List<Color>
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
            Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    Brush.linearGradient(gradient),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ExpensesByCategoryChart(categories: List<CategoryExpense>, onNavigateToReportsSection: (String) -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = GlassWhiteSubtle,
        cornerRadius = 20.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gastos por Categoría",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                TextButton(onClick = { onNavigateToReportsSection("expenseByCategory") }) {
                    Text(
                        text = "Ver todo",
                        color = AccentVibrantStart,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (categories.isNotEmpty()) {
                DonutChart(
                    categories = categories.map { CategoryData(it.name, it.percentage, it.color) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(vertical = 10.dp),
                    totalAmount = categories.sumOf { it.amount }
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                        items(categories) { category ->
                        CategoryLegendItem(
                            category = CategoryData(category.name, category.percentage, category.color),
                            amount = category.amount
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Sin datos",
                        tint = TextSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Sin gastos este mes",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    categories: List<CategoryData>,
    modifier: Modifier = Modifier,
    totalAmount: Double = 0.0
) {
    val context = LocalContext.current

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val size = 200.dp
        Canvas(
            modifier = Modifier
                .size(size)
                .aspectRatio(1f)
        ) {
            val total = categories.sumOf { it.percentage.toDouble() }
            var currentAngle = -90f
            val canvasSize = this.size.minDimension
            val strokeWidth = (canvasSize * 0.2f)
            val radius = (canvasSize - strokeWidth) / 2f
            val center = androidx.compose.ui.geometry.Offset(
                x = this.size.width / 2f,
                y = this.size.height / 2f
            )

            categories.forEach { category ->
                val sweepAngle = (category.percentage / total * 360).toFloat()

                drawArc(
                    color = category.color,
                    startAngle = currentAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth),
                    topLeft = androidx.compose.ui.geometry.Offset(
                        x = center.x - radius - strokeWidth / 2f,
                        y = center.y - radius - strokeWidth / 2f
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        width = (radius + strokeWidth / 2f) * 2f,
                        height = (radius + strokeWidth / 2f) * 2f
                    )
                )

                currentAngle += sweepAngle
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "Total",
                fontSize = 14.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (totalAmount > 0) com.example.admin_ingresos.data.CurrencyUtils.format(totalAmount, context) else com.example.admin_ingresos.data.CurrencyUtils.format(0.0, context),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CategoryLegendItem(category: CategoryData, amount: Double = 0.0) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(category.color, CircleShape)
        )

        Column {
            Text(
                text = category.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = if (amount > 0) com.example.admin_ingresos.data.CurrencyUtils.format(amount, context) else "${category.percentage.toInt()}%",
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun TrendsAndInsights(
    monthlyIncome: Double,
    monthlyExpenses: Double,
    incomeChangePercent: Double = 0.0,
    expenseChangePercent: Double = 0.0,
    categoryExpenses: List<CategoryExpense> = emptyList()
) {
    val context = LocalContext.current
    // compute simple savings rate and delta using provided change percents
    val savingsRate = if (monthlyIncome > 0) ((monthlyIncome - monthlyExpenses) / monthlyIncome) * 100 else 0.0
    val prevIncome = if (incomeChangePercent != 0.0) monthlyIncome / (1 + (incomeChangePercent / 100.0)) else monthlyIncome
    val prevExpenses = if (expenseChangePercent != 0.0) monthlyExpenses / (1 + (expenseChangePercent / 100.0)) else monthlyExpenses
    val prevSavingsRate = if (prevIncome > 0) ((prevIncome - prevExpenses) / prevIncome) * 100 else 0.0
    val savingsDelta = savingsRate - prevSavingsRate

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = GlassWhiteSubtle,
        cornerRadius = 20.dp
    ) {
        Column {
            Text(
                text = "Tendencias e Insights",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Top small cards using InsightCard structure (header icon+title, big value, subtitle)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InsightCard(
                    title = "Ingresos",
                    value = if (incomeChangePercent >= 0) "+${incomeChangePercent.toInt()}%" else "${incomeChangePercent.toInt()}%",
                    subtitle = "vs. mes pasado",
                    icon = com.example.admin_ingresos.ui.icons.LucideIconMapper.getTransactionTypeIcon("ingreso"),
                    color = IncomeGreen,
                    valueColor = if (incomeChangePercent >= 0) IncomeGreen else ExpenseRed,
                    modifier = Modifier.weight(1f)
                )

                InsightCard(
                    title = "Gastos",
                    value = if (expenseChangePercent >= 0) "+${expenseChangePercent.toInt()}%" else "${expenseChangePercent.toInt()}%",
                    subtitle = "vs. mes pasado",
                    icon = com.example.admin_ingresos.ui.icons.LucideIconMapper.getTransactionTypeIcon("gasto"),
                    color = ExpenseRed,
                    valueColor = if (expenseChangePercent > 0) ExpenseRed else IncomeGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Large savings card using InsightCard structure
            InsightCard(
                title = "Ahorro actual",
                value = if (savingsDelta >= 0) "+${savingsDelta.toInt()}%" else "${savingsDelta.toInt()}%",
                subtitle = "vs. mes pasado",
                icon = com.example.admin_ingresos.ui.icons.LucideIconMapper.getIconFromCategoryName("ahorros"),
                color = Color(0xFF42A5F5),
                valueColor = if (savingsDelta >= 0) IncomeGreen else ExpenseRed,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// MainBalanceCards and InsightCard have been moved to ui.components.MainBalance.kt

@Composable
private fun RecentTransactionsSection(
    transactions: List<DashboardTransaction>,
    onViewAll: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = GlassWhiteSubtle,
        cornerRadius = 20.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transacciones Recientes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                TextButton(onClick = onViewAll) {
                    Text(
                        text = "Ver todas",
                        color = AccentVibrantStart,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            transactions.take(5).forEach { transaction ->
                DashboardTransactionItemCard(transaction = transaction)
                if (transaction != transactions.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DashboardTransactionItemCard(transaction: DashboardTransaction) {
    val context = LocalContext.current

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
                    .size(40.dp)
                    .background(
                        transaction.categoryColor.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = transaction.icon,
                    contentDescription = transaction.category,
                    tint = transaction.categoryColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = transaction.description,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${transaction.category} • ${transaction.date}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        val amountText = when {
            transaction.isIncome -> "+${com.example.admin_ingresos.data.CurrencyUtils.format(transaction.amount, context).replace(" ", "\u00A0")}"
            transaction.isTransfer -> com.example.admin_ingresos.data.CurrencyUtils.format(transaction.amount, context).replace(" ", "\u00A0")
            else -> "-${com.example.admin_ingresos.data.CurrencyUtils.format(transaction.amount, context).replace(" ", "\u00A0")}"
        }

        Text(
            text = amountText,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = when {
                transaction.isIncome -> IncomeGreen
                transaction.isTransfer -> Color(0xFF42A5F5)
                else -> ExpenseRed
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false
        )
    }
}

@Composable
private fun TransactionItemCard(transaction: TransactionItem) {
    val context = LocalContext.current

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
                    .size(40.dp)
                    .background(
                        transaction.categoryColor.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = transaction.icon,
                    contentDescription = transaction.category,
                    tint = transaction.categoryColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = transaction.description,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${transaction.category} • ${transaction.date}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Text(
            text = "${if (transaction.isIncome) "+" else "-"}${com.example.admin_ingresos.data.CurrencyUtils.format(transaction.amount, context)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (transaction.isIncome) IncomeGreen else ExpenseRed
        )
    }
}

@Composable
private fun SavingsGoalsSection(savingsGoalViewModel: com.example.admin_ingresos.viewmodel.SavingsGoalViewModel) {
    val savingsGoals by savingsGoalViewModel.savingsGoals.collectAsState()

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = GlassWhiteSubtle,
        cornerRadius = 20.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Metas de Ahorro",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                var showAddGoal by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(AccentVibrantStart, AccentVibrantEnd)))
                        .clickable { showAddGoal = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LucideIconMapper.Navigation.add,
                        contentDescription = "Agregar meta",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (showAddGoal) {
                    com.example.admin_ingresos.ui.savings.AddEditSavingsGoalDialog(
                        onConfirm = { name, amount, icon, color ->
                            savingsGoalViewModel.addSavingsGoal(
                                name = name,
                                targetAmount = amount,
                                emoji = icon,
                                color = color,
                                description = ""
                            )
                            showAddGoal = false
                        },
                        onDismiss = { showAddGoal = false }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (savingsGoals.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = LucideIconMapper.getIconFromEmoji("💰"),
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No tienes metas de ahorro",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )

                    Text(
                        text = "Crea tu primera meta para empezar a ahorrar",
                        fontSize = 14.sp,
                        color = TextSecondary.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                var editGoal by remember { mutableStateOf<com.example.admin_ingresos.data.model.SavingsGoal?>(null) }
                var showEditDialog by remember { mutableStateOf(false) }
                savingsGoals.forEach { goal ->
                    SavingsGoalCard(
                        goal = goal,
                        onEdit = {
                            editGoal = goal
                            showEditDialog = true
                        },
                        onDelete = {
                            savingsGoalViewModel.deleteSavingsGoal(goal)
                        },
                        onAddMoney = { amount ->
                            savingsGoalViewModel.addProgress(goal.id, amount)
                        }
                    )
                    if (goal != savingsGoals.last()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                if (showEditDialog && editGoal != null) {
                    com.example.admin_ingresos.ui.savings.AddEditSavingsGoalDialog(
                        initialName = editGoal!!.name,
                        initialAmount = editGoal!!.targetAmount.toString(),
                        initialColor = editGoal!!.color,
                        initialIcon = editGoal!!.emoji,
                        onConfirm = { name, amount, icon, color ->
                            savingsGoalViewModel.updateSavingsGoal(
                                editGoal!!.copy(
                                    name = name,
                                    targetAmount = amount,
                                    emoji = icon,
                                    color = color
                                )
                            )
                            showEditDialog = false
                        },
                        onDismiss = { showEditDialog = false }
                    )
                }
            }
        }
    }
}

@Composable
fun SavingsGoalCard(
    goal: com.example.admin_ingresos.data.model.SavingsGoal,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onAddMoney: ((Double) -> Unit)? = null
) {
    val progress = goal.progressPercentage
    val context = LocalContext.current
    val goalColor = Color(android.graphics.Color.parseColor(goal.color))
    var showAddMoneyDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = GlassWhiteSubtle,
        cornerRadius = 16.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(goalColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIconMapper.getSavingsGoalIcon(goal.emoji),
                            contentDescription = goal.name,
                            tint = goalColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = goal.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                var menuExpanded by remember { mutableStateOf(false) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showAddMoneyDialog = true }) {
                        Icon(
                            imageVector = LucideIconMapper.getNavigationIcon("DollarSign"),
                            contentDescription = "Agregar dinero",
                            tint = AccentVibrantStart,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                LucideIconMapper.Navigation.more,
                                contentDescription = "Más opciones",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(com.example.admin_ingresos.ui.components.resolvedMenuContainerColor())
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar", color = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color.White else Color.Black) },
                                leadingIcon = { Icon(LucideIconMapper.Navigation.edit, null, tint = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color.White else Color.Black) },
                                onClick = {
                                    menuExpanded = false
                                    onEdit?.invoke()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(LucideIconMapper.Navigation.delete, null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuExpanded = false
                                    showDeleteConfirmDialog = true
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gradient progress bar (red -> green) for savings card
            com.example.admin_ingresos.ui.theme.GradientProgressBar(
                progress = (progress).toFloat(),
                startColor = com.example.admin_ingresos.ui.theme.ExpenseRed,
                endColor = com.example.admin_ingresos.ui.theme.IncomeGreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = com.example.admin_ingresos.data.CurrencyUtils.format(goal.currentAmount, context),
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                val pctColor = androidx.compose.ui.graphics.lerp(com.example.admin_ingresos.ui.theme.ExpenseRed, com.example.admin_ingresos.ui.theme.IncomeGreen, (progress).toFloat().coerceIn(0f,1f))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (goal.isCompleted) com.example.admin_ingresos.ui.theme.IncomeGreen else pctColor
                )
                Text(
                    text = com.example.admin_ingresos.data.CurrencyUtils.format(goal.targetAmount, context),
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    if (showAddMoneyDialog) {
        AddMoneyDialog(
            onConfirm = { amount ->
                onAddMoney?.invoke(amount)
                showAddMoneyDialog = false
            },
            onDismiss = { showAddMoneyDialog = false }
        )
    }

    if (showDeleteConfirmDialog) {
        DeleteConfirmationDialog(
            goalName = goal.name,
            onConfirm = {
                onDelete?.invoke()
                showDeleteConfirmDialog = false
            },
            onDismiss = {
                showDeleteConfirmDialog = false
            }
        )
    }
}

@Composable
private fun AddMoneyDialog(
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    ThemedAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar dinero a la meta") },
        text = {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Monto") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        onConfirm(amount)
                    }
                }
            ) { Text("Agregar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

}

@Composable
private fun DeleteConfirmationDialog(
    goalName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Eliminación") },
        text = {
            Text("¿Estás seguro de que quieres eliminar la meta \"$goalName\"? Esta acción no se puede deshacer.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ExpenseRed,
                    contentColor = Color.White
                )
            ) { 
                Text("Eliminar") 
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}



@Composable
private fun WeeklyBarChart(
    day: DayData,
    maxValue: Double
) {
    val incomeHeight = if (maxValue > 0) ((day.income / maxValue) * 80).dp else 0.dp
    val expenseHeight = if (maxValue > 0) ((day.expense / maxValue) * 80).dp else 0.dp
    val transferHeight = if (maxValue > 0) ((day.transfers / maxValue) * 80).dp else 0.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(incomeHeight)
                    .background(
                        IncomeGreen,
                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(expenseHeight)
                    .background(
                        ExpenseRed,
                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                    )
            )
            // Transfers / Ahorro bar
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(transferHeight)
                    .background(
                        Color(0xFF42A5F5),
                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = day.day,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

data class TransactionItem(
    val id: String,
    val description: String,
    val amount: Double,
    val category: String,
    val categoryColor: Color,
    val icon: ImageVector,
    val date: String,
    val isIncome: Boolean
)

data class CategoryData(
    val name: String,
    val percentage: Float,
    val color: Color
)

// DayData moved to shared Models.kt
