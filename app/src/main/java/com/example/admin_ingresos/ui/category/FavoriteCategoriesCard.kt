package com.example.admin_ingresos.ui.category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin_ingresos.data.Category
import com.example.admin_ingresos.ui.components.CashFlowCard
import com.example.admin_ingresos.ui.theme.*
import java.text.NumberFormat
import java.util.*

data class CategoryUsageData(
    val category: Category,
    val usageCount: Int,
    val totalAmount: Double,
    val lastUsed: Long
)

@Composable
fun FavoriteCategoriesCard(
    favoriteCategories: List<CategoryUsageData>,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    CashFlowCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Categorías Favoritas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                TextButton(
                    onClick = { /* Navigate to all categories */ }
                ) {
                    Text(
                        text = "Ver todas",
                        fontSize = 12.sp
                    )
                }
            }
            
            if (favoriteCategories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sin categorías frecuentes",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Usa las categorías para verlas aquí",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(favoriteCategories) { categoryData ->
                        FavoriteCategoryItem(
                            categoryData = categoryData,
                            onClick = { onCategoryClick(categoryData.category) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteCategoryItem(
    categoryData: CategoryUsageData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    
    Card(
        modifier = modifier.width(140.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = getCategoryColor(categoryData.category.name).copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = getCategoryColor(categoryData.category.name).copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono de categoría
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = getCategoryColor(categoryData.category.name).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(categoryData.category.name),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = getCategoryColor(categoryData.category.name)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Nombre de categoría
            Text(
                text = categoryData.category.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Estadísticas
            Text(
                text = "${categoryData.usageCount} usos",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (categoryData.totalAmount > 0) {
                Text(
                    text = formatter.format(categoryData.totalAmount),
                    fontSize = 10.sp,
                    color = if (categoryData.category.name.contains("ingreso", ignoreCase = true)) IncomeGreen else ExpenseRed,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CategoryStatsCard(
    topCategories: List<CategoryUsageData>,
    modifier: Modifier = Modifier
) {
    CashFlowCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Categorías Más Usadas",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            if (topCategories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin datos de categorías",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    topCategories.take(5).forEach { categoryData ->
                        CategoryStatItem(categoryData = categoryData)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryStatItem(
    categoryData: CategoryUsageData,
    modifier: Modifier = Modifier
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = getCategoryColor(categoryData.category.name).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(categoryData.category.name),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = getCategoryColor(categoryData.category.name)
                )
            }
            
            Column {
                Text(
                    text = categoryData.category.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${categoryData.usageCount} transacciones",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (categoryData.totalAmount > 0) {
            Text(
                text = formatter.format(categoryData.totalAmount),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (categoryData.category.name.contains("ingreso", ignoreCase = true)) IncomeGreen else ExpenseRed
            )
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
