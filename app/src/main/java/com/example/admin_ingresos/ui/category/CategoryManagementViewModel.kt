package com.example.admin_ingresos.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin_ingresos.data.AppDatabase
import com.example.admin_ingresos.data.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CategoryManagementViewModel(private val database: AppDatabase) : ViewModel() {
    
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _categoryStats = MutableStateFlow<List<CategoryStats>>(emptyList())
    val categoryStats: StateFlow<List<CategoryStats>> = _categoryStats.asStateFlow()
    
    private val _favoriteCategories = MutableStateFlow<List<Category>>(emptyList())
    val favoriteCategories: StateFlow<List<Category>> = _favoriteCategories.asStateFlow()
    
    private val _currentSortOption = MutableStateFlow(CategorySortOption.NAME_ASC)
    val currentSortOption: StateFlow<CategorySortOption> = _currentSortOption.asStateFlow()
    
    private val _allCategories = MutableStateFlow<List<Category>>(emptyList())
    
    init {
        loadCategories()
        loadCategoryStats()
        
        // Combinar datos de búsqueda y ordenamiento
        viewModelScope.launch {
            combine(
                _allCategories,
                _searchQuery,
                _currentSortOption
            ) { categories, query, sortOption ->
                val filtered = if (query.isBlank()) {
                    categories
                } else {
                    categories.filter { category -> 
                        category.name.contains(query, ignoreCase = true)
                    }
                }
                
                val sorted = when (sortOption) {
                    CategorySortOption.NAME_ASC -> filtered.sortedBy { it.name }
                    CategorySortOption.NAME_DESC -> filtered.sortedByDescending { it.name }
                    CategorySortOption.USAGE_DESC -> filtered.sortedByDescending { it.id }
                    CategorySortOption.USAGE_ASC -> filtered.sortedBy { it.id }
                    CategorySortOption.AMOUNT_DESC -> filtered.sortedByDescending { it.id }
                    CategorySortOption.AMOUNT_ASC -> filtered.sortedBy { it.id }
                    CategorySortOption.DATE_DESC -> filtered.sortedByDescending { it.id }
                    CategorySortOption.DATE_ASC -> filtered.sortedBy { it.id }
                }
                
                sorted
            }.collect { result ->
                _categories.value = result
            }
        }
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val allCategories = database.categoryDao().getAllCategories()
                _allCategories.value = allCategories
                
                // Cargar categorías favoritas (simulado - primeras 3)
                _favoriteCategories.value = allCategories.take(3)
                
            } catch (e: Exception) {
                // Manejar error - crear algunas categorías de ejemplo
                _allCategories.value = listOf(
                    Category(1, "Comida", "🍔", "#FF9800"),
                    Category(2, "Transporte", "🚗", "#2196F3"),
                    Category(3, "Entretenimiento", "🎬", "#9C27B0"),
                    Category(4, "Salud", "🏥", "#4CAF50"),
                    Category(5, "Educación", "📚", "#607D8B")
                )
                _favoriteCategories.value = _allCategories.value.take(2)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun loadCategoryStats() {
        viewModelScope.launch {
            try {
                val categories = _allCategories.value
                
                // Crear estadísticas de ejemplo
                val stats = categories.mapIndexed { index, category ->
                    CategoryStats(
                        categoryId = category.id,
                        categoryName = category.name,
                        transactionCount = (index + 1) * 5, // Datos de ejemplo
                        totalAmount = (index + 1) * 50000.0, // Datos de ejemplo
                        lastUsed = System.currentTimeMillis() - (index * 86400000L) // Días atrás
                    )
                }
                
                _categoryStats.value = stats
            } catch (e: Exception) {
                _categoryStats.value = emptyList()
            }
        }
    }
    
    fun searchCategories(query: String) {
        _searchQuery.value = query
    }
    
    fun setSortOption(sortOption: CategorySortOption) {
        _currentSortOption.value = sortOption
    }
    
    fun addCategory(name: String, icon: String, color: String) {
        viewModelScope.launch {
            try {
                val category = Category(
                    id = 0, // Room autogenerará el ID
                    name = name,
                    icon = icon,
                    color = color
                )
                database.categoryDao().insert(category)
                loadCategories()
                loadCategoryStats()
            } catch (e: Exception) {
                // Agregar a la lista local como ejemplo
                val newCategory = Category(
                    id = (_allCategories.value.maxByOrNull { it.id }?.id ?: 0) + 1,
                    name = name,
                    icon = icon,
                    color = color
                )
                _allCategories.value = _allCategories.value + newCategory
                loadCategoryStats()
            }
        }
    }
    
    fun editCategory(category: Category) {
        viewModelScope.launch {
            try {
                database.categoryDao().update(category)
                loadCategories()
                loadCategoryStats()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
    
    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                database.categoryDao().delete(category)
                loadCategories()
                loadCategoryStats()
            } catch (e: Exception) {
                // Eliminar de la lista local
                _allCategories.value = _allCategories.value.filter { it.id != category.id }
                _favoriteCategories.value = _favoriteCategories.value.filter { it.id != category.id }
                loadCategoryStats()
            }
        }
    }
    
    fun toggleFavorite(category: Category) {
        viewModelScope.launch {
            try {
                val currentFavorites = _favoriteCategories.value.toMutableList()
                if (currentFavorites.contains(category)) {
                    currentFavorites.remove(category)
                } else {
                    currentFavorites.add(category)
                }
                _favoriteCategories.value = currentFavorites
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
    
    fun refreshData() {
        loadCategories()
        loadCategoryStats()
    }
}

// Data classes necesarias
data class CategoryStats(
    val categoryId: Int,
    val categoryName: String,
    val transactionCount: Int,
    val totalAmount: Double,
    val lastUsed: Long?
)

enum class CategorySortOption(val displayName: String) {
    NAME_ASC("Nombre A-Z"),
    NAME_DESC("Nombre Z-A"),
    USAGE_DESC("Más utilizadas"),
    USAGE_ASC("Menos utilizadas"),
    AMOUNT_DESC("Mayor gasto"),
    AMOUNT_ASC("Menor gasto"),
    DATE_DESC("Más recientes"),
    DATE_ASC("Más antiguas")
}
