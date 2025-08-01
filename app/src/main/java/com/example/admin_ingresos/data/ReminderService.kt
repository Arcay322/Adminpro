package com.example.admin_ingresos.data

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class TransactionReminder(
    val id: String,
    val title: String,
    val description: String,
    val amount: Double? = null,
    val categoryId: Int? = null,
    val type: String, // "Ingreso" or "Gasto"
    val scheduledTime: Long,
    val isRecurring: Boolean = false,
    val recurringInterval: RecurringInterval = RecurringInterval.NONE,
    val isActive: Boolean = true
)

data class BillReminder(
    val id: String,
    val billName: String,
    val amount: Double,
    val dueDate: Long,
    val categoryId: Int? = null,
    val isRecurring: Boolean = false,
    val recurringInterval: RecurringInterval = RecurringInterval.MONTHLY,
    val reminderDaysBefore: Int = 3,
    val isActive: Boolean = true
)

enum class RecurringInterval {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

class ReminderService(
    private val context: Context,
    private val notificationService: NotificationService,
    private val preferencesManager: PreferencesManager
) {
    
    companion object {
        const val TRANSACTION_REMINDER_WORK_PREFIX = "transaction_reminder_"
        const val BILL_REMINDER_WORK_PREFIX = "bill_reminder_"
        const val RECURRING_CHECK_WORK_NAME = "recurring_check_work"
    }
    
    fun scheduleTransactionReminder(reminder: TransactionReminder) {
        if (!preferencesManager.notificationsEnabled) return
        
        val delay = reminder.scheduledTime - System.currentTimeMillis()
        if (delay <= 0) return // Don't schedule past reminders
        
        val workRequest = OneTimeWorkRequestBuilder<TransactionReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                "reminder_id" to reminder.id,
                "title" to reminder.title,
                "description" to reminder.description,
                "amount" to (reminder.amount ?: 0.0),
                "has_amount" to (reminder.amount != null),
                "is_recurring" to reminder.isRecurring,
                "recurring_interval" to reminder.recurringInterval.name
            ))
            .addTag("transaction_reminders")
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "$TRANSACTION_REMINDER_WORK_PREFIX${reminder.id}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }
    
    fun scheduleBillReminder(reminder: BillReminder) {
        if (!preferencesManager.notificationsEnabled) return
        
        val reminderTime = reminder.dueDate - (reminder.reminderDaysBefore * 24 * 60 * 60 * 1000L)
        val delay = reminderTime - System.currentTimeMillis()
        if (delay <= 0) return // Don't schedule past reminders
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dueDateString = dateFormat.format(Date(reminder.dueDate))
        
        val workRequest = OneTimeWorkRequestBuilder<BillReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                "reminder_id" to reminder.id,
                "bill_name" to reminder.billName,
                "amount" to reminder.amount,
                "due_date" to dueDateString,
                "is_recurring" to reminder.isRecurring,
                "recurring_interval" to reminder.recurringInterval.name
            ))
            .addTag("bill_reminders")
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "$BILL_REMINDER_WORK_PREFIX${reminder.id}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }
    
    fun cancelTransactionReminder(reminderId: String) {
        WorkManager.getInstance(context)
            .cancelUniqueWork("$TRANSACTION_REMINDER_WORK_PREFIX$reminderId")
    }
    
    fun cancelBillReminder(reminderId: String) {
        WorkManager.getInstance(context)
            .cancelUniqueWork("$BILL_REMINDER_WORK_PREFIX$reminderId")
    }
    
    fun scheduleRecurringChecks() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val recurringCheckRequest = PeriodicWorkRequestBuilder<RecurringReminderWorker>(
            repeatInterval = 24, // Check daily
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag("recurring_reminders")
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                RECURRING_CHECK_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                recurringCheckRequest
            )
    }
    
    fun cancelAllReminders() {
        WorkManager.getInstance(context).cancelAllWorkByTag("transaction_reminders")
        WorkManager.getInstance(context).cancelAllWorkByTag("bill_reminders")
        WorkManager.getInstance(context).cancelAllWorkByTag("recurring_reminders")
    }
    
    // Predefined reminder templates
    fun getCommonTransactionReminders(): List<TransactionReminder> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        
        return listOf(
            // Daily coffee reminder
            TransactionReminder(
                id = "daily_coffee",
                title = "Recordatorio de Café",
                description = "¿Compraste tu café de hoy?",
                amount = 5.0,
                type = "Gasto",
                scheduledTime = now + (2 * 60 * 60 * 1000), // 2 hours from now
                isRecurring = true,
                recurringInterval = RecurringInterval.DAILY
            ),
            
            // Weekly grocery reminder
            TransactionReminder(
                id = "weekly_groceries",
                title = "Compras de la Semana",
                description = "Recordatorio para registrar las compras del supermercado",
                amount = 100.0,
                type = "Gasto",
                scheduledTime = getNextWeekday(calendar, Calendar.SATURDAY, 10), // Next Saturday at 10 AM
                isRecurring = true,
                recurringInterval = RecurringInterval.WEEKLY
            ),
            
            // Monthly salary reminder
            TransactionReminder(
                id = "monthly_salary",
                title = "Registro de Salario",
                description = "Recordatorio para registrar el salario mensual",
                type = "Ingreso",
                scheduledTime = getNextMonthDay(calendar, 1, 9), // 1st of next month at 9 AM
                isRecurring = true,
                recurringInterval = RecurringInterval.MONTHLY
            )
        )
    }
    
    fun getCommonBillReminders(): List<BillReminder> {
        val calendar = Calendar.getInstance()
        
        return listOf(
            // Monthly rent
            BillReminder(
                id = "monthly_rent",
                billName = "Renta/Alquiler",
                amount = 800.0,
                dueDate = getNextMonthDay(calendar, 1, 0), // 1st of next month
                isRecurring = true,
                recurringInterval = RecurringInterval.MONTHLY,
                reminderDaysBefore = 3
            ),
            
            // Electricity bill
            BillReminder(
                id = "electricity_bill",
                billName = "Factura de Electricidad",
                amount = 150.0,
                dueDate = getNextMonthDay(calendar, 15, 0), // 15th of next month
                isRecurring = true,
                recurringInterval = RecurringInterval.MONTHLY,
                reminderDaysBefore = 5
            ),
            
            // Internet bill
            BillReminder(
                id = "internet_bill",
                billName = "Factura de Internet",
                amount = 50.0,
                dueDate = getNextMonthDay(calendar, 20, 0), // 20th of next month
                isRecurring = true,
                recurringInterval = RecurringInterval.MONTHLY,
                reminderDaysBefore = 3
            )
        )
    }
    
    private fun getNextWeekday(calendar: Calendar, dayOfWeek: Int, hour: Int): Long {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.WEEK_OF_YEAR, 1)
        }
        
        return cal.timeInMillis
    }
    
    private fun getNextMonthDay(calendar: Calendar, dayOfMonth: Int, hour: Int): Long {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.MONTH, 1)
        }
        
        return cal.timeInMillis
    }
}

// Worker classes
class TransactionReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val notificationService = NotificationService(applicationContext)
            val preferencesManager = PreferencesManager(applicationContext)
            
            if (!preferencesManager.notificationsEnabled) {
                return Result.success()
            }
            
            val title = inputData.getString("title") ?: "Recordatorio de Transacción"
            val description = inputData.getString("description") ?: ""
            val hasAmount = inputData.getBoolean("has_amount", false)
            val amount = if (hasAmount) inputData.getDouble("amount", 0.0) else null
            val isRecurring = inputData.getBoolean("is_recurring", false)
            
            notificationService.showTransactionReminderNotification(
                title = title,
                description = description,
                amount = amount
            )
            
            // Schedule next occurrence if recurring
            if (isRecurring) {
                val reminderId = inputData.getString("reminder_id") ?: return Result.success()
                val intervalName = inputData.getString("recurring_interval") ?: "NONE"
                val interval = RecurringInterval.valueOf(intervalName)
                
                scheduleNextRecurrence(reminderId, interval)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private fun scheduleNextRecurrence(reminderId: String, interval: RecurringInterval) {
        val calendar = Calendar.getInstance()
        val nextTime = when (interval) {
            RecurringInterval.DAILY -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.timeInMillis
            }
            RecurringInterval.WEEKLY -> {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                calendar.timeInMillis
            }
            RecurringInterval.MONTHLY -> {
                calendar.add(Calendar.MONTH, 1)
                calendar.timeInMillis
            }
            RecurringInterval.YEARLY -> {
                calendar.add(Calendar.YEAR, 1)
                calendar.timeInMillis
            }
            else -> return
        }
        
        val delay = nextTime - System.currentTimeMillis()
        if (delay > 0) {
            val workRequest = OneTimeWorkRequestBuilder<TransactionReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("transaction_reminders")
                .build()
            
            WorkManager.getInstance(applicationContext)
                .enqueueUniqueWork(
                    "${ReminderService.TRANSACTION_REMINDER_WORK_PREFIX}$reminderId",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }
    }
}

class BillReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val notificationService = NotificationService(applicationContext)
            val preferencesManager = PreferencesManager(applicationContext)
            
            if (!preferencesManager.notificationsEnabled) {
                return Result.success()
            }
            
            val billName = inputData.getString("bill_name") ?: "Factura"
            val amount = inputData.getDouble("amount", 0.0)
            val dueDate = inputData.getString("due_date") ?: ""
            val isRecurring = inputData.getBoolean("is_recurring", false)
            
            notificationService.showBillReminderNotification(
                billName = billName,
                amount = amount,
                dueDate = dueDate
            )
            
            // Schedule next occurrence if recurring
            if (isRecurring) {
                val reminderId = inputData.getString("reminder_id") ?: return Result.success()
                val intervalName = inputData.getString("recurring_interval") ?: "MONTHLY"
                val interval = RecurringInterval.valueOf(intervalName)
                
                scheduleNextBillReminder(reminderId, interval)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private fun scheduleNextBillReminder(reminderId: String, interval: RecurringInterval) {
        val calendar = Calendar.getInstance()
        val nextTime = when (interval) {
            RecurringInterval.MONTHLY -> {
                calendar.add(Calendar.MONTH, 1)
                calendar.timeInMillis
            }
            RecurringInterval.YEARLY -> {
                calendar.add(Calendar.YEAR, 1)
                calendar.timeInMillis
            }
            else -> return
        }
        
        val delay = nextTime - System.currentTimeMillis()
        if (delay > 0) {
            val workRequest = OneTimeWorkRequestBuilder<BillReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("bill_reminders")
                .build()
            
            WorkManager.getInstance(applicationContext)
                .enqueueUniqueWork(
                    "${ReminderService.BILL_REMINDER_WORK_PREFIX}$reminderId",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }
    }
}

class RecurringReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            // This worker runs daily to check for any missed recurring reminders
            // and reschedule them if necessary
            
            val preferencesManager = PreferencesManager(applicationContext)
            if (!preferencesManager.notificationsEnabled) {
                return Result.success()
            }
            
            // TODO: Implement logic to check and reschedule missed recurring reminders
            // This would typically involve checking a database of scheduled reminders
            // and ensuring they are properly scheduled in WorkManager
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}