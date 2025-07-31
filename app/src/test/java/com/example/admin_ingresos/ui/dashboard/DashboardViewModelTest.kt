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
        viewModel = DashboardViewModel(repository)
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
}
