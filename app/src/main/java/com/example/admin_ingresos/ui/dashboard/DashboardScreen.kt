package com.example.admin_ingresos.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add

@Composable
fun DashboardScreen(onAddTransaction: () -> Unit, onViewHistory: () -> Unit, onViewReports: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.bg_slate_50))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
        // Balance del mes
        Text(
            text = "Balance del mes",
            style = MaterialTheme.typography.titleLarge,
            color = colorResource(id = R.color.text_black)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ...existing code...
        }
    }
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { com.example.admin_ingresos.AppDatabaseProvider.getDatabase(context) }
    val viewModel: DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(com.example.admin_ingresos.data.TransactionRepository(db)) as T
        }
    })
    val transactions by viewModel.transactions.collectAsState(initial = emptyList())
    LaunchedEffect(Unit) { viewModel.loadTransactions() }

    val ingresos = transactions.filter { it.type == "Ingreso" }.sumOf { it.amount }
    val gastos = transactions.filter { it.type == "Gasto" }.sumOf { it.amount }
    val balance = ingresos - gastos
    val recientes = transactions.take(5)
    val categorias by produceState(initialValue = emptyList<com.example.admin_ingresos.data.Category>(), db) {
        value = db.categoryDao().getAll()
    }
    fun getCategoryName(catId: Int?): String {
        return categorias.find { it.id == catId }?.name ?: "Sin categoría"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.bg_slate_50))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Balance del mes: $balance",
                style = MaterialTheme.typography.titleLarge,
                color = colorResource(id = R.color.text_black)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardCard(title = "Ingresos", amount = "$ingresos", color = colorResource(id = R.color.green_600))
                DashboardCard(title = "Gastos", amount = "$gastos", color = colorResource(id = R.color.red_500))
            }
            Spacer(modifier = Modifier.height(24.dp))
            // Gráfico de dona de gastos por categoría (MPAndroidChart)
            val gastosPorCategoria: Map<String, Double> = transactions.filter { it.type == "Gasto" }
                .groupBy { getCategoryName(it.categoryId) }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
            com.example.admin_ingresos.ui.dashboard.ExpensePieChart(
                context = context,
                expensesByCategory = gastosPorCategoria,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            // Lista de transacciones recientes (datos reales)
            Text(
                text = "Transacciones recientes",
                style = MaterialTheme.typography.titleLarge,
                color = colorResource(id = R.color.text_black)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recientes.forEach { tx ->
                    RecentTransactionItem(
                        tx.description,
                        tx.type,
                        if (tx.type == "Ingreso") tx.amount else -tx.amount,
                        getCategoryName(tx.categoryId),
                        if (tx.type == "Ingreso") colorResource(id = R.color.green_600)
                        else colorResource(id = R.color.red_500)
                    )
                }
            }
        }
        // Botón de acción flotante (FAB)
        androidx.compose.material3.FloatingActionButton(
            onClick = { onAddTransaction() },
            containerColor = colorResource(id = R.color.indigo_500),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Add,
                contentDescription = "Añadir transacción"
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Lista de transacciones recientes (placeholder funcional)
        Text(
            text = "Transacciones recientes",
            style = MaterialTheme.typography.titleLarge,
            color = colorResource(id = R.color.text_black)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RecentTransactionItem("Salario", "Ingreso", 2500.0, "Trabajo", colorResource(id = R.color.green_600))
            RecentTransactionItem("Comida", "Gasto", -120.0, "Restaurante", colorResource(id = R.color.red_500))
            RecentTransactionItem("Transporte", "Gasto", -50.0, "Taxi", colorResource(id = R.color.orange_500))
            RecentTransactionItem("Freelance", "Ingreso", 400.0, "Proyecto", colorResource(id = R.color.green_600))
            RecentTransactionItem("Ocio", "Gasto", -80.0, "Cine", colorResource(id = R.color.red_500))
        }
        }
        // Botón de acción flotante (FAB)
        androidx.compose.material3.FloatingActionButton(
            onClick = { onAddTransaction() },
            containerColor = colorResource(id = R.color.indigo_500),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Add,
                contentDescription = "Añadir transacción"
            )
        }
        // Botón para ver historial
        Button(
            onClick = { onViewHistory() },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.text_gray_800))
        ) {
            Text("Ver historial", color = Color.White)
        }
        // Botón para ver reportes
        Button(
            onClick = { onViewReports() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.indigo_500))
        ) {
            Text("Ver reportes", color = Color.White)
        }
    }
}

@Composable
fun RecentTransactionItem(
    category: String,
    type: String,
    amount: Double,
    description: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.card_bg), shape = RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono minimalista
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = category.take(1), color = color, style = MaterialTheme.typography.titleLarge)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = category, color = colorResource(id = R.color.text_gray_800), style = MaterialTheme.typography.bodyLarge)
            Text(text = description, color = colorResource(id = R.color.text_gray_500), style = MaterialTheme.typography.bodySmall)
        }
        Text(
            text = if (amount > 0) "+$amount" else "$amount",
            color = color,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun DashboardCard(title: String, amount: String, color: Color) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .height(110.dp),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.card_bg))
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, color = color)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = amount, style = MaterialTheme.typography.titleLarge, color = color)
        }
    }
}
