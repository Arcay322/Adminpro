package com.example.admin_ingresos.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random
import java.util.*

/**
 * TestDataSeeder
 *
 * Usage: call `TestDataSeeder.seedReportTestData(database)` from a coroutine scope (for example
 * viewModelScope.launch { TestDataSeeder.seedReportTestData(db) })
 *
 * You can temporarily add a debug button in `ReportsScreen` that calls this function to populate
 * the database during development.
 */
object TestDataSeeder {

    private val debugCategories = listOf(
        Category(name = "Alimentación", icon = "UtensilsCrossed", color = "#FF6B6B", isFavorite = true),
        Category(name = "Transporte", icon = "Car", color = "#4ECDC4", isFavorite = true),
        Category(name = "Entretenimiento", icon = "Gamepad2", color = "#45B7D1"),
        Category(name = "Salud", icon = "Heart", color = "#96CEB4"),
        Category(name = "Educación", icon = "BookOpen", color = "#FFEAA7"),
        Category(name = "Compras", icon = "ShoppingCart", color = "#DDA0DD", isFavorite = true),
        Category(name = "Servicios", icon = "Settings", color = "#98D8C8"),
        Category(name = "Trabajo", icon = "Briefcase", color = "#F7DC6F", isFavorite = true),
        Category(name = "Hogar", icon = "Home", color = "#BB8FCE"),
        Category(name = "Otros", icon = "Package", color = "#85C1E9")
    )

    private val debugPaymentMethods = listOf(
        PaymentMethod(name = "Efectivo", icon = "💵"),
        PaymentMethod(name = "Tarjeta Débito", icon = "💳"),
        PaymentMethod(name = "Tarjeta Crédito", icon = "💳"),
        PaymentMethod(name = "Transferencia", icon = "🏦"),
        PaymentMethod(name = "PayPal", icon = "📱")
    )

    // Local sample descriptions to avoid depending on private members elsewhere
    private val sampleDescriptions = mapOf(
        "Alimentación" to listOf("Supermercado", "Restaurante", "Comida rápida", "Cafetería", "Panadería", "Mercado", "Delivery"),
        "Transporte" to listOf("Gasolina", "Uber", "Taxi", "Autobús", "Metro", "Estacionamiento", "Peaje"),
        "Entretenimiento" to listOf("Cine", "Concierto", "Streaming", "Videojuegos", "Libros", "Teatro"),
        "Salud" to listOf("Farmacia", "Doctor", "Dentista", "Gimnasio", "Terapia"),
        "Educación" to listOf("Curso online", "Libros", "Universidad", "Certificación", "Seminario"),
        "Compras" to listOf("Ropa", "Electrónicos", "Hogar", "Regalos", "Accesorios"),
        "Servicios" to listOf("Internet", "Electricidad", "Agua", "Teléfono", "Limpieza"),
        "Trabajo" to listOf("Salario", "Freelance", "Bonificación", "Comisión"),
        "Hogar" to listOf("Alquiler", "Hipoteca", "Muebles", "Electrodomésticos"),
        "Otros" to listOf("Varios", "Imprevisto", "Donación", "Multa")
    )

    suspend fun seedReportTestData(database: AppDatabase) = withContext(Dispatchers.IO) {
        val existingTx = database.transactionDao().getAll()
        val existingCategories = database.categoryDao().getCategoriesList()
        val existingPayments = database.paymentMethodDao().getAll()

        val categoryIds = mutableMapOf<String, Int>()
        if (existingCategories.isEmpty()) {
            debugCategories.forEach { cat ->
                val id = database.categoryDao().insert(cat).toInt()
                categoryIds[cat.name] = id
            }
        } else {
            existingCategories.forEach { c -> categoryIds[c.name] = c.id }
            debugCategories.forEach { cat ->
                if (categoryIds[cat.name] == null) {
                    val id = database.categoryDao().insert(cat).toInt()
                    categoryIds[cat.name] = id
                }
            }
        }

        val paymentIds = mutableListOf<Int>()
        if (existingPayments.isEmpty()) {
            debugPaymentMethods.forEach { pm ->
                val id = database.paymentMethodDao().insert(pm).toInt()
                paymentIds.add(id)
            }
        } else {
            existingPayments.forEach { paymentIds.add(it.id) }
            debugPaymentMethods.forEach { pm ->
                if (existingPayments.none { it.name == pm.name }) {
                    val id = database.paymentMethodDao().insert(pm).toInt()
                    paymentIds.add(id)
                }
            }
        }

        val targetCount = if (existingTx.isEmpty()) 400 else 120

        val now = Calendar.getInstance()
        val startCal = Calendar.getInstance()
        startCal.add(Calendar.MONTH, -6)
        val startTime = startCal.timeInMillis
        val endTime = now.timeInMillis

        val allCategoryNames = categoryIds.keys.toList()
        val transactions = mutableListOf<Transaction>()

        repeat(targetCount) {
            val isIncome = Random.nextDouble() < 0.18
            val categoryName = if (isIncome) "Trabajo" else allCategoryNames.filter { it != "Trabajo" }.random()
            val categoryId = categoryIds[categoryName] ?: categoryIds.values.first()

            val date = randomTimeBetween(startTime, endTime)

            val amount = if (isIncome) {
                (Random.nextDouble(300.0, 5000.0) * 100).toInt() / 100.0
            } else {
                when (categoryName) {
                    "Alimentación" -> (Random.nextDouble(5.0, 200.0) * 100).toInt() / 100.0
                    "Transporte" -> (Random.nextDouble(2.0, 150.0) * 100).toInt() / 100.0
                    "Entretenimiento" -> (Random.nextDouble(1.0, 300.0) * 100).toInt() / 100.0
                    "Salud" -> (Random.nextDouble(10.0, 600.0) * 100).toInt() / 100.0
                    "Educación" -> (Random.nextDouble(20.0, 1000.0) * 100).toInt() / 100.0
                    "Compras" -> (Random.nextDouble(5.0, 900.0) * 100).toInt() / 100.0
                    "Servicios" -> (Random.nextDouble(15.0, 500.0) * 100).toInt() / 100.0
                    "Hogar" -> (Random.nextDouble(20.0, 2000.0) * 100).toInt() / 100.0
                    else -> (Random.nextDouble(1.0, 250.0) * 100).toInt() / 100.0
                }
            }

            val descList = sampleDescriptions[categoryName] ?: listOf("Pago", "Compra", "Servicio")
            val desc = descList.random()

            val paymentId = if (paymentIds.isNotEmpty()) paymentIds.random() else null

            transactions.add(
                Transaction(
                    amount = amount,
                    type = if (isIncome) "Ingreso" else "Gasto",
                    categoryId = categoryId,
                    description = desc,
                    date = date,
                    paymentMethodId = paymentId
                )
            )
        }

        transactions.forEach { database.transactionDao().insert(it) }
    }

    private fun randomTimeBetween(start: Long, end: Long): Long {
        val lower = minOf(start, end)
        val upper = maxOf(start, end)
        val range = upper - lower
        if (range <= 0) return lower
        return lower + (Random.nextLong(range))
    }
}
