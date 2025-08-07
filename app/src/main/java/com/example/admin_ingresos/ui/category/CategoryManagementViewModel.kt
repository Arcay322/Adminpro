
package com.example.admin_ingresos.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin_ingresos.data.Category
import com.example.admin_ingresos.data.CategoryDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryManagementUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val showDialog: Boolean = false,
    val editingCategory: Category? = null
)

class CategoryManagementViewModel(private val categoryDao: CategoryDao) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryManagementUiState())
    val uiState: StateFlow<CategoryManagementUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            categoryDao.getAllCategories().catch { e ->
                _uiState.update { it.copy(error = "Error al cargar categorías: ${e.message}", isLoading = false) }
            }.collect { categories ->
                _uiState.update {
                    it.copy(
                        categories = categories,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun showAddCategoryDialog() {
        _uiState.update { it.copy(showDialog = true, editingCategory = null) }
    }

    fun showEditCategoryDialog(category: Category) {
        _uiState.update { it.copy(showDialog = true, editingCategory = category) }
    }

    fun dismissCategoryDialog() {
        _uiState.update { it.copy(showDialog = false, editingCategory = null) }
    }

    fun saveCategory(name: String, icon: String, isFavorite: Boolean) {
        viewModelScope.launch {
            val category = _uiState.value.editingCategory?.copy(name = name, icon = icon, isFavorite = isFavorite)
                ?: Category(name = name, icon = icon, isFavorite = isFavorite)

            if (category.id == 0) {
                categoryDao.insert(category)
            } else {
                categoryDao.update(category)
            }
            dismissCategoryDialog()
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.delete(category)
        }
    }

    fun toggleFavorite(category: Category) {
        viewModelScope.launch {
            categoryDao.update(category.copy(isFavorite = !category.isFavorite))
        }
    }
}
