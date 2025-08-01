package com.example.admin_ingresos.ui.budget

import android.content.Context
import androidx.work.*
import com.example.admin_ingresos.AppDatabaseProvider
import com.example.admin_ingresos.data.NotificationService
import com.example.admin_ingresos.data.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class BudgetAlertService(
    private val context: Context,
    private val database: com.example.admin_ingresos.data.AppDatabase,
    private val notificationService: NotificationService,
    private val preferencesManager: PreferencesManager
) {
    
    companion object {
        const val BUDGET_CHECK_WORK_NAME = "budget_check_work"
        const val WARNING_THRESHOLD = 0.8f // 80%
        const val CRITICAL_THRESHOLD = 0.95f // 95%
    }
    
    suspend fun checkBudgetAlerts() {
        if (!preferencesManager.budgetAlertsEnabled) return
        
        withContext(Dispatchers.IO) {
            val currentTime = System.currentTimeMillis()
            val budgetProgressList = database.budgetDao().getCurrentBudgetProgress(currentTime)
            val categories = database.categoryDao().getAll()
            
            budgetProgressList.forEach { budgetProgress ->
                val category = categories.find { it.id == budgetProgress.categoryId }
                val categoryName = category?.name ?: "Categoría desconocida"
                
                val percentage = if (budgetProgress.amount > 0) {
                    (budgetProgress.spent / budgetProgress.amount).toFloat()
                } else {
                    0f
                }
                
                when {
                    // Budget exceeded
                    percentage > 1.0f -> {
                        val overspent = budgetProgress.spent - budgetProgress.amount
                        notificationService.showBudgetExceededNotification(
                            categoryName = categoryName,
                            spent = budgetProgress.spent,
                            limit = budgetProgress.amount,
                            overspent = overspent
                        )
                    }
                    
                    // Critical threshold (95%)
                    percentage >= CRITICAL_THRESHOLD -> {
                        notificationService.showBudgetCriticalNotification(
                            categoryName = categoryName,
                            spent = budgetProgress.spent,
                            limit = budgetProgress.amount,
                            percentage = percentage * 100
                        )
                    }
                    
                    // Warning threshold (80%)
                    percentage >= WARNING_THRESHOLD -> {
                        notificationService.showBudgetWarningNotification(
                            categoryName = categoryName,
                            spent = budgetProgress.spent,
                            limit = budgetProgress.amount,
                            percentage = percentage * 100
                        )
                    }
                }
            }
        }
    }
    
    fun scheduleBudgetChecks() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val budgetCheckRequest = PeriodicWorkRequestBuilder<BudgetCheckWorker>(
            repeatInterval = 6, // Check every 6 hours
            repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = 1, // Allow 1 hour flexibility
            flexTimeIntervalUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag("budget_alerts")
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                BUDGET_CHECK_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                budgetCheckRequest
            )
    }
    
    fun cancelBudgetChecks() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(BUDGET_CHECK_WORK_NAME)
    }
    
    suspend fun checkSpecificBudget(categoryId: Int) {
        if (!preferencesManager.budgetAlertsEnabled) return
        
        withContext(Dispatchers.IO) {
            val currentTime = System.currentTimeMillis()
            val budgetProgress = database.budgetDao().getBudgetProgressForCategory(categoryId, currentTime)
            val category = database.categoryDao().getById(categoryId)
            
            budgetProgress?.let { progress ->
                val categoryName = category?.name ?: "Categoría desconocida"
                val percentage = if (progress.amount > 0) {
                    (progress.spent / progress.amount).toFloat()
                } else {
                    0f
                }
                
                when {
                    percentage > 1.0f -> {
                        val overspent = progress.spent - progress.amount
                        notificationService.showBudgetExceededNotification(
                            categoryName = categoryName,
                            spent = progress.spent,
                            limit = progress.amount,
                            overspent = overspent
                        )
                    }
                    
                    percentage >= CRITICAL_THRESHOLD -> {
                        notificationService.showBudgetCriticalNotification(
                            categoryName = categoryName,
                            spent = progress.spent,
                            limit = progress.amount,
                            percentage = percentage * 100
                        )
                    }
                    
                    percentage >= WARNING_THRESHOLD -> {
                        notificationService.showBudgetWarningNotification(
                            categoryName = categoryName,
                            spent = progress.spent,
                            limit = progress.amount,
                            percentage = percentage * 100
                        )
                    }
                }
            }
        }
    }
}

class BudgetCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabaseProvider.getDatabase(applicationContext)
            val notificationService = NotificationService(applicationContext)
            val preferencesManager = PreferencesManager(applicationContext)
            
            val budgetAlertService = BudgetAlertService(
                context = applicationContext,
                database = database,
                notificationService = notificationService,
                preferencesManager = preferencesManager
            )
            
            budgetAlertService.checkBudgetAlerts()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}