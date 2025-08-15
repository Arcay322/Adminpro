package com.example.admin_ingresos.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.font.PdfFont
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
            
            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)
            
            // Add title
            val titleParagraph = Paragraph(reportTitle)
                .setFontSize(18f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
            document.add(titleParagraph)
            
            // Add generation date
            val dateParagraph = Paragraph("Generado el: ${dateFormat.format(Date())}")
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20f)
            document.add(dateParagraph)
            
            // Add summary statistics
            val totalIncome = transactions.filter { it.type == "Ingreso" }.sumOf { it.amount }
            val totalExpenses = transactions.filter { it.type == "Gasto" }.sumOf { it.amount }
            val balance = totalIncome - totalExpenses
            
            val summaryTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f, 1f)))
                .setWidth(UnitValue.createPercentValue(100f))
            
            // Summary headers
            summaryTable.addHeaderCell(Cell().add(Paragraph("Total Ingresos").setBold()))
            summaryTable.addHeaderCell(Cell().add(Paragraph("Total Gastos").setBold()))
            summaryTable.addHeaderCell(Cell().add(Paragraph("Balance").setBold()))
            
            // Summary values
            summaryTable.addCell(Cell().add(Paragraph("$${String.format("%.2f", totalIncome)}")))
            summaryTable.addCell(Cell().add(Paragraph("$${String.format("%.2f", totalExpenses)}")))
            summaryTable.addCell(Cell().add(Paragraph("$${String.format("%.2f", balance)}")))
            
            document.add(summaryTable)
            document.add(Paragraph("\n"))
            
            // Add transactions table
            val transactionsTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 3f, 1.5f, 1f, 2f)))
                .setWidth(UnitValue.createPercentValue(100f))
            
            // Table headers
            transactionsTable.addHeaderCell(Cell().add(Paragraph("Fecha").setBold()))
            transactionsTable.addHeaderCell(Cell().add(Paragraph("Descripción").setBold()))
            transactionsTable.addHeaderCell(Cell().add(Paragraph("Monto").setBold()))
            transactionsTable.addHeaderCell(Cell().add(Paragraph("Tipo").setBold()))
            transactionsTable.addHeaderCell(Cell().add(Paragraph("Categoría").setBold()))
            
            // Add transaction rows
            transactions.forEach { transaction ->
                val category = categories.find { it.id == transaction.categoryId }
                
                transactionsTable.addCell(Cell().add(Paragraph(dateFormat.format(Date(transaction.date)))))
                transactionsTable.addCell(Cell().add(Paragraph(transaction.description)))
                transactionsTable.addCell(Cell().add(Paragraph("$${String.format("%.2f", transaction.amount)}")))
                transactionsTable.addCell(Cell().add(Paragraph(transaction.type)))
                transactionsTable.addCell(Cell().add(Paragraph(category?.name ?: "Sin categoría")))
            }
            
            document.add(transactionsTable)
            
            // Add footer
            document.add(Paragraph("\n"))
            val footerParagraph = Paragraph("Total de transacciones: ${transactions.size}")
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER)
            document.add(footerParagraph)
            
            document.close()
            
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
            
            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)
            
            // Add title
            val titleParagraph = Paragraph(reportTitle)
                .setFontSize(18f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
            document.add(titleParagraph)
            
            // Add generation date
            val dateParagraph = Paragraph("Generado el: ${dateFormat.format(Date())}")
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20f)
            document.add(dateParagraph)
            
            // Add summary statistics
            val totalBudget = budgets.sumOf { it.budget.amount }
            val totalSpent = budgets.sumOf { it.spent }
            val totalRemaining = budgets.sumOf { it.remaining }
            
            val summaryTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f, 1f)))
                .setWidth(UnitValue.createPercentValue(100f))
            
            // Summary headers
            summaryTable.addHeaderCell(Cell().add(Paragraph("Total Presupuestado").setBold()))
            summaryTable.addHeaderCell(Cell().add(Paragraph("Total Gastado").setBold()))
            summaryTable.addHeaderCell(Cell().add(Paragraph("Total Restante").setBold()))
            
            // Summary values
            summaryTable.addCell(Cell().add(Paragraph("$${String.format("%.2f", totalBudget)}")))
            summaryTable.addCell(Cell().add(Paragraph("$${String.format("%.2f", totalSpent)}")))
            summaryTable.addCell(Cell().add(Paragraph("$${String.format("%.2f", totalRemaining)}")))
            
            document.add(summaryTable)
            document.add(Paragraph("\n"))
            
            // Add budgets table
            val budgetsTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 1.5f, 1.5f, 1.5f, 1f, 1.5f, 1f)))
                .setWidth(UnitValue.createPercentValue(100f))
            
            // Table headers
            budgetsTable.addHeaderCell(Cell().add(Paragraph("Categoría").setBold()))
            budgetsTable.addHeaderCell(Cell().add(Paragraph("Presupuesto").setBold()))
            budgetsTable.addHeaderCell(Cell().add(Paragraph("Gastado").setBold()))
            budgetsTable.addHeaderCell(Cell().add(Paragraph("Restante").setBold()))
            budgetsTable.addHeaderCell(Cell().add(Paragraph("Porcentaje").setBold()))
            budgetsTable.addHeaderCell(Cell().add(Paragraph("Estado").setBold()))
            budgetsTable.addHeaderCell(Cell().add(Paragraph("Días Rest.").setBold()))
            
            // Add budget rows
            budgets.forEach { budgetProgress ->
                budgetsTable.addCell(Cell().add(Paragraph(budgetProgress.category.name)))
                budgetsTable.addCell(Cell().add(Paragraph("$${String.format("%.2f", budgetProgress.budget.amount)}")))
                budgetsTable.addCell(Cell().add(Paragraph("$${String.format("%.2f", budgetProgress.spent)}")))
                budgetsTable.addCell(Cell().add(Paragraph("$${String.format("%.2f", budgetProgress.remaining)}")))
                budgetsTable.addCell(Cell().add(Paragraph("${(budgetProgress.percentage * 100).toInt()}%")))
                budgetsTable.addCell(Cell().add(Paragraph(budgetProgress.status.displayName)))
                budgetsTable.addCell(Cell().add(Paragraph(budgetProgress.daysRemaining.toString())))
            }
            
            document.add(budgetsTable)
            
            // Add footer
            document.add(Paragraph("\n"))
            val footerParagraph = Paragraph("Total de presupuestos activos: ${budgets.size}")
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER)
            document.add(footerParagraph)
            
            document.close()
            
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