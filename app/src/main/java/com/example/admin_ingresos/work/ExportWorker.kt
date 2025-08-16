package com.example.admin_ingresos.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.admin_ingresos.AppDatabaseProvider
import com.example.admin_ingresos.data.ExportService

class ExportWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabaseProvider.getDatabase(applicationContext)
            // Simple scheduled export: export last 7 days transactions to XLSX
            val since = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
            val now = System.currentTimeMillis()
            val transactions = db.transactionDao().getTransactionsByDateRange(since, now)
            val categories = db.categoryDao().getCategoriesList()
            val paymentMethods = db.paymentMethodDao().getAll()

            val service = ExportService(applicationContext)
            val result = service.exportTransactionsToXlsx(transactions, categories, paymentMethods)
            if (result.uri != null) {
                if (result.usedFallback) {
                    // fallback to CSV used — log and still succeed
                    // You may want to notify the user via NotificationManager
                }
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
