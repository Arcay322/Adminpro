package com.example.admin_ingresos.ui.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin_ingresos.data.Category
import com.example.admin_ingresos.data.CategoryDao
import com.example.admin_ingresos.data.Transaction
import com.example.admin_ingresos.data.TransactionDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Define el estado de la UI para la pantalla de detalles de categoría.
 */
data class CategoryDetailUiState(
    val category: Category? = null,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel para la pantalla de detalles de una categoría. Se encarga de obtener
 * los datos de la categoría y su lista de transacciones desde la base de datos.
 */
class CategoryDetailViewModel(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    categoryId: Int?
) : ViewModel() {

    // El StateFlow privado para manejar el estado internamente.
    private val _uiState = MutableStateFlow(CategoryDetailUiState())
    // El StateFlow público que la UI observará para actualizarse.
    val uiState: StateFlow<CategoryDetailUiState> = _uiState

    init {
        if (categoryId != null) {
            loadData(categoryId)
        } else {
            _uiState.update {
                it.copy(isLoading = false, error = "No se pudo encontrar el ID de la categoría.")
            }
        }
    }

    private fun loadData(categoryId: Int) {
        viewModelScope.launch {
            // Primero, buscamos los detalles de la categoría.
            try {
                val category = categoryDao.getCategoryById(categoryId)
                if (category != null) {
                    // Si la encontramos, actualizamos el estado con la categoría
                    // y LUEGO buscamos sus transacciones.
                    _uiState.update { it.copy(category = category) }
                    
                    // Nos suscribimos al Flow de transacciones.
                    transactionDao.getTransactionsByCategoryIdFlow(categoryId)
                        .catch { e ->
                            _uiState.update { it.copy(error = e.message, isLoading = false) }
                        }
                        .collect { transactions ->
                            // Cada vez que la lista de transacciones cambie,
                            // actualizamos el estado y finalizamos la carga.
                            _uiState.update {
                                it.copy(transactions = transactions, isLoading = false)
                            }
                        }
                } else {
                    // Si no encontramos la categoría, mostramos un error.
                    _uiState.update {
                        it.copy(isLoading = false, error = "Categoría no encontrada.")
                    }
                }
            } catch (e: Exception) {
                // Si hay cualquier otro error, lo mostramos.
                _uiState.update {
                    it.copy(isLoading = false, error = "Error al cargar los datos.")
                }
            }
        }
    }
}