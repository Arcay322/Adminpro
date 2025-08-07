package com.example.admin_ingresos.ui.category

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin_ingresos.data.Category
import com.example.admin_ingresos.ui.components.CashFlowCard
import com.example.admin_ingresos.ui.theme.*

@Composable
fun EnhancedCategorySelector(
    categories: List<Category>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    CashFlowCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Seleccionar Categoría",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            if (categories.isEmpty()) {
                Text(
                    text = "No hay categorías disponibles",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        CategoryChip(
                            category = category,
                            selected = selectedCategoryId == category.id,
                            onClick = { onCategorySelected(category.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { 
            Text(
                text = category.name,
                fontSize = 12.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = getCategoryIcon(category.name),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        modifier = modifier
    )
}

@Composable
fun CategoryGrid(
    categories: List<Category>,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    CashFlowCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Todas las Categorías",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            if (categories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sin categorías",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Grid de categorías (simplificado para LazyRow)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories) { category ->
                        Card(
                            modifier = Modifier
                                .width(120.dp)
                                .height(80.dp),
                            onClick = { onCategoryClick(category) },
                            colors = CardDefaults.cardColors(
                                containerColor = getCategoryColor(category.name).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(category.name),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = getCategoryColor(category.name)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = category.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }
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
