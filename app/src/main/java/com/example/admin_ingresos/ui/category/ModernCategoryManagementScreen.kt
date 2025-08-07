package com.example.admin_ingresos.ui.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin_ingresos.data.Category
import com.example.admin_ingresos.ui.components.CashFlowCard
import com.example.admin_ingresos.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernCategoryManagementScreen(
    categories: List<Category>,
    onAddCategory: (String, String) -> Unit,
    onEditCategory: (Category, String) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    onBackClick: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Categorías") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar categoría")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (categories.isEmpty()) {
                item {
                    EnhancedEmptyCategoriesState(
                        onAddCategory = { showAddDialog = true }
                    )
                }
            } else {
                items(categories) { category ->
                    EnhancedCategoryItem(
                        category = category,
                        onEdit = { categoryToEdit = category },
                        onDelete = { onDeleteCategory(category) }
                    )
                }
            }
        }
    }
    
    if (showAddDialog) {
        ModernCategoryDialog(
            onDismiss = { showAddDialog = false },
            onCategorySelected = { name, type ->
                onAddCategory(name, type)
                showAddDialog = false
            }
        )
    }
    
    categoryToEdit?.let { category ->
        ModernCategoryDialog(
            onDismiss = { categoryToEdit = null },
            onCategorySelected = { name, type ->
                onEditCategory(category, name)
                categoryToEdit = null
            },
            existingCategories = categories
        )
    }
}

@Composable
private fun EnhancedEmptyCategoriesState(
    onAddCategory: () -> Unit
) {
    CashFlowCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Sin categorías",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Agrega categorías para organizar mejor tus transacciones",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = onAddCategory) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar Categoría")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedCategoryItem(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    CashFlowCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getCategoryIcon(category.name),
                contentDescription = null,
                tint = getCategoryColor(category.name),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = category.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
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

private fun getCategoryColor(categoryName: String): androidx.compose.ui.graphics.Color {
    return when (categoryName.lowercase()) {
        "comida", "alimentación", "supermercado", "restaurant" -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        "transporte", "taxi", "uber", "gasolina" -> androidx.compose.ui.graphics.Color(0xFF2196F3)
        "entretenimiento", "ocio", "cine", "diversión" -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
        "salud", "médico", "farmacia", "hospital" -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        "educación", "estudio", "universidad", "cursos" -> androidx.compose.ui.graphics.Color(0xFF607D8B)
        "trabajo", "salario", "sueldo" -> androidx.compose.ui.graphics.Color(0xFF795548)
        "hogar", "casa", "servicios", "luz", "agua" -> androidx.compose.ui.graphics.Color(0xFF00BCD4)
        "ropa", "vestimenta", "shopping" -> androidx.compose.ui.graphics.Color(0xFFE91E63)
        "tecnología", "software", "apps" -> androidx.compose.ui.graphics.Color(0xFF3F51B5)
        "viajes", "vacaciones", "turismo" -> androidx.compose.ui.graphics.Color(0xFF009688)
        else -> androidx.compose.ui.graphics.Color(0xFF757575)
    }
}
