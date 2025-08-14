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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    onNavigateToSettings: () -> Unit
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

    var userName by remember { mutableStateOf("Usuario") }

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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .background(color = Background),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp)
    ) {
        // Encabezado del Dashboard
        item {
            DashboardHeader(
                userName = userName,
                onNotificationClick = { /*TODO*/ },
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
                        onAddTransaction = onNavigateToAddTransaction,
                        onViewTransactions = onNavigateToTransactions,
                        onViewReports = onNavigateToReports,
                        onViewBudget = { /* TODO: Implementar presupuesto */ }
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
                        ExpensesByCategoryChart(categories = uiState.categoryExpenses)
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
                            expenseChangePercent = uiState.expenseChangePercent
                        )
                }
            }

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

            // Metas de ahorro
            item {
                AnimatedVisibility(
                    visible = !uiState.isLoading,
                    enter = slideInVertically(initialOffsetY = { it / 7 }) + fadeIn()
                ) {
                    SavingsGoalsSection(savingsGoalViewModel)
                }
            }

            // Flujo de efectivo semanal
            item {
                AnimatedVisibility(
                    visible = !uiState.isLoading,
                    enter = slideInVertically(initialOffsetY = { it / 8 }) + fadeIn()
                ) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = GlassWhite,
                        cornerRadius = 20.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Flujo de efectivo semanal",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                TextButton(onClick = { /* TODO: ver detalles semanal */ }) {
                                    Text(
                                        text = "Ver",
                                        color = AccentVibrantStart,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (weeklyData.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Sin datos para esta semana",
                                        color = TextSecondary
                                    )
                                }
                            } else {
                                val maxValue = (weeklyData.maxOfOrNull { maxOf(it.income, it.expense) } ?: 1.0)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    weeklyData.forEach { day ->
                                        WeeklyBarChart(day = day, maxValue = maxValue)
                                    }
                                }
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
        backgroundColor = GlassWhiteStrong,
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
                            GlassWhite,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = LucideIconMapper.Navigation.notifications,
                        contentDescription = "Notificaciones",
                        tint = TextPrimary
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
private fun MainBalanceCards(
    currentBalance: Double,
    monthlyIncome: Double,
    monthlyExpenses: Double,
    onViewDetails: () -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
    val balanceChange = monthlyIncome - monthlyExpenses
    val balanceChangePercent = if (monthlyExpenses > 0) (balanceChange / monthlyExpenses) * 100 else 0.0

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AdvancedBalanceCard(
            title = "Balance Total",
            amount = formatter.format(currentBalance),
            subtitle = "Actualizado ahora",
            onClick = onViewDetails,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdvancedMetricCard(
                title = "Ingresos",
                value = formatter.format(monthlyIncome),
                subtitle = "Este mes",
                icon = Icons.Default.TrendingUp,
                color = IncomeGreen,
                modifier = Modifier.weight(1f)
            )

            AdvancedMetricCard(
                title = "Gastos",
                value = formatter.format(monthlyExpenses),
                subtitle = "Este mes",
                icon = Icons.Default.TrendingDown,
                color = ExpenseRed,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    onAddTransaction: () -> Unit,
    onViewTransactions: () -> Unit,
    onViewReports: () -> Unit,
    onViewBudget: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = GlassWhite,
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
                    subtitle = "Transacción",
                    onClick = onAddTransaction,
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
private fun ExpensesByCategoryChart(categories: List<CategoryExpense>) {
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
                    text = "Gastos por Categoría",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                TextButton(onClick = { /* TODO: Ver detalles */ }) {
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
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))

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
                text = if (totalAmount > 0) formatter.format(totalAmount) else "S/0.00",
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
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))

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
                text = if (amount > 0) formatter.format(amount) else "${category.percentage.toInt()}%",
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
    expenseChangePercent: Double = 0.0
) {
    val savingsRate = if (monthlyIncome > 0) ((monthlyIncome - monthlyExpenses) / monthlyIncome) * 100 else 0.0
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = GlassWhite,
        cornerRadius = 20.dp
    ) {
        Column {
            Text(
                text = "Tendencias e Insights",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    InsightCard(
                        title = "Ingresos",
                        value = formatter.format(monthlyIncome),
                        subtitle = "Este mes",
                        icon = Icons.Default.TrendingUp,
                        color = IncomeGreen,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // percent badge
                    Text(
                        text = "${incomeChangePercent.toInt()}%",
                        fontSize = 12.sp,
                        color = if (incomeChangePercent >= 0) IncomeGreen else ExpenseRed,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 8.dp, top = 8.dp)
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    InsightCard(
                        title = "Gastos",
                        value = formatter.format(monthlyExpenses),
                        subtitle = "Este mes",
                        icon = Icons.Default.TrendingDown,
                        color = ExpenseRed,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // percent badge
                    Text(
                        text = "${expenseChangePercent.toInt()}%",
                        fontSize = 12.sp,
                        color = if (expenseChangePercent >= 0) ExpenseRed else IncomeGreen,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 8.dp, top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Meta de ahorro: 25%",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${savingsRate.toInt()}%",
                                fontSize = 12.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = (savingsRate / 25.0).toFloat().coerceAtMost(1f),
                    modifier = Modifier.fillMaxWidth(),
                    color = if (savingsRate >= 25) IncomeGreen else AccentVibrantStart,
                    trackColor = GlassWhiteSubtle
                )
            }
        }
    }
}

@Composable
private fun InsightCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        backgroundColor = GlassWhiteSubtle,
        cornerRadius = 16.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun RecentTransactionsSection(
    transactions: List<DashboardTransaction>,
    onViewAll: () -> Unit
) {
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
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))

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
            text = "${if (transaction.isIncome) "+" else "-"}${formatter.format(transaction.amount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (transaction.isIncome) IncomeGreen else ExpenseRed
        )
    }
}

@Composable
private fun TransactionItemCard(transaction: TransactionItem) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))

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
            text = "${if (transaction.isIncome) "+" else "-"}${formatter.format(transaction.amount)}",
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
                    text = "Metas de Ahorro",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                var showAddGoal by remember { mutableStateOf(false) }
                IconButton(
                    onClick = { showAddGoal = true }
                ) {
                    Icon(
                        imageVector = LucideIconMapper.Navigation.add,
                        contentDescription = "Agregar meta",
                        tint = AccentVibrantStart,
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
private fun SavingsGoalCard(
    goal: com.example.admin_ingresos.data.model.SavingsGoal,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onAddMoney: ((Double) -> Unit)? = null
) {
    val progress = goal.progressPercentage
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
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
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar") },
                                leadingIcon = { Icon(LucideIconMapper.Navigation.edit, null, tint = AccentVibrantStart) },
                                onClick = {
                                    menuExpanded = false
                                    onEdit?.invoke()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar") },
                                leadingIcon = { Icon(LucideIconMapper.Navigation.delete, null, tint = ExpenseRed) },
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

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (goal.isCompleted) IncomeGreen else goalColor,
                trackColor = GlassWhiteStrong
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatter.format(goal.currentAmount),
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (goal.isCompleted) IncomeGreen else goalColor
                )
                Text(
                    text = formatter.format(goal.targetAmount),
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
    AlertDialog(
        onDismissRequest = onDismiss,
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
        },
        title = { Text("Agregar dinero a la meta") },
        text = {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Monto") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
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
    val incomeHeight = ((day.income / maxValue) * 80).dp
    val expenseHeight = ((day.expense / maxValue) * 80).dp

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