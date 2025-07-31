package com.example.admin_ingresos.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.admin_ingresos.R

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
@Composable
fun ReportsScreen() {
    val context = LocalContext.current
    val db = remember { com.example.admin_ingresos.AppDatabaseProvider.getDatabase(context) }
    val viewModel: ReportsViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ReportsViewModel(db) as T
        }
    })
    val transactions by viewModel.transactions.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadTransactions() }

    val ingresos = transactions.filter { it.type == "Ingreso" }.sumOf { it.amount }
    val gastos = transactions.filter { it.type == "Gasto" }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.bg_slate_50))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Reportes y Análisis", style = MaterialTheme.typography.titleLarge)
        // Gráfico de barras real (MPAndroidChart)
        com.example.admin_ingresos.ui.reports.IncomeExpenseBarChart(
            context = context,
            ingresos = ingresos,
            gastos = gastos,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        // Análisis por categoría con nombres reales
        val categorias by produceState(initialValue = emptyList<com.example.admin_ingresos.data.Category>(), db) {
            value = db.categoryDao().getAll()
        }
        fun getCategoryName(catId: Int?): String {
            return categorias.find { it.id == catId }?.name ?: "Sin categoría"
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                val categoriasMap = transactions.groupBy { it.categoryId }
                categoriasMap.forEach { (catId, transList) ->
                    val total = transList.sumOf { it.amount }
                    Text("${getCategoryName(catId)}: $total", color = colorResource(id = R.color.text_gray_800))
                }
            }
        }
    }
}
