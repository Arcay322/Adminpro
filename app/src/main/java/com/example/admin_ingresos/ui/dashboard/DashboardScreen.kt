package com.example.admin_ingresos.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin_ingresos.ui.components.*
import com.example.admin_ingresos.ui.theme.*
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

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
    
    // ViewModel con factory
    val dashboardViewModel: DashboardViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository, db) as T
        }
    })
    
    // Estados del dashboard desde el ViewModel
    val uiState by dashboardViewModel.uiState.collectAsState()
    var userName by remember { mutableStateOf("Usuario") }
    
    // Efectos de animación
    var isVisible by remember { mutableStateOf(false) }
    val animatedBalance by animateFloatAsState(
        targetValue = if (isVisible) uiState.currentBalance.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "balance_animation"
    )
    
    // Activar animaciones cuando se carguen los datos
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isVisible = true
        }
    }
    
    GlassmorphismScreen {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con saludo y notificaciones
            item {
                DashboardHeader(
                    userName = userName,
                    onNotificationClick = { /* TODO: Implementar notificaciones */ },
                    onProfileClick = { onNavigateToSettings() }
                )
            }
            
            // Mostrar loading o error
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AccentVibrantStart,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                }
            } else if (uiState.error != null) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0x20FF6B6B),
                        borderColor = Color(0x60FF6B6B)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = Color(0xFFFF6B6B),
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
                            monthlyExpenses = uiState.monthlyExpenses
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
                } else if (!uiState.isLoading) {
                    // Mostrar mensaje cuando no hay transacciones
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = GlassWhiteSubtle
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                
                // Metas de ahorro (simuladas por ahora)
                item {
                    AnimatedVisibility(
                        visible = !uiState.isLoading,
                        enter = slideInVertically(initialOffsetY = { it / 7 }) + fadeIn()
                    ) {
                        SavingsGoalsSection()
                    }
                }
                
                // Flujo de efectivo semanal (simulado por ahora)
                item {
                    AnimatedVisibility(
                        visible = !uiState.isLoading,
                        enter = slideInVertically(initialOffsetY = { it / 8 }) + fadeIn()
                    ) {
                        WeeklyCashFlowChart()
                    }
                }
            }
            
            // Spacer inferior
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
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
                        imageVector = Icons.Default.Notifications,
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
                        imageVector = Icons.Default.Person,
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
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    val balanceChange = monthlyIncome - monthlyExpenses
    val balanceChangePercent = if (monthlyExpenses > 0) (balanceChange / monthlyExpenses) * 100 else 0.0
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Balance principal
        AdvancedBalanceCard(
            title = "Balance Total",
            amount = formatter.format(currentBalance),
            subtitle = "Actualizado ahora",
            onClick = onViewDetails,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Tarjetas de ingresos y gastos - diseño mejorado y simétrico
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
                    icon = Icons.Default.Add,
                    title = "Agregar",
                    subtitle = "Transacción",
                    onClick = onAddTransaction,
                    gradient = listOf(AccentVibrantStart, AccentVibrantEnd)
                )
                
                QuickActionButton(
                    icon = Icons.Default.List,
                    title = "Ver",
                    subtitle = "Historial",
                    onClick = onViewTransactions,
                    gradient = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                )
                
                QuickActionButton(
                    icon = Icons.Default.BarChart,
                    title = "Reportes",
                    subtitle = "Análisis",
                    onClick = onViewReports,
                    gradient = listOf(Color(0xFFFF6B6B), Color(0xFFFFE66D))
                )
                
                QuickActionButton(
                    icon = Icons.Default.AccountBalance,
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
                // Gráfico circular con datos reales
                DonutChart(
                    categories = categories.map { CategoryData(it.name, it.percentage, it.color) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(vertical = 10.dp),
                    totalAmount = categories.sumOf { it.amount }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Leyenda con datos reales
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
                // Mensaje cuando no hay datos
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
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Asegurar que el Canvas sea cuadrado para mantener la forma circular
        val size = 200.dp
        Canvas(
            modifier = Modifier
                .size(size)
                .aspectRatio(1f) // Forzar proporción 1:1 para un círculo perfecto
        ) {
            val total = categories.sumOf { it.percentage.toDouble() }
            var currentAngle = -90f
            val canvasSize = this.size.minDimension
            val strokeWidth = (canvasSize * 0.2f) // 20% del diámetro para el grosor del anillo
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
                    style = Stroke(strokeWidth),
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
        
        // Contenido central del donut - mejorado para evitar saltos de línea
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
                text = if (totalAmount > 0) formatter.format(totalAmount) else "$0",
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
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    
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
    monthlyExpenses: Double
) {
    val savingsRate = if (monthlyIncome > 0) ((monthlyIncome - monthlyExpenses) / monthlyIncome) * 100 else 0.0
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    
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
                InsightCard(
                    title = "Tasa de Ahorro",
                    value = "${savingsRate.toInt()}%",
                    subtitle = "Este mes",
                    icon = Icons.Default.Savings,
                    color = if (savingsRate >= 20) IncomeGreen else Color(0xFFFBBF24),
                    modifier = Modifier.weight(1f)
                )
                
                InsightCard(
                    title = "Promedio Diario",
                    value = formatter.format(monthlyExpenses / 30),
                    subtitle = "Gastos",
                    icon = Icons.Default.TrendingDown,
                    color = ExpenseRed,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Barra de progreso de ahorro
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Meta de ahorro: 25%",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "${savingsRate.toInt()}%",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
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
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    
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
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    
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
private fun SavingsGoalsSection() {
    val goals = remember {
        listOf(
            SavingsGoal("Vacaciones", 2500000.0, 1800000.0, "🏖️"),
            SavingsGoal("Emergencia", 5000000.0, 3200000.0, "🚨"),
            SavingsGoal("Nuevo Auto", 15000000.0, 4500000.0, "🚗")
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
                    text = "Metas de Ahorro",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                TextButton(onClick = { /* TODO: Agregar meta */ }) {
                    Text(
                        text = "Agregar",
                        color = AccentVibrantStart,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            goals.forEach { goal ->
                SavingsGoalCard(goal = goal)
                if (goal != goals.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun SavingsGoalCard(goal: SavingsGoal) {
    val progress = (goal.currentAmount / goal.targetAmount).toFloat()
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = GlassWhiteSubtle,
        cornerRadius = 16.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = goal.emoji,
                        fontSize = 20.sp
                    )
                    Text(
                        text = goal.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
                
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentVibrantStart
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = AccentVibrantStart,
                trackColor = GlassWhiteSubtle
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatter.format(goal.currentAmount),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = formatter.format(goal.targetAmount),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun WeeklyCashFlowChart() {
    val weeklyData = remember {
        listOf(
            DayData("L", 150000.0, 80000.0),
            DayData("M", 200000.0, 120000.0),
            DayData("X", 100000.0, 95000.0),
            DayData("J", 180000.0, 140000.0),
            DayData("V", 220000.0, 180000.0),
            DayData("S", 300000.0, 250000.0),
            DayData("D", 120000.0, 90000.0)
        )
    }
    
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = GlassWhite,
        cornerRadius = 20.dp
    ) {
        Column {
            Text(
                text = "Flujo de Efectivo Semanal",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Gráfico de barras simple
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEach { day ->
                    WeeklyBarChart(
                        day = day,
                        maxValue = weeklyData.maxOf { maxOf(it.income, it.expense) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Leyenda
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(
                    color = IncomeGreen,
                    label = "Ingresos",
                    modifier = Modifier.weight(1f)
                )
                LegendItem(
                    color = ExpenseRed,
                    label = "Gastos",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
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

// Data classes
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

data class SavingsGoal(
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val emoji: String
)

data class DayData(
    val day: String,
    val income: Double,
    val expense: Double
)