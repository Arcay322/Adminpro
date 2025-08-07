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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.admin_ingresos.data.Category
import com.example.admin_ingresos.ui.components.CashFlowCard
import com.example.admin_ingresos.ui.theme.*
import com.example.admin_ingresos.ui.icons.LucideIconMapper

data class CategoryOption(
    val id: Int,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val type: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernCategoryDialog(
    onDismiss: () -> Unit,
    onCategorySelected: (String, String) -> Unit, // name, type
    existingCategories: List<Category> = emptyList(),
    selectedType: String = "expense"
) {
    var categoryName by remember { mutableStateOf("") }
    var categoryType by remember { mutableStateOf(selectedType) }
    var selectedIcon by remember { mutableStateOf<ImageVector>(Icons.Default.Category) }
    
    val predefinedCategories = remember {
        listOf(
            CategoryOption(1, "Comida", Icons.Default.Restaurant, Color(0xFFFF9800), "expense"),
            CategoryOption(2, "Transporte", Icons.Default.DirectionsCar, Color(0xFF2196F3), "expense"),
            CategoryOption(3, "Entretenimiento", Icons.Default.Movie, Color(0xFF9C27B0), "expense"),
            CategoryOption(4, "Salud", Icons.Default.LocalHospital, Color(0xFF4CAF50), "expense"),
            CategoryOption(5, "Educación", Icons.Default.School, Color(0xFF607D8B), "expense"),
            CategoryOption(6, "Hogar", Icons.Default.Home, Color(0xFF00BCD4), "expense"),
            CategoryOption(7, "Ropa", Icons.Default.Checkroom, Color(0xFFE91E63), "expense"),
            CategoryOption(8, "Tecnología", Icons.Default.Computer, Color(0xFF3F51B5), "expense"),
            CategoryOption(9, "Viajes", Icons.Default.Flight, Color(0xFF009688), "expense"),
            CategoryOption(10, "Gimnasio", Icons.Default.FitnessCenter, Color(0xFFFF5722), "expense"),
            CategoryOption(11, "Salario", Icons.Default.Work, Color(0xFF4CAF50), "income"),
            CategoryOption(12, "Inversiones", Icons.Default.TrendingUp, Color(0xFF2196F3), "income"),
            CategoryOption(13, "Ventas", Icons.Default.Sell, Color(0xFF9C27B0), "income"),
            CategoryOption(14, "Bonos", Icons.Default.Stars, Color(0xFFFF9800), "income")
        )
    }
    
    val filteredCategories = predefinedCategories.filter { it.type == categoryType }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nueva Categoría",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Type Selector
                Text(
                    text = "Tipo",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = categoryType == "expense",
                        onClick = { categoryType = "expense" },
                        label = { Text("Gasto") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.TrendingDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    
                    FilterChip(
                        selected = categoryType == "income",
                        onClick = { categoryType = "income" },
                        label = { Text("Ingreso") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Name Input
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Nombre de la categoría") },
                    placeholder = { Text("Ej: Comida, Transporte") },
                    leadingIcon = {
                        Icon(
                            imageVector = selectedIcon,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Category Options
                Text(
                    text = "Selecciona una opción o personaliza",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredCategories) { category ->
                        CategoryOptionCard(
                            categoryOption = category,
                            isSelected = categoryName == category.name,
                            onClick = {
                                categoryName = category.name
                                selectedIcon = category.icon
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            if (categoryName.isNotBlank()) {
                                onCategorySelected(categoryName.trim(), categoryType)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = categoryName.isNotBlank()
                    ) {
                        Text("Crear")
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryOptionCard(
    categoryOption: CategoryOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.size(80.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                categoryOption.color.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, categoryOption.color)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        },
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
                imageVector = categoryOption.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) categoryOption.color else MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = categoryOption.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) categoryOption.color else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CategoryIconSelector(
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val availableIcons = LucideIconMapper.getAvailableCategoryIcons()
    
    CashFlowCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Seleccionar Ícono",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableIcons) { iconOption ->
                    IconButton(
                        onClick = { onIconSelected(iconOption.icon) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (selectedIcon == iconOption.icon) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                } else {
                                    Color.Transparent
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = LucideIconMapper.getIconFromCategoryName(iconOption.icon),
                            contentDescription = iconOption.name,
                            tint = if (selectedIcon == iconOption.icon) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
    }
}
