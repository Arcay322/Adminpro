package com.example.admin_ingresos.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.random.Random

object SampleDataProvider {

    val defaultCategories = listOf(
        Category(name = "Alimentación", icon = "UtensilsCrossed", color = "#FF6B6B", isFavorite = true),
        Category(name = "Transporte", icon = "Car", color = "#4ECDC4", isFavorite = true),
        Category(name = "Entretenimiento", icon = "Gamepad2", color = "#45B7D1", isFavorite = false),
        Category(name = "Salud", icon = "Heart", color = "#96CEB4", isFavorite = false),
        Category(name = "Educación", icon = "BookOpen", color = "#FFEAA7", isFavorite = false),
        Category(name = "Compras", icon = "ShoppingCart", color = "#DDA0DD", isFavorite = true),
        Category(name = "Servicios", icon = "Settings", color = "#98D8C8", isFavorite = false),
        Category(name = "Trabajo", icon = "Briefcase", color = "#F7DC6F", isFavorite = true),
        Category(name = "Hogar", icon = "Home", color = "#BB8FCE", isFavorite = false),
        Category(name = "Otros", icon = "Package", color = "#85C1E9", isFavorite = false)
    )

    val defaultPaymentMethods = listOf(
        PaymentMethod(name = "Efectivo", icon = "💵"),
        PaymentMethod(name = "Tarjeta de Débito", icon = "💳"),
        PaymentMethod(name = "Tarjeta de Crédito", icon = "💳"),
        PaymentMethod(name = "Transferencia", icon = "🏦"),
        PaymentMethod(name = "PayPal", icon = "📱"),
        PaymentMethod(name = "Otro", icon = "💰")
    )

    private val sampleDescriptions = mapOf(
        "Alimentación" to listOf("Supermercado", "Restaurante", "Comida rápida", "Cafetería"),
        "Transporte" to listOf("Gasolina", "Uber", "Taxi", "Autobús"),
        "Entretenimiento" to listOf("Cine", "Concierto", "Streaming", "Videojuegos"),
        "Salud" to listOf("Farmacia", "Doctor", "Dentista", "Gimnasio"),
        "Educación" to listOf("Curso online", "Libros", "Universidad", "Material escolar"),
        "Compras" to listOf("Ropa", "Zapatos", "Electrónicos", "Regalos"),
        "Servicios" to listOf("Internet", "Teléfono", "Electricidad", "Agua"),
        "Trabajo" to listOf("Salario", "Freelance", "Bonificación", "Proyecto"),
        "Hogar" to listOf("Alquiler", "Hipoteca", "Muebles", "Reparaciones"),
        "Otros" to listOf("Varios", "Imprevisto", "Donación", "Ahorro")
    )

    suspend fun initializeSampleData(database: AppDatabase) = withContext(Dispatchers.IO) {
        val categoryDao = database.categoryDao()
        val paymentMethodDao = database.paymentMethodDao()
        val transactionDao = database.transactionDao()

        if (categoryDao.getCategoriesList().isEmpty() &&
            paymentMethodDao.getAll().isEmpty() &&
            transactionDao.getAll().isEmpty()) {

            val categoryIds = defaultCategories.associate { category ->
                val id = categoryDao.insert(category)
                category.name to id.toInt()
            }

            val paymentMethodIds = defaultPaymentMethods.map { paymentMethod ->
                paymentMethodDao.insert(paymentMethod).toInt()
            }

            generateSampleTransactions(database, categoryIds, paymentMethodIds)
        }
    }

    private suspend fun generateSampleTransactions(
        database: AppDatabase,
        categoryIds: Map<String, Int>,
        paymentMethodIds: List<Int>
    ) {
        val calendar = Calendar.getInstance()
        val transactions = mutableListOf<Transaction>()

        for (dayOffset in 0..89) {
            calendar.timeInMillis = System.currentTimeMillis() - (dayOffset * 24 * 60 * 60 * 1000L)
            val transactionsPerDay = Random.nextInt(1, 5)

            repeat(transactionsPerDay) {
                val isIncome = Random.nextDouble() < 0.2
                val randomCategory = defaultCategories.random()
                val descriptions = sampleDescriptions[randomCategory.name] ?: listOf("Gasto")

                val transaction = if (isIncome) {
                    Transaction(
                        amount = Random.nextDouble(500.0, 3000.0),
                        type = "Ingreso",
                        categoryId = categoryIds["Trabajo"] ?: 1,
                        description = sampleDescriptions["Trabajo"]?.random() ?: "Ingreso",
                        date = calendar.timeInMillis,
                        paymentMethodId = paymentMethodIds.randomOrNull()
                    )
                } else {
                    Transaction(
                        amount = Random.nextDouble(10.0, 500.0),
                        type = "Gasto",
                        categoryId = categoryIds[randomCategory.name] ?: 1,
                        description = descriptions.random(),
                        date = calendar.timeInMillis,
                        paymentMethodId = paymentMethodIds.randomOrNull()
                    )
                }
                transactions.add(transaction)
            }
        }
        database.transactionDao().insertAll(transactions)
    }

    suspend fun ensureBudgetTemplateCategories(database: AppDatabase) = withContext(Dispatchers.IO) {
        val categoryDao = database.categoryDao()
        val existingCategories = categoryDao.getCategoriesList()
        val requiredCategories = listOf("Alimentación", "Transporte", "Entretenimiento", "Compras")

        requiredCategories.forEach { requiredName ->
            if (existingCategories.none { it.name.equals(requiredName, ignoreCase = true) }) {
                val category = defaultCategories.find { it.name.equals(requiredName, ignoreCase = true) }
                category?.let { categoryDao.insert(it) }
            }
        }
    }
}
