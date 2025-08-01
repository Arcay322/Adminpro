package com.example.admin_ingresos.ui.dashboard

import android.content.Context
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.data.Entry

@Composable
fun InteractiveExpensePieChart(
    context: Context,
    expensesByCategory: Map<String, Double>,
    onCategorySelected: (String, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<Pair<String, Double>?>(null) }
    
    Column(modifier = modifier) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            factory = { ctx ->
                PieChart(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    description.isEnabled = false
                    isDrawHoleEnabled = true
                    setHoleColor(AndroidColor.WHITE)
                    holeRadius = 40f
                    transparentCircleRadius = 45f
                    setUsePercentValues(true)
                    legend.isEnabled = false
                    setDrawEntryLabels(false)
                    
                    // Enable touch interactions
                    isHighlightPerTapEnabled = true
                    isRotationEnabled = true
                    setRotationAngle(0f)
                    
                    // Set up click listener
                    setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                        override fun onValueSelected(e: Entry?, h: Highlight?) {
                            if (e is PieEntry) {
                                val category = e.label
                                val value = e.value.toDouble()
                                selectedCategory = Pair(category, value)
                                onCategorySelected(category, value)
                            }
                        }
                        
                        override fun onNothingSelected() {
                            selectedCategory = null
                        }
                    })
                }
            },
            update = { chart ->
                val entries = expensesByCategory.map { (category, total) ->
                    PieEntry(total.toFloat(), category)
                }
                
                val dataSet = PieDataSet(entries, "Gastos por categoría").apply {
                    colors = listOf(
                        AndroidColor.rgb(244, 67, 54),   // Red
                        AndroidColor.rgb(255, 152, 0),   // Orange
                        AndroidColor.rgb(76, 175, 80),   // Green
                        AndroidColor.rgb(33, 150, 243),  // Blue
                        AndroidColor.rgb(156, 39, 176),  // Purple
                        AndroidColor.rgb(255, 193, 7),   // Amber
                        AndroidColor.rgb(96, 125, 139),  // Blue Grey
                        AndroidColor.rgb(121, 85, 72),   // Brown
                        AndroidColor.rgb(158, 158, 158), // Grey
                        AndroidColor.rgb(255, 87, 34)    // Deep Orange
                    )
                    valueTextColor = AndroidColor.BLACK
                    valueTextSize = 12f
                    sliceSpace = 2f
                    selectionShift = 8f
                }
                
                val pieData = PieData(dataSet)
                chart.data = pieData
                chart.invalidate()
            }
        )
        
        // Selected category details
        selectedCategory?.let { (category, amount) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Categoría Seleccionada",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$${String.format("%.2f", amount)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val percentage = (amount / expensesByCategory.values.sum()) * 100
                    Text(
                        text = "${String.format("%.1f", percentage)}% del total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}