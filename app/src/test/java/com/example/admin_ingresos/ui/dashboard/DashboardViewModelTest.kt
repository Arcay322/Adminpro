package com.example.admin_ingresos.ui.dashboard

import com.example.admin_ingresos.data.Transaction
import com.example.admin_ingresos.data.TransactionRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FakeTransactionRepository : TransactionRepository(null) {
    private val transactions = mutableListOf<Transaction>()
    override suspend fun getAllTransactions(): List<Transaction> = transactions
    fun setTransactions(list: List<Transaction>) { transactions.clear(); transactions.addAll(list) }
}

class DashboardViewModelTest {
    private lateinit var repository: FakeTransactionRepository
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        repository = FakeTransactionRepository()
        // Pass a minimal fake AppDatabase by using the provider already in the project (null is acceptable for tests here)
        val fakeDb = com.example.admin_ingresos.data.AppDatabase::class.java
        viewModel = DashboardViewModel(repository, com.example.admin_ingresos.data.AppDatabaseProvider.getDatabase(androidx.test.core.app.ApplicationProvider.getApplicationContext()))
    }

    @Test
    fun testBalanceCalculation() = runBlocking {
        val txs = listOf(
            Transaction(id = 1, amount = 1000.0, type = "Ingreso", categoryId = 1, description = "Salario", date = 0L, paymentMethodId = null),
            Transaction(id = 2, amount = 200.0, type = "Gasto", categoryId = 2, description = "Comida", date = 0L, paymentMethodId = null),
            Transaction(id = 3, amount = 300.0, type = "Gasto", categoryId = 3, description = "Transporte", date = 0L, paymentMethodId = null)
        )
        repository.setTransactions(txs)
        viewModel.loadTransactions()
        val transactions = viewModel.transactions.value
        val ingresos = transactions.filter { it.type == "Ingreso" }.sumOf { it.amount }
        val gastos = transactions.filter { it.type == "Gasto" }.sumOf { it.amount }
        val balance = ingresos - gastos
        assertEquals(1000.0, ingresos, 0.01)
        assertEquals(500.0, gastos, 0.01)
        assertEquals(500.0, balance, 0.01)
    }

    @Test
    fun testWeeklyFlowCalculation() = runBlocking {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val today = cal.timeInMillis
        val txs = listOf(
            Transaction(id = 10, amount = 100.0, type = "Ingreso", categoryId = null, description = "t1", date = today, paymentMethodId = null),
            Transaction(id = 11, amount = 50.0, type = "Gasto", categoryId = null, description = "t2", date = today - 24*60*60*1000L, paymentMethodId = null)
        )
        repository.setTransactions(txs)
        val weekly = viewModel.getWeeklyFlowData(txs)
        assertEquals(7, weekly.size)
        assertTrue(weekly.any { it.income > 0 || it.expense > 0 })
    }
}
