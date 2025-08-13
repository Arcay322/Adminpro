// REEMPLAZA TU ARCHIVO ui/category/CategoryViewModel.kt COMPLETO

package com.example.admin_ingresos.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin_ingresos.data.AppDatabase
import com.example.admin_ingresos.data.Category
import com.example.admin_ingresos.data.CategoryTransactionStats // <-- Importa la clase renombrada
import com.example.admin_ingresos.data.CategoryType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CategoryViewModel(private val db: AppDatabase) : ViewModel() {

    // El UiState ahora contiene el mapa con el tipo correcto.
    data class UiState(
        val selectedTab: CategoryType = CategoryType.GASTO,
        val categories: List<Category> = emptyList(),
        val archivedCategories: List<Category> = emptyList(),
        val searchQuery: String = "",
        val showAddEditDialog: Category? = null,
        val showArchiveDialog: Category? = null,
        val statsMap: Map<Int, CategoryTransactionStats> = emptyMap() // <-- TIPO CORREGIDO
    )

    private val categoryDao = db.categoryDao()
    private val transactionDao = db.transactionDao()

    private val _selectedTab = MutableStateFlow(CategoryType.GASTO)
    private val _searchQuery = MutableStateFlow("")
    private val _dialogState = MutableStateFlow(Pair<Category?, Category?>(null, null))

    private val _snackbarEvents = Channel<String>(Channel.BUFFERED)
    val snackbarEvents = _snackbarEvents.receiveAsFlow()
    
    val uiState: StateFlow<UiState> = combine(
        categoryDao.getAllCategories(),
        transactionDao.getAllCategoryStats(), // Ahora devuelve el tipo correcto
        _selectedTab,
        _searchQuery,
        _dialogState
    ) { allCategories, statsMap, selectedTab, query, dialogs ->
        
        val filteredActive = allCategories.filter {
            !it.isArchived &&
            it.type == selectedTab &&
            (query.isBlank() || it.name.contains(query, ignoreCase = true))
        }

        UiState(
            selectedTab = selectedTab,
            categories = filteredActive,
            archivedCategories = allCategories.filter { it.isArchived },
            searchQuery = query,
            statsMap = statsMap,
            showAddEditDialog = dialogs.first,
            showArchiveDialog = dialogs.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState()
    )
    
    fun onTabSelected(tab: CategoryType) { _selectedTab.value = tab }
    fun onSearchQueryChanged(query: String) { _searchQuery.value = query }
    fun showAddEditDialog(category: Category?) { _dialogState.value = _dialogState.value.copy(first = category) }
    fun hideAddEditDialog() { _dialogState.value = _dialogState.value.copy(first = null) }
    fun showArchiveDialog(category: Category?) { _dialogState.value = _dialogState.value.copy(second = category) }
    fun hideArchiveDialog() { _dialogState.value = _dialogState.value.copy(second = null) }

    fun addCategory(category: Category) = viewModelScope.launch {
        val maxOrder = categoryDao.getMaxOrder() ?: -1
        categoryDao.insert(category.copy(order = maxOrder + 1, type = _selectedTab.value))
        _snackbarEvents.send("Categoría guardada")
    }

    fun updateCategory(category: Category) = viewModelScope.launch {
        categoryDao.update(category)
        _snackbarEvents.send("Categoría actualizada")
    }

    fun archiveCategory(category: Category) = viewModelScope.launch {
        categoryDao.archiveCategory(category.id)
        _snackbarEvents.send("Categoría archivada")
        hideArchiveDialog()
    }

    fun unarchiveCategory(category: Category) = viewModelScope.launch {
        categoryDao.unarchiveCategory(category.id)
        _snackbarEvents.send("Categoría restaurada")
    }

    fun reorderCategories(newOrder: List<Category>) = viewModelScope.launch {
        val updates = newOrder.mapIndexed { index, category -> category.copy(order = index) }
        categoryDao.updateAll(updates)
    }
}