package com.example.admin_ingresos.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin_ingresos.data.AppDatabase
import com.example.admin_ingresos.data.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(private val db: AppDatabase) : ViewModel() {
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    fun loadCategories() {
        viewModelScope.launch {
            _categories.value = db.categoryDao().getAll()
        }
    }

    fun addCategory(name: String, icon: String) {
        viewModelScope.launch {
            db.categoryDao().insert(Category(name = name, icon = icon))
            loadCategories()
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            db.categoryDao().delete(category)
            loadCategories()
        }
    }
}
