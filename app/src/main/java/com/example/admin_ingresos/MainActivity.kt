package com.example.admin_ingresos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.admin_ingresos.ui.animations.getEnterTransition
import com.example.admin_ingresos.ui.animations.getExitTransition
import com.example.admin_ingresos.ui.animations.getPopEnterTransition
import com.example.admin_ingresos.ui.animations.getPopExitTransition
import com.example.admin_ingresos.ui.animations.getTransitionForRoute
import com.example.admin_ingresos.ui.navigation.BottomNavigationBar
import com.example.admin_ingresos.ui.theme.Admin_ingresosTheme
import com.example.admin_ingresos.ui.dashboard.DashboardScreen
import com.example.admin_ingresos.ui.budget.BudgetScreen
import com.example.admin_ingresos.ui.history.TransactionHistoryScreen
import com.example.admin_ingresos.ui.components.AddTransactionModal

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Admin_ingresosTheme {
                MainAppNavigation()
            }
        }
    }
}

@Composable
fun MainAppNavigation() {
    val navController = rememberNavController()
    var showAddTransactionModal by remember { mutableStateOf(false) }
    // Glassmorphism Background
    val context = LocalContext.current
    val database = remember { AppDatabaseProvider.getDatabase(context) }
    val categoryViewModel: com.example.admin_ingresos.ui.category.CategoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return com.example.admin_ingresos.ui.category.CategoryViewModel(database) as T
            }
        }
    )
    val categories by categoryViewModel.categories.collectAsState()
    val transactionCounts by categoryViewModel.transactionCounts.collectAsState()
    val totalAmounts by categoryViewModel.totalAmounts.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        com.example.admin_ingresos.ui.theme.BackgroundGradientStart,
                        com.example.admin_ingresos.ui.theme.BackgroundGradientEnd
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = { BottomNavigationBar(navController) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddTransactionModal = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar transacción"
                    )
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "dashboard",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(
                    "dashboard",
                    enterTransition = { getEnterTransition(getTransitionForRoute("dashboard")) },
                    exitTransition = { getExitTransition(getTransitionForRoute("dashboard")) },
                    popEnterTransition = { getPopEnterTransition(getTransitionForRoute("dashboard")) },
                    popExitTransition = { getPopExitTransition(getTransitionForRoute("dashboard")) }
                ) {
                    DashboardScreen(
                        onNavigateToTransactions = { navController.navigate("history") },
                        onNavigateToAddTransaction = { showAddTransactionModal = true },
                        onNavigateToReports = { /* No-op or implement if needed */ },
                        onNavigateToSettings = { /* No-op or implement if needed */ }
                    )
                }
                composable(
                    "budget",
                    enterTransition = { getEnterTransition(getTransitionForRoute("budget")) },
                    exitTransition = { getExitTransition(getTransitionForRoute("budget")) },
                    popEnterTransition = { getPopEnterTransition(getTransitionForRoute("budget")) },
                    popExitTransition = { getPopExitTransition(getTransitionForRoute("budget")) }
                ) {
                    BudgetScreen()
                }
                composable(
                    "categories",
                    enterTransition = { getEnterTransition(getTransitionForRoute("categories")) },
                    exitTransition = { getExitTransition(getTransitionForRoute("categories")) },
                    popEnterTransition = { getPopEnterTransition(getTransitionForRoute("categories")) },
                    popExitTransition = { getPopExitTransition(getTransitionForRoute("categories")) }
                ) {
                    val coroutineScope = rememberCoroutineScope()
                    com.example.admin_ingresos.ui.category.CategoryScreen(
                        categories = categories,
                        onAddCategory = { category -> categoryViewModel.addCategory(category) },
                        onEditCategory = { category -> categoryViewModel.updateCategory(category) },
                        onDeleteCategory = { category -> categoryViewModel.deleteCategory(category) },
                        onReorder = categoryViewModel::reorderCategories,
                        getTransactionCount = { categoryId -> transactionCounts[categoryId] ?: 0 },
                        getTotalAmount = { categoryId -> totalAmounts[categoryId] ?: 0.0 }
                    )
                }
                composable(
                    "history",
                    enterTransition = { getEnterTransition(getTransitionForRoute("history")) },
                    exitTransition = { getExitTransition(getTransitionForRoute("history")) },
                    popEnterTransition = { getPopEnterTransition(getTransitionForRoute("history")) },
                    popExitTransition = { getPopExitTransition(getTransitionForRoute("history")) }
                ) {
                    TransactionHistoryScreen()
                }
            }
            AddTransactionModal(
                show = showAddTransactionModal,
                onDismiss = { showAddTransactionModal = false },
                onTransactionAdded = { /* TODO: Recargar datos si es necesario */ }
            )
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Admin_ingresosTheme {
        Greeting("Android")
    }
}
