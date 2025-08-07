package com.example.admin_ingresos.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin_ingresos.data.Category
import com.example.admin_ingresos.data.CategoryDao
import com.example.admin_ingresos.data.CategoryStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class CategoryViewModel(private val categoryDao: CategoryDao) : ViewModel() {
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    private val _categoriesWithStats = MutableStateFlow<List<CategoryStats>>(emptyList())
    val categoriesWithStats: StateFlow<List<CategoryStats>> = _categoriesWithStats.asStateFlow()
    
    private val _favoriteCategories = MutableStateFlow<List<CategoryStats>>(emptyList())
    val favoriteCategories: StateFlow<List<CategoryStats>> = _favoriteCategories.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _filteredCategories = MutableStateFlow<List<CategoryStats>>(emptyList())
    val filteredCategories: StateFlow<List<CategoryStats>> = _filteredCategories.asStateFlow()

    init {
        loadCategories()
        loadCategoriesWithStats()
        loadFavoriteCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _categories.value = categoryDao.getAllCategories()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadCategoriesWithStats() {
        viewModelScope.launch {
            try {
                val categories = categoryDao.getAllCategories()
                val stats = categories.map { category ->
                    val usageCount = categoryDao.getCategoryUsageCount(category.id)
                    val totalExpenses = categoryDao.getCategoryTotalExpenses(category.id) ?: 0.0
                    val totalIncome = categoryDao.getCategoryTotalIncome(category.id) ?: 0.0
                    val lastUsed = categoryDao.getCategoryLastUsed(category.id)
                    
                    CategoryStats(
                        id = category.id,
                        name = category.name,
                        icon = category.icon,
                        color = category.color,
                        isActive = category.isActive,
                        usageCount = usageCount,
                        totalExpenses = totalExpenses,
                        totalIncome = totalIncome,
                        lastUsed = lastUsed
                    )
                }.sortedByDescending { it.usageCount }
                
                _categoriesWithStats.value = stats
                updateFilteredCategories()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun loadFavoriteCategories() {
        viewModelScope.launch {
            try {
                // Get categories used in the last 30 days
                val thirtyDaysAgo = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -30)
                }.timeInMillis
                
                val categories = categoryDao.getAllCategories()
                val favoriteStats = categories.mapNotNull { category ->
                    val usageCount = categoryDao.getCategoryUsageCount(category.id)
                    val lastUsed = categoryDao.getCategoryLastUsed(category.id)
                    
                    // Only include categories used in the last 30 days
                    if (lastUsed != null && lastUsed >= thirtyDaysAgo && usageCount > 0) {
                        val totalExpenses = categoryDao.getCategoryTotalExpenses(category.id) ?: 0.0
                        val totalIncome = categoryDao.getCategoryTotalIncome(category.id) ?: 0.0
                        
                        CategoryStats(
                            id = category.id,
                            name = category.name,
                            icon = category.icon,
                            color = category.color,
                            isActive = category.isActive,
                            usageCount = usageCount,
                            totalExpenses = totalExpenses,
                            totalIncome = totalIncome,
                            lastUsed = lastUsed
                        )
                    } else null
                }.sortedByDescending { it.usageCount }.take(5)
                
                _favoriteCategories.value = favoriteStats
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun searchCategories(query: String) {
        _searchQuery.value = query
        updateFilteredCategories()
    }
    
    private fun updateFilteredCategories() {
        viewModelScope.launch {
            try {
                val filtered = if (_searchQuery.value.isBlank()) {
                    _categoriesWithStats.value
                } else {
                    // Filter by search query in memory since we can't use the complex Room query
                    _categoriesWithStats.value.filter { 
                        it.name.contains(_searchQuery.value, ignoreCase = true) 
                    }
                }
                _filteredCategories.value = filtered
            } catch (e: Exception) {
                _filteredCategories.value = _categoriesWithStats.value.filter { 
                    it.name.contains(_searchQuery.value, ignoreCase = true) 
                }
            }
        }
    }

    fun insertCategory(category: Category) {
        viewModelScope.launch {
            try {
                categoryDao.insertCategory(category)
                loadCategories()
                loadCategoriesWithStats()
                loadFavoriteCategories()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun updateCategory(category: Category) {
        viewModelScope.launch {
            try {
                categoryDao.updateCategory(category)
                loadCategories()
                loadCategoriesWithStats()
                loadFavoriteCategories()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                categoryDao.deleteCategory(category)
                loadCategories()
                loadCategoriesWithStats()
                loadFavoriteCategories()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun getCategoryUsageCount(categoryId: Int): Int {
        return _categoriesWithStats.value.find { it.id == categoryId }?.usageCount ?: 0
    }
}
