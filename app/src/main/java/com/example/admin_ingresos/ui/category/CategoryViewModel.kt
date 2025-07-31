package com.example.admin_ingresos.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin_ingresos.data.AppDatabase
import com.example.admin_ingresos.data.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(private val db: AppDatabase) : ViewModel() {
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _categories.value = db.categoryDao().getAll()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addCategory(name: String, icon: String, color: String) {
        viewModelScope.launch {
            try {
                db.categoryDao().insert(Category(name = name, icon = icon, color = color))
                loadCategories()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun updateCategory(category: Category) {
        viewModelScope.launch {
            try {
                db.categoryDao().update(category)
                loadCategories()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                db.categoryDao().delete(category)
                loadCategories()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun getCategoryUsageCount(categoryId: Int): Int {
        // This would require a query to count transactions using this category
        // For now, return 0 as placeholder
        return 0
    }
}
