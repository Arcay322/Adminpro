package com.example.admin_ingresos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Inicializar la base de datos Room
        val db = AppDatabaseProvider.getDatabase(applicationContext)
        setContent {
            Admin_ingresosTheme {
                MainNavigation()
            }
        }
    }
}

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val preferencesManager = remember { com.example.admin_ingresos.data.PreferencesManager(context) }
    
    // Check if initial setup is completed
    var isSetupCompleted by remember { mutableStateOf(preferencesManager.isSetupCompleted()) }
    
    if (!isSetupCompleted) {
        // Show initial setup wizard
        com.example.admin_ingresos.ui.setup.InitialSetupWizard(
            onSetupComplete = {
                preferencesManager.setSetupCompleted(true)
                isSetupCompleted = true
            }
        )
    } else {
        // Show main app navigation
        MainAppNavigation()
    }
}

@Composable
fun MainAppNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("addTransaction") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir transacción"
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
                com.example.admin_ingresos.ui.dashboard.DashboardScreen(
                    onAddTransaction = { navController.navigate("addTransaction") },
                    onViewHistory = { navController.navigate("history") },
                    onViewReports = { navController.navigate("reports") }
                )
            }
            composable(
                "budget",
                enterTransition = { getEnterTransition(getTransitionForRoute("budget")) },
                exitTransition = { getExitTransition(getTransitionForRoute("budget")) },
                popEnterTransition = { getPopEnterTransition(getTransitionForRoute("budget")) },
                popExitTransition = { getPopExitTransition(getTransitionForRoute("budget")) }
            ) {
                com.example.admin_ingresos.ui.budget.BudgetScreen()
            }
            composable(
                "reports",
                enterTransition = { getEnterTransition(getTransitionForRoute("reports")) },
                exitTransition = { getExitTransition(getTransitionForRoute("reports")) },
                popEnterTransition = { getPopEnterTransition(getTransitionForRoute("reports")) },
                popExitTransition = { getPopExitTransition(getTransitionForRoute("reports")) }
            ) {
                com.example.admin_ingresos.ui.reports.ReportsScreen()
            }
            composable(
                "addTransaction",
                enterTransition = { getEnterTransition(getTransitionForRoute("addTransaction")) },
                exitTransition = { getExitTransition(getTransitionForRoute("addTransaction")) },
                popEnterTransition = { getPopEnterTransition(getTransitionForRoute("addTransaction")) },
                popExitTransition = { getPopExitTransition(getTransitionForRoute("addTransaction")) }
            ) {
                com.example.admin_ingresos.ui.transaction.AddTransactionScreen(
                    onSave = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(
                "history",
                enterTransition = { getEnterTransition(getTransitionForRoute("history")) },
                exitTransition = { getExitTransition(getTransitionForRoute("history")) },
                popEnterTransition = { getPopEnterTransition(getTransitionForRoute("history")) },
                popExitTransition = { getPopExitTransition(getTransitionForRoute("history")) }
            ) {
                com.example.admin_ingresos.ui.history.TransactionHistoryScreen()
            }
            composable(
                "categories",
                enterTransition = { getEnterTransition(getTransitionForRoute("categories")) },
                exitTransition = { getExitTransition(getTransitionForRoute("categories")) },
                popEnterTransition = { getPopEnterTransition(getTransitionForRoute("categories")) },
                popExitTransition = { getPopExitTransition(getTransitionForRoute("categories")) }
            ) {
                com.example.admin_ingresos.ui.category.CategoryManagementScreen()
            }
            composable(
                "settings",
                enterTransition = { getEnterTransition(getTransitionForRoute("settings")) },
                exitTransition = { getExitTransition(getTransitionForRoute("settings")) },
                popEnterTransition = { getPopEnterTransition(getTransitionForRoute("settings")) },
                popExitTransition = { getPopExitTransition(getTransitionForRoute("settings")) }
            ) {
                com.example.admin_ingresos.ui.settings.SettingsScreen(
                    onNavigateToDataManagement = { /* TODO: Implement data management */ }
                )
            }
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