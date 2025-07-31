package com.example.admin_ingresos.ui.dashboard

import android.content.Context
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

@Composable
fun ExpensePieChart(
    context: Context,
    expensesByCategory: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PieChart(ctx).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                description.isEnabled = false
                isDrawHoleEnabled = true
                setHoleColor(AndroidColor.WHITE)
                setUsePercentValues(true)
                legend.isEnabled = true
            }
        },
        update = { chart ->
            val entries = expensesByCategory.map { (catId, total) ->
                PieEntry(total.toFloat(), "Cat. $catId")
            }
            val dataSet = PieDataSet(entries, "Gastos por categoría")
            dataSet.colors = listOf(
                AndroidColor.rgb(244, 67, 54), // rojo
                AndroidColor.rgb(255, 152, 0), // naranja
                AndroidColor.rgb(76, 175, 80), // verde
                AndroidColor.rgb(33, 150, 243), // azul
                AndroidColor.rgb(156, 39, 176) // morado
            )
            dataSet.valueTextColor = AndroidColor.BLACK
            dataSet.valueTextSize = 14f
            val pieData = PieData(dataSet)
            chart.data = pieData
            chart.invalidate()
        }
    )
}
