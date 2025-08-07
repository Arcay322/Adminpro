package com.example.admin_ingresos.ui.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin_ingresos.data.Category
import com.example.admin_ingresos.ui.components.CashFlowCard
import com.example.admin_ingresos.ui.theme.*
import java.text.NumberFormat
import java.util.*

@Composable
fun CategoryAnalyticsCard(
    category: Category,
    totalAmount: Double,
    transactionCount: Int,
    modifier: Modifier = Modifier
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    
    CashFlowCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.size(48.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = getCategoryColor(category.name).copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(category.name),
                            contentDescription = null,
                            tint = getCategoryColor(category.name),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$transactionCount transacciones",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatter.format(totalAmount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (category.name.contains("ingreso", ignoreCase = true)) IncomeGreen else ExpenseRed
                    )
                }
            }
        }
    }
}

private fun getCategoryColor(categoryName: String): Color {
    return when (categoryName.lowercase()) {
        "comida", "alimentación", "supermercado", "restaurant" -> Color(0xFFFF9800)
        "transporte", "taxi", "uber", "gasolina" -> Color(0xFF2196F3)
        "entretenimiento", "ocio", "cine", "diversión" -> Color(0xFF9C27B0)
        "salud", "médico", "farmacia", "hospital" -> Color(0xFF4CAF50)
        "educación", "estudio", "universidad", "cursos" -> Color(0xFF607D8B)
        "trabajo", "salario", "sueldo" -> Color(0xFF795548)
        "hogar", "casa", "servicios", "luz", "agua" -> Color(0xFF00BCD4)
        "ropa", "vestimenta", "shopping" -> Color(0xFFE91E63)
        "tecnología", "software", "apps" -> Color(0xFF3F51B5)
        "viajes", "vacaciones", "turismo" -> Color(0xFF009688)
        else -> Color(0xFF757575)
    }
}

private fun getCategoryIcon(categoryName: String): ImageVector {
    return when (categoryName.lowercase()) {
        "comida", "alimentación", "supermercado", "restaurant" -> Icons.Default.Restaurant
        "transporte", "taxi", "uber", "gasolina" -> Icons.Default.DirectionsCar
        "entretenimiento", "ocio", "cine", "diversión" -> Icons.Default.Movie
        "salud", "médico", "farmacia", "hospital" -> Icons.Default.LocalHospital
        "educación", "estudio", "universidad", "cursos" -> Icons.Default.School
        "trabajo", "salario", "sueldo" -> Icons.Default.Work
        "hogar", "casa", "servicios", "luz", "agua" -> Icons.Default.Home
        "ropa", "vestimenta", "shopping" -> Icons.Default.Checkroom
        "tecnología", "software", "apps" -> Icons.Default.Computer
        "viajes", "vacaciones", "turismo" -> Icons.Default.Flight
        else -> Icons.Default.Category
    }
}
