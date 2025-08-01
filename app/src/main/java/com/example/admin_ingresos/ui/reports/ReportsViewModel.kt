package com.example.admin_ingresos.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin_ingresos.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReportsViewModel(private val database: AppDatabase) : ViewModel() {
    
    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val budgetDao = database.budgetDao()
    private val analyticsService = AnalyticsService()
    
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()
    
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    private val _monthlyTrends = MutableStateFlow<List<MonthlyTrend>>(emptyList())
    val monthlyTrends: StateFlow<List<MonthlyTrend>> = _monthlyTrends.asStateFlow()
    
    private val _categoryTrends = MutableStateFlow<List<CategoryTrend>>(emptyList())
    val categoryTrends: StateFlow<List<CategoryTrend>> = _categoryTrends.asStateFlow()
    
    private val _spendingPatterns = MutableStateFlow<SpendingPatterns?>(null)
    val spendingPatterns: StateFlow<SpendingPatterns?> = _spendingPatterns.asStateFlow()
    
    private val _financialInsights = MutableStateFlow<List<FinancialInsight>>(emptyList())
    val financialInsights: StateFlow<List<FinancialInsight>> = _financialInsights.asStateFlow()
    
    private val _financialProjections = MutableStateFlow<FinancialProjections?>(null)
    val financialProjections: StateFlow<FinancialProjections?> = _financialProjections.asStateFlow()
    
    private val _comparativeAnalysis = MutableStateFlow<ComparativeAnalysis?>(null)
    val comparativeAnalysis: StateFlow<ComparativeAnalysis?> = _comparativeAnalysis.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadData()
    }
    
    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load basic data
                val transactions = transactionDao.getAll()
                val categories = categoryDao.getAll()
                
                _transactions.value = transactions
                _categories.value = categories
                
                // Calculate analytics
                if (transactions.isNotEmpty()) {
                    _monthlyTrends.value = analyticsService.calculateMonthlyTrends(transactions)
                    _categoryTrends.value = analyticsService.calculateCategoryTrends(transactions, categories)
                    _spendingPatterns.value = analyticsService.detectSpendingPatterns(transactions)
                    
                    // Get budget data for insights
                    val currentTime = System.currentTimeMillis()
                    val budgetProgressData = budgetDao.getCurrentBudgetProgress(currentTime)
                    val budgetProgress = budgetProgressData.map { raw ->
                        val category = categories.find { it.id == raw.categoryId }
                        val budget = Budget(
                            id = raw.id,
                            categoryId = raw.categoryId,
                            amount = raw.amount,
                            period = raw.period,
                            startDate = raw.startDate,
                            endDate = raw.endDate,
                            isActive = raw.isActive,
                            createdAt = raw.createdAt,
                            updatedAt = raw.updatedAt
                        )
                        
                        val remaining = (budget.amount - raw.spent).coerceAtLeast(0.0)
                        val percentage = if (budget.amount > 0) (raw.spent / budget.amount).toFloat() else 0f
                        val isOverBudget = raw.spent > budget.amount
                        val daysRemaining = ((budget.endDate - currentTime) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
                        
                        BudgetProgress(
                            budget = budget,
                            category = category ?: Category(raw.categoryId, raw.categoryName, raw.categoryIcon, raw.categoryColor),
                            spent = raw.spent,
                            remaining = remaining,
                            percentage = percentage,
                            isOverBudget = isOverBudget,
                            daysRemaining = daysRemaining
                        )
                    }
                    
                    _financialInsights.value = analyticsService.generateFinancialInsights(
                        transactions, categories, budgetProgress
                    )
                    
                    // Generate financial projections
                    _financialProjections.value = analyticsService.generateFinancialProjections(
                        transactions, categories, budgetProgress
                    )
                    
                    // Generate comparative analysis
                    _comparativeAnalysis.value = analyticsService.generateComparativeAnalysis(
                        transactions, categories
                    )
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadTransactions() {
        loadData()
    }
    
    fun refreshData() {
        loadData()
    }
}