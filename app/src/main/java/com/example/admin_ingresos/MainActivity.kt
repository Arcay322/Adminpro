package com.example.admin_ingresos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.admin_ingresos.ui.theme.Admin_ingresosTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Inicializar la base de datos Room
        val db = AppDatabaseProvider.getDatabase(applicationContext)
        setContent {
            Admin_ingresosTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "dashboard"
                ) {
                    composable("dashboard") {
                        com.example.admin_ingresos.ui.dashboard.DashboardScreen(
                            onAddTransaction = { navController.navigate("addTransaction") },
                            onViewHistory = { navController.navigate("history") },
                            onViewReports = { navController.navigate("reports") }
                        )
                    }
                    composable("reports") {
                        com.example.admin_ingresos.ui.reports.ReportsScreen()
                    }
                    composable("addTransaction") {
                        com.example.admin_ingresos.ui.transaction.AddTransactionScreen(
                            onSave = { navController.popBackStack() },
                            onCancel = { navController.popBackStack() }
                        )
                    }
                    composable("history") {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val db = com.example.admin_ingresos.AppDatabaseProvider.getDatabase(context)
                        val viewModel: com.example.admin_ingresos.ui.history.TransactionHistoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return com.example.admin_ingresos.ui.history.TransactionHistoryViewModel(db) as T
                            }
                        })
                        val transactions by viewModel.transactions.collectAsState()
                        com.example.admin_ingresos.ui.history.TransactionHistoryScreen(transactions)
                        LaunchedEffect(Unit) { viewModel.loadTransactions() }
                    }
                }
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