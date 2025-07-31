package com.example.admin_ingresos.ui.reports

import android.content.Context
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

@Composable
fun IncomeExpenseBarChart(
    context: Context,
    ingresos: Double,
    gastos: Double,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            BarChart(ctx).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                description.isEnabled = false
                legend.isEnabled = true
                axisRight.isEnabled = false
                axisLeft.textColor = AndroidColor.BLACK
                xAxis.textColor = AndroidColor.BLACK
                xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            }
        },
        update = { chart ->
            val entries = listOf(
                BarEntry(0f, ingresos.toFloat()),
                BarEntry(1f, gastos.toFloat())
            )
            val dataSet = BarDataSet(entries, "Ingresos vs Gastos")
            dataSet.colors = listOf(AndroidColor.rgb(76, 175, 80), AndroidColor.rgb(244, 67, 54))
            dataSet.valueTextColor = AndroidColor.BLACK
            dataSet.valueTextSize = 14f
            val barData = BarData(dataSet)
            chart.data = barData
            chart.xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(listOf("Ingresos", "Gastos"))
            chart.invalidate()
        }
    )
}
