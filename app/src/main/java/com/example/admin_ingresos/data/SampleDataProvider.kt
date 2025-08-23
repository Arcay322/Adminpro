package com.example.admin_ingresos.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.random.Random

object SampleDataProvider {

    // Default categories with Lucide icons and colors
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

    // Default payment methods
    val defaultPaymentMethods = listOf(
        PaymentMethod(name = "Efectivo", icon = "💵"),
        PaymentMethod(name = "Tarjeta de Débito", icon = "💳"),
        PaymentMethod(name = "Tarjeta de Crédito", icon = "💳"),
        PaymentMethod(name = "Transferencia", icon = "🏦"),
        PaymentMethod(name = "PayPal", icon = "📱"),
        PaymentMethod(name = "Otro", icon = "💰")
    )

    // Sample transaction descriptions by category
    private val sampleDescriptions = mapOf(
        "Alimentación" to listOf(
            "Supermercado", "Restaurante", "Comida rápida", "Cafetería",
            "Panadería", "Mercado", "Delivery", "Almuerzo trabajo"
        ),
        "Transporte" to listOf(
            "Gasolina", "Uber", "Taxi", "Autobús", "Metro",
            "Estacionamiento", "Peaje", "Mantenimiento auto"
        ),
        "Entretenimiento" to listOf(
            "Cine", "Concierto", "Streaming", "Videojuegos",
            "Libros", "Revista", "Salida nocturna", "Teatro"
        ),
        "Salud" to listOf(
            "Farmacia", "Doctor", "Dentista", "Laboratorio",
            "Seguro médico", "Vitaminas", "Gimnasio", "Terapia"
        ),
        "Educación" to listOf(
            "Curso online", "Libros", "Universidad", "Certificación",
            "Seminario", "Material escolar", "Clases particulares", "Software"
        ),
        "Compras" to listOf(
            "Ropa", "Zapatos", "Electrónicos", "Hogar",
            "Regalos", "Accesorios", "Perfume", "Decoración"
        ),
        "Servicios" to listOf(
            "Internet", "Teléfono", "Electricidad", "Agua",
            "Gas", "Limpieza", "Reparaciones", "Suscripciones"
        ),
        "Trabajo" to listOf(
            "Salario", "Freelance", "Bonificación", "Comisión",
            "Proyecto", "Consultoría", "Venta", "Inversión"
        ),
        "Hogar" to listOf(
            "Alquiler", "Hipoteca", "Muebles", "Electrodomésticos",
            "Jardinería", "Seguridad", "Mantenimiento", "Decoración"
        ),
        "Otros" to listOf(
            "Varios", "Imprevisto", "Donación", "Regalo",
            "Multa", "Impuestos", "Seguro", "Ahorro"
        )
    )

    suspend fun initializeSampleData(database: AppDatabase) = withContext(Dispatchers.IO) {
        // Check if data already exists
        val existingCategories = database.categoryDao().getCategoriesList()
        val existingPaymentMethods = database.paymentMethodDao().getAll()
        val existingTransactions = database.transactionDao().getAll()

        // Only initialize if database is empty
        if (existingCategories.isEmpty() && existingPaymentMethods.isEmpty() && existingTransactions.isEmpty()) { // Changed to .isEmpty()

            // Insert default categories
            val categoryIds = mutableMapOf<String, Int>()
            defaultCategories.forEach { category ->
                val id = database.categoryDao().insert(category).toInt()
                categoryIds[category.name] = id
            }

            // Insert default payment methods
            val paymentMethodIds = mutableListOf<Int>()
            defaultPaymentMethods.forEach { paymentMethod ->
                val id = database.paymentMethodDao().insert(paymentMethod).toInt()
                paymentMethodIds.add(id)
            }

            // Generate sample transactions for the last 3 months
            generateSampleTransactions(database, categoryIds, paymentMethodIds)
        }
    }

    private suspend fun generateSampleTransactions(
        database: AppDatabase,
        categoryIds: Map<String, Int>,
        paymentMethodIds: List<Int>
    ) {
        val calendar = Calendar.getInstance()
        val currentTime = calendar.timeInMillis

        // Generate transactions for the last 90 days
        val transactions = mutableListOf<Transaction>()

        for (dayOffset in 0..89) {
            calendar.timeInMillis = currentTime - (dayOffset * 24 * 60 * 60 * 1000L)

            // Generate 1-4 transactions per day (more realistic)
            val transactionsPerDay = Random.nextInt(1, 5)

            repeat(transactionsPerDay) {
                val isIncome = Random.nextDouble() < 0.2 // 20% chance of income

                if (isIncome) {
                    // Generate income transaction
                    val workCategoryId = categoryIds["Trabajo"] ?: 1
                    val amount = Random.nextDouble(500.0, 3000.0)
                    val descriptions = sampleDescriptions["Trabajo"] ?: listOf("Ingreso")

                    transactions.add(
                        Transaction(
                            amount = amount,
                            type = "Ingreso",
                            categoryId = workCategoryId,
                            description = descriptions.random(),
                            date = calendar.timeInMillis - Random.nextLong(0, 24 * 60 * 60 * 1000L),
                            paymentMethodId = paymentMethodIds.randomOrNull()
                        )
                    )
                } else {
                    // Generate expense transaction
                    val categoryName = defaultCategories.filter { it.name != "Trabajo" }.random().name
                    val categoryId = categoryIds[categoryName] ?: 1
                    val descriptions = sampleDescriptions[categoryName] ?: listOf("Gasto")

                    // Different amount ranges based on category
                    val amount = when (categoryName) {
                        "Alimentación" -> Random.nextDouble(10.0, 150.0)
                        "Transporte" -> Random.nextDouble(5.0, 100.0)
                        "Entretenimiento" -> Random.nextDouble(15.0, 200.0)
                        "Salud" -> Random.nextDouble(20.0, 300.0)
                        "Educación" -> Random.nextDouble(50.0, 500.0)
                        "Compras" -> Random.nextDouble(25.0, 400.0)
                        "Servicios" -> Random.nextDouble(30.0, 250.0)
                        "Hogar" -> Random.nextDouble(100.0, 800.0)
                        else -> Random.nextDouble(10.0, 100.0)
                    }

                    transactions.add(
                        Transaction(
                            amount = amount,
                            type = "Gasto",
                            categoryId = categoryId,
                            description = descriptions.random(),
                            date = calendar.timeInMillis - Random.nextLong(0, 24 * 60 * 60 * 1000L),
                            paymentMethodId = paymentMethodIds.randomOrNull()
                        )
                    )
                }
            }
        }

        // Insert all transactions
        transactions.forEach { transaction ->
            database.transactionDao().insert(transaction)
        }
    }

    // Budget template category helper removed (templates UI removed)
}
