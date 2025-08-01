package com.example.admin_ingresos.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.admin_ingresos.MainActivity
import com.example.admin_ingresos.R

class NotificationService(private val context: Context) {
    
    companion object {
        const val BUDGET_CHANNEL_ID = "budget_alerts"
        const val REMINDER_CHANNEL_ID = "reminders"
        const val GENERAL_CHANNEL_ID = "general"
        
        const val BUDGET_WARNING_ID = 1001
        const val BUDGET_EXCEEDED_ID = 1002
        const val BUDGET_CRITICAL_ID = 1003
        const val TRANSACTION_REMINDER_ID = 2001
        const val BILL_REMINDER_ID = 2002
    }
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Budget alerts channel
            val budgetChannel = NotificationChannel(
                BUDGET_CHANNEL_ID,
                "Alertas de Presupuesto",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones cuando te acercas o superas tus límites de presupuesto"
                enableVibration(true)
                setShowBadge(true)
            }
            
            // Reminders channel
            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Recordatorios",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorios de transacciones y pagos"
                enableVibration(true)
                setShowBadge(true)
            }
            
            // General channel
            val generalChannel = NotificationChannel(
                GENERAL_CHANNEL_ID,
                "General",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones generales de la aplicación"
                setShowBadge(false)
            }
            
            notificationManager.createNotificationChannels(listOf(
                budgetChannel,
                reminderChannel,
                generalChannel
            ))
        }
    }
    
    fun showBudgetWarningNotification(
        categoryName: String,
        spent: Double,
        limit: Double,
        percentage: Float
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "budget")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            BUDGET_WARNING_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, BUDGET_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: Add proper notification icon
            .setContentTitle("⚠️ Alerta de Presupuesto")
            .setContentText("Has gastado ${String.format("%.0f", percentage)}% de tu presupuesto en $categoryName")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Has gastado $${String.format("%.2f", spent)} de $${String.format("%.2f", limit)} en $categoryName (${String.format("%.0f", percentage)}%)"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(context.getColor(android.R.color.holo_orange_dark))
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Ver Presupuestos",
                pendingIntent
            )
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(BUDGET_WARNING_ID, notification)
        }
    }
    
    fun showBudgetExceededNotification(
        categoryName: String,
        spent: Double,
        limit: Double,
        overspent: Double
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "budget")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            BUDGET_EXCEEDED_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, BUDGET_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🚨 Presupuesto Superado")
            .setContentText("Has superado tu presupuesto de $categoryName por $${String.format("%.2f", overspent)}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Has gastado $${String.format("%.2f", spent)} en $categoryName, superando tu límite de $${String.format("%.2f", limit)} por $${String.format("%.2f", overspent)}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(context.getColor(android.R.color.holo_red_dark))
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Ajustar Presupuesto",
                pendingIntent
            )
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(BUDGET_EXCEEDED_ID, notification)
        }
    }
    
    fun showBudgetCriticalNotification(
        categoryName: String,
        spent: Double,
        limit: Double,
        percentage: Float
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "budget")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            BUDGET_CRITICAL_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, BUDGET_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🔥 Presupuesto Crítico")
            .setContentText("Solo te queda ${String.format("%.0f", 100 - percentage)}% de tu presupuesto en $categoryName")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Has gastado $${String.format("%.2f", spent)} de $${String.format("%.2f", limit)} en $categoryName. ¡Cuidado con los próximos gastos!"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(context.getColor(android.R.color.holo_red_dark))
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Ver Detalles",
                pendingIntent
            )
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(BUDGET_CRITICAL_ID, notification)
        }
    }
    
    fun showTransactionReminderNotification(
        title: String,
        description: String,
        amount: Double? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "addTransaction")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            TRANSACTION_REMINDER_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val contentText = if (amount != null) {
            "$description - $${String.format("%.2f", amount)}"
        } else {
            description
        }
        
        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📝 $title")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Agregar Transacción",
                pendingIntent
            )
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(TRANSACTION_REMINDER_ID, notification)
        }
    }
    
    fun showBillReminderNotification(
        billName: String,
        amount: Double,
        dueDate: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "addTransaction")
            putExtra("transaction_type", "Gasto")
            putExtra("transaction_description", billName)
            putExtra("transaction_amount", amount)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            BILL_REMINDER_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💳 Recordatorio de Pago")
            .setContentText("$billName vence el $dueDate")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Recordatorio: $billName por $${String.format("%.2f", amount)} vence el $dueDate"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Pagar Ahora",
                pendingIntent
            )
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(BILL_REMINDER_ID, notification)
        }
    }
    
    fun cancelNotification(notificationId: Int) {
        with(NotificationManagerCompat.from(context)) {
            cancel(notificationId)
        }
    }
    
    fun cancelAllNotifications() {
        with(NotificationManagerCompat.from(context)) {
            cancelAll()
        }
    }
}