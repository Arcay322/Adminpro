package com.example.admin_ingresos.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.BufferedWriter
import java.text.SimpleDateFormat
import java.util.*

class ExportService(private val context: Context) {
    
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val fileNameDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    
    suspend fun exportTransactionsToCSV(
        transactions: List<Transaction>,
        categories: List<Category>,
        paymentMethods: List<PaymentMethod>,
        includeHeaders: Boolean = true,
        customFields: List<ExportField> = ExportField.getDefaultFields()
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val fileName = "transacciones_${fileNameDateFormat.format(Date())}.csv"
            val file = File(context.getExternalFilesDir(null), fileName)
            // Helper to escape CSV values
            fun escapeCsv(value: String?): String {
                if (value == null) return "\"\""
                val escaped = value.replace("\"", "\"\"")
                return "\"$escaped\""
            }

            // Sort descending by date to produce predictable order
            val sortedTransactions = transactions.sortedByDescending { it.date }

            FileOutputStream(file).use { fos ->
                // Write UTF-8 BOM so Excel on Windows recognizes encoding
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                OutputStreamWriter(fos, Charsets.UTF_8).use { osw ->
                    BufferedWriter(osw).use { writer ->
                        if (includeHeaders) {
                            val headers = customFields.joinToString(",") { it.displayName }
                            writer.append(headers).append("\r\n")
                        }

                        sortedTransactions.forEach { transaction ->
                            val category = categories.find { it.id == transaction.categoryId }
                            val paymentMethod = paymentMethods.find { it.id == transaction.paymentMethodId }

                            val row = customFields.joinToString(",") { field ->
                                when (field) {
                                    ExportField.DATE -> escapeCsv(dateFormat.format(Date(transaction.date)))
                                    ExportField.DESCRIPTION -> escapeCsv(transaction.description)
                                    ExportField.AMOUNT -> transaction.amount.toString()
                                    ExportField.TYPE -> escapeCsv(transaction.type)
                                    ExportField.CATEGORY -> escapeCsv(category?.name ?: "Sin categoría")
                                    ExportField.PAYMENT_METHOD -> escapeCsv(paymentMethod?.name ?: "No especificado")
                                    ExportField.CATEGORY_ICON -> escapeCsv(category?.icon ?: "")
                                    ExportField.PAYMENT_METHOD_ICON -> escapeCsv(paymentMethod?.icon ?: "")
                                }
                            }
                            writer.append(row).append("\r\n")
                        }
                        writer.flush()
                    }
                }
            }
            
            // Return file URI using FileProvider
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun exportBudgetsToCSV(
        budgets: List<BudgetProgress>,
        includeHeaders: Boolean = true
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val fileName = "presupuestos_${fileNameDateFormat.format(Date())}.csv"
            val file = File(context.getExternalFilesDir(null), fileName)
            fun escapeCsv(value: String?): String {
                if (value == null) return "\"\""
                val escaped = value.replace("\"", "\"\"")
                return "\"$escaped\""
            }

            FileOutputStream(file).use { fos ->
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                OutputStreamWriter(fos, Charsets.UTF_8).use { osw ->
                    BufferedWriter(osw).use { writer ->
                        if (includeHeaders) {
                            writer.append("Categoría,Presupuesto,Gastado,Restante,Porcentaje,Estado,Días Restantes,Período").append("\r\n")
                        }

                        budgets.forEach { budgetProgress ->
                            val row = listOf(
                                escapeCsv(budgetProgress.category.name),
                                budgetProgress.budget.amount.toString(),
                                budgetProgress.spent.toString(),
                                budgetProgress.remaining.toString(),
                                "${(budgetProgress.percentage * 100).toInt()}%",
                                escapeCsv(budgetProgress.status.displayName),
                                budgetProgress.daysRemaining.toString(),
                                escapeCsv(budgetProgress.budget.period.displayName)
                            ).joinToString(",")

                            writer.append(row).append("\r\n")
                        }

                        writer.flush()
                    }
                }
            }
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun generateTransactionsPDFReport(
        transactions: List<Transaction>,
        categories: List<Category>,
        paymentMethods: List<PaymentMethod>,
        reportTitle: String = "Reporte de Transacciones"
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val fileName = "reporte_transacciones_${fileNameDateFormat.format(Date())}.pdf"
            val file = File(context.getExternalFilesDir(null), fileName)
            // Use Android PdfDocument to create a simple PDF (no external licensing)
            val pdf = PdfDocument()

            // Simple page config
            var currentPageNum = 1
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create() // A4-like in points
            var page = pdf.startPage(pageInfo)
            var canvas: Canvas = page.canvas
            val paint = Paint().apply { isAntiAlias = true }

            var y = 40f
            paint.textSize = 18f
            paint.isFakeBoldText = true
            val titleX = (pageInfo.pageWidth / 2).toFloat()
            canvas.drawText(reportTitle, 40f, y, paint)

            y += 24f
            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("Generado el: ${dateFormat.format(Date())}", 40f, y, paint)

            y += 24f
            // Summary
            val totalIncome = transactions.filter { it.type == "Ingreso" }.sumOf { it.amount }
            val totalExpenses = transactions.filter { it.type == "Gasto" }.sumOf { it.amount }
            val balance = totalIncome - totalExpenses

            paint.textSize = 12f
            paint.isFakeBoldText = true
            canvas.drawText("Resumen:", 40f, y, paint)
            paint.isFakeBoldText = false
            y += 18f
            canvas.drawText("Total Ingresos: $${String.format("%.2f", totalIncome)}", 40f, y, paint)
            y += 14f
            canvas.drawText("Total Gastos: $${String.format("%.2f", totalExpenses)}", 40f, y, paint)
            y += 14f
            canvas.drawText("Balance: $${String.format("%.2f", balance)}", 40f, y, paint)

            y += 22f
            paint.isFakeBoldText = true
            canvas.drawText("Transacciones:", 40f, y, paint)
            paint.isFakeBoldText = false
            y += 18f

            paint.textSize = 10f
            // Table-like rendering: Date | Desc | Amount | Type | Category
            val colX = listOf(40f, 140f, 380f, 450f, 510f)
            // Header
            canvas.drawText("Fecha", colX[0], y, paint)
            canvas.drawText("Descripción", colX[1], y, paint)
            canvas.drawText("Monto", colX[2], y, paint)
            canvas.drawText("Tipo", colX[3], y, paint)
            canvas.drawText("Categoría", colX[4], y, paint)

            y += 12f
            // Sort by date desc for consistency
            val sorted = transactions.sortedByDescending { it.date }
            for (t in sorted) {
                if (y > pageInfo.pageHeight - 60) {
                    pdf.finishPage(page)
                    // start a new page
                    currentPageNum += 1
                    val nextPageInfo = PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create()
                    page = pdf.startPage(nextPageInfo)
                    canvas = page.canvas
                    y = 40f
                }

                canvas.drawText(dateFormat.format(Date(t.date)), colX[0], y, paint)
                // Truncate description
                val desc = if (t.description.length > 30) t.description.substring(0, 27) + "..." else t.description
                canvas.drawText(desc, colX[1], y, paint)
                canvas.drawText("$${String.format("%.2f", t.amount)}", colX[2], y, paint)
                canvas.drawText(t.type, colX[3], y, paint)
                val categoryName = categories.find { it.id == t.categoryId }?.name ?: "Sin categoría"
                canvas.drawText(categoryName, colX[4], y, paint)
                y += 12f
            }

            pdf.finishPage(page)

            // Write to file
            FileOutputStream(file).use { out ->
                pdf.writeTo(out)
            }
            pdf.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun generateBudgetsPDFReport(
        budgets: List<BudgetProgress>,
        reportTitle: String = "Reporte de Presupuestos"
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val fileName = "reporte_presupuestos_${fileNameDateFormat.format(Date())}.pdf"
            val file = File(context.getExternalFilesDir(null), fileName)
            // Use PdfDocument for budgets report
            val pdf = PdfDocument()
            var currentPageNum = 1
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create()
            var page = pdf.startPage(pageInfo)
            var canvas = page.canvas
            val paint = Paint().apply { isAntiAlias = true }

            var y = 40f
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText(reportTitle, 40f, y, paint)
            y += 24f
            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("Generado el: ${dateFormat.format(Date())}", 40f, y, paint)

            y += 24f
            val totalBudget = budgets.sumOf { it.budget.amount }
            val totalSpent = budgets.sumOf { it.spent }
            val totalRemaining = budgets.sumOf { it.remaining }

            paint.textSize = 12f
            paint.isFakeBoldText = true
            canvas.drawText("Resumen:", 40f, y, paint)
            paint.isFakeBoldText = false
            y += 18f
            canvas.drawText("Total Presupuestado: $${String.format("%.2f", totalBudget)}", 40f, y, paint)
            y += 14f
            canvas.drawText("Total Gastado: $${String.format("%.2f", totalSpent)}", 40f, y, paint)
            y += 14f
            canvas.drawText("Total Restante: $${String.format("%.2f", totalRemaining)}", 40f, y, paint)

            y += 22f
            paint.isFakeBoldText = true
            canvas.drawText("Presupuestos:", 40f, y, paint)
            paint.isFakeBoldText = false
            y += 18f

            paint.textSize = 10f
            val colX = listOf(40f, 220f, 320f, 420f, 480f)
            canvas.drawText("Categoría", colX[0], y, paint)
            canvas.drawText("Presupuesto", colX[1], y, paint)
            canvas.drawText("Gastado", colX[2], y, paint)
            canvas.drawText("Restante", colX[3], y, paint)
            canvas.drawText("%", colX[4], y, paint)
            y += 12f

            for (b in budgets) {
                if (y > pageInfo.pageHeight - 60) {
                    pdf.finishPage(page)
                    currentPageNum += 1
                    val nextPageInfo = PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create()
                    page = pdf.startPage(nextPageInfo)
                    canvas = page.canvas
                    y = 40f
                }
                canvas.drawText(b.category.name, colX[0], y, paint)
                canvas.drawText("$${String.format("%.2f", b.budget.amount)}", colX[1], y, paint)
                canvas.drawText("$${String.format("%.2f", b.spent)}", colX[2], y, paint)
                canvas.drawText("$${String.format("%.2f", b.remaining)}", colX[3], y, paint)
                canvas.drawText("${(b.percentage * 100).toInt()}%", colX[4], y, paint)
                y += 12f
            }

            pdf.finishPage(page)
            FileOutputStream(file).use { out -> pdf.writeTo(out) }
            pdf.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun shareFile(uri: Uri, mimeType: String = "text/csv", title: String = "Compartir archivo") {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Reporte de Admin Ingresos")
            putExtra(Intent.EXTRA_TEXT, "Adjunto encontrarás el reporte generado desde Admin Ingresos.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooserIntent = Intent.createChooser(shareIntent, title)
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }
    
    fun shareMultipleFiles(uris: List<Uri>, mimeType: String = "*/*", title: String = "Compartir archivos") {
        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = mimeType
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            putExtra(Intent.EXTRA_SUBJECT, "Reportes de Admin Ingresos")
            putExtra(Intent.EXTRA_TEXT, "Adjunto encontrarás los reportes generados desde Admin Ingresos.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooserIntent = Intent.createChooser(shareIntent, title)
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }
    
    fun shareTextSummary(
        transactions: List<Transaction>,
        categories: List<Category>,
        title: String = "Resumen Financiero"
    ) {
        val totalIncome = transactions.filter { it.type == "Ingreso" }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.type == "Gasto" }.sumOf { it.amount }
        val balance = totalIncome - totalExpenses
        
        val summary = buildString {
            appendLine("📊 $title")
            appendLine("Generado el: ${dateFormat.format(Date())}")
            appendLine()
            appendLine("💰 Resumen Financiero:")
            appendLine("• Total Ingresos: $${String.format("%.2f", totalIncome)}")
            appendLine("• Total Gastos: $${String.format("%.2f", totalExpenses)}")
            appendLine("• Balance: $${String.format("%.2f", balance)}")
            appendLine("• Total Transacciones: ${transactions.size}")
            appendLine()
            
            if (categories.isNotEmpty()) {
                appendLine("📋 Categorías más utilizadas:")
                val categoryUsage = transactions.groupBy { it.categoryId }
                    .mapValues { it.value.size }
                    .entries
                    .sortedByDescending { it.value }
                    .take(5)
                
                categoryUsage.forEach { (categoryId, count) ->
                    val category = categories.find { it.id == categoryId }
                    appendLine("• ${category?.name ?: "Sin categoría"}: $count transacciones")
                }
            }
            
            appendLine()
            appendLine("📱 Generado con Admin Ingresos")
        }
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
            putExtra(Intent.EXTRA_SUBJECT, title)
        }
        
        val chooserIntent = Intent.createChooser(shareIntent, "Compartir resumen")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }
}

enum class ExportField(val displayName: String) {
    DATE("Fecha"),
    DESCRIPTION("Descripción"),
    AMOUNT("Monto"),
    TYPE("Tipo"),
    CATEGORY("Categoría"),
    PAYMENT_METHOD("Método de Pago"),
    CATEGORY_ICON("Icono Categoría"),
    PAYMENT_METHOD_ICON("Icono Método de Pago");
    
    companion object {
        fun getDefaultFields(): List<ExportField> {
            return listOf(DATE, DESCRIPTION, AMOUNT, TYPE, CATEGORY, PAYMENT_METHOD)
        }
        
        fun getAllFields(): List<ExportField> {
            return values().toList()
        }
    }
}

enum class ExportFormat(val displayName: String, val extension: String, val mimeType: String) {
    CSV("CSV (Excel)", "csv", "text/csv"),
    PDF("PDF (Reporte)", "pdf", "application/pdf")
}