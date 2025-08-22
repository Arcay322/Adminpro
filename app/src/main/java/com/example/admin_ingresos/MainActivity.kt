package com.example.admin_ingresos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.admin_ingresos.ui.animations.getEnterTransition
import com.example.admin_ingresos.ui.animations.getExitTransition
import com.example.admin_ingresos.ui.animations.getPopEnterTransition
import com.example.admin_ingresos.ui.animations.getPopExitTransition
import com.example.admin_ingresos.ui.animations.getTransitionForRoute
import com.example.admin_ingresos.ui.navigation.BottomNavigationBar
import com.example.admin_ingresos.ui.onboarding.OnboardingScreen
import com.example.admin_ingresos.data.PreferencesManager
import com.example.admin_ingresos.ui.theme.Admin_ingresosTheme
import com.example.admin_ingresos.ui.dashboard.DashboardScreen
import com.example.admin_ingresos.ui.budget.BudgetScreen
import com.example.admin_ingresos.ui.history.TransactionHistoryScreen
import com.example.admin_ingresos.ui.components.AddTransactionModal
import com.example.admin_ingresos.ui.category.CategoryDetailScreen
import com.example.admin_ingresos.ui.category.CategoryDetailViewModel
import com.example.admin_ingresos.ui.category.CategoryScreen
import com.example.admin_ingresos.ui.category.CategoryViewModel
import com.example.admin_ingresos.ui.reports.ReportsScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    enableEdgeToEdge()
        // Seed AppThemeManager with persisted preference so theme immediately reflects saved color
        try {
            val prefs = PreferencesManager(this)
            // if a custom background stored in profile prefs, it will also be in ProfileViewModel prefs; fallback uses PreferencesManager themeMode ignored
            // Attempt to read profile prefs file directly
            val profilePrefs = getSharedPreferences("profile_prefs", MODE_PRIVATE)
            if (profilePrefs.contains("background_color")) {
                // Accept any stored int (including 0xFFFFFFFF for white which is -1 as signed int)
                val saved = profilePrefs.getInt("background_color", Int.MIN_VALUE)
                com.example.admin_ingresos.ui.theme.AppThemeManager.setBackgroundColor(saved)
            }
            if (profilePrefs.contains("force_light_mode")) {
                val force = profilePrefs.getBoolean("force_light_mode", false)
                com.example.admin_ingresos.ui.theme.AppThemeManager.setForceLight(force)
            } else if (profilePrefs.contains("dark_mode")) {
                // Older installs might only have dark_mode saved; derive forceLight as the inverse
                val dark = profilePrefs.getBoolean("dark_mode", false)
                com.example.admin_ingresos.ui.theme.AppThemeManager.setForceLight(!dark)
            }
        } catch (_: Exception) {}

        setContent {
            // Observe the global AppThemeManager so changes apply immediately
            val bgInt by com.example.admin_ingresos.ui.theme.AppThemeManager.backgroundColor.collectAsState()
            val forceLight by com.example.admin_ingresos.ui.theme.AppThemeManager.forceLight.collectAsState()
            val bgColor = androidx.compose.ui.graphics.Color(bgInt)
            val isDarkTheme = !forceLight

            // Apply theme without remounting the navigation graph so the
            // user remains on the current screen when toggling theme/colors.
            Admin_ingresosTheme(isDarkTheme = isDarkTheme, backgroundColor = bgColor) {
                MainAppNavigation()
            }
        }
    }
}

@Composable
fun MainAppNavigation() {
    val navController = rememberNavController()
    var showAddTransactionModal by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    // Show onboarding only when it hasn't been completed (read persisted preference)
    var showOnboarding by remember { mutableStateOf(!prefs.isOnboardingCompleted) }
    val database = remember { AppDatabaseProvider.getDatabase(context) }
    val categoryViewModel: CategoryViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return CategoryViewModel(database) as T
            }
        }
    )
    val uiState by categoryViewModel.uiState.collectAsState()
    val categories = uiState.categories

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // If onboarding hasn't been completed, show onboarding flow first
        if (showOnboarding) {
            OnboardingScreen(onFinish = {
                prefs.isOnboardingCompleted = true
                showOnboarding = false
            }, onSkip = {
                prefs.isOnboardingCompleted = true
                showOnboarding = false
            })
            } else {
            // Determine whether to show FAB based on current route
            val currentBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = currentBackStackEntry?.destination?.route ?: ""

            val showFab = !currentRoute.startsWith("transaction_detail")

            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = { BottomNavigationBar(navController) },
                floatingActionButton = {
                    if (showFab) {
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
                }
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = "dashboard",
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable("dashboard") {
                        DashboardScreen(
                            onNavigateToTransactions = { navController.navigate("history") },
                            onNavigateToAddTransaction = { showAddTransactionModal = true },
                            onNavigateToReports = { navController.navigate("reports") },
                            onNavigateToSettings = { navController.navigate("profile") }
                        )
                    }
                    composable("budget") {
                        BudgetScreen()
                    }
                    composable("categories") {
                        CategoryScreen(
                            viewModel = categoryViewModel,
                            onNavigateToCategoryDetail = { categoryId ->
                                navController.navigate("category_detail/$categoryId")
                            }
                            , onNavigateToSavingsDetail = { goalId ->
                                navController.navigate("savings_detail/$goalId")
                            }
                        )
                    }
                    composable("history") {
                        com.example.admin_ingresos.ui.history.TransactionHistoryScreen(navController)
                    }
                    composable("reports") {
                        ReportsScreen()
                    }

                    composable("profile") {
                        com.example.admin_ingresos.ui.profile.ProfileScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onSignOut = {
                                navController.navigate("dashboard") {
                                    popUpTo("dashboard") {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }

                    composable(
                        route = "category_detail/{categoryId}",
                        arguments = listOf(navArgument("categoryId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val categoryId = backStackEntry.arguments?.getInt("categoryId")
                        val categoryDetailViewModel: CategoryDetailViewModel = viewModel(
                            factory = object : ViewModelProvider.Factory {
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    @Suppress("UNCHECKED_CAST")
                                    return CategoryDetailViewModel(
                                        transactionDao = database.transactionDao(),
                                        categoryDao = database.categoryDao(),
                                        budgetDao = database.budgetDao(),
                                        categoryId = categoryId
                                    ) as T
                                }
                            }
                        )
                        CategoryDetailScreen(
                            viewModel = categoryDetailViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "savings_detail/{goalId}",
                        arguments = listOf(navArgument("goalId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val goalId = backStackEntry.arguments?.getLong("goalId") ?: 0L
                        val savingsGoalViewModel: com.example.admin_ingresos.viewmodel.SavingsGoalViewModel = viewModel(
                            factory = object : ViewModelProvider.Factory {
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    @Suppress("UNCHECKED_CAST")
                                    return com.example.admin_ingresos.viewmodel.SavingsGoalViewModel(database) as T
                                }
                            }
                        )
                        com.example.admin_ingresos.ui.savings.SavingsGoalDetailScreen(
                            savingsGoalViewModel = savingsGoalViewModel,
                            savingsGoalId = goalId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                        composable(
                            route = "transaction_detail/{transactionId}",
                            arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val txId = backStackEntry.arguments?.getInt("transactionId") ?: 0
                            com.example.admin_ingresos.ui.transaction.TransactionDetailScreen(
                                transactionId = txId,
                                onNavigateBack = { navController.popBackStack() },
                                onOpenTransaction = { id -> navController.navigate("transaction_detail/$id") }
                            )
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
}