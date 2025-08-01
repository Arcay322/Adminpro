package com.example.admin_ingresos.data

data class FilterPreset(
    val name: String,
    val filter: TransactionFilter
) {
    companion object {
        fun getDefaultPresets(): List<FilterPreset> {
            val currentTime = System.currentTimeMillis()
            val dayInMillis = 24 * 60 * 60 * 1000L
            val weekInMillis = 7 * dayInMillis
            val monthInMillis = 30 * dayInMillis
            
            return listOf(
                FilterPreset(
                    name = "Hoy",
                    filter = TransactionFilter(
                        dateRange = DateRange(
                            startDate = currentTime - dayInMillis,
                            endDate = currentTime
                        )
                    )
                ),
                FilterPreset(
                    name = "Esta semana",
                    filter = TransactionFilter(
                        dateRange = DateRange(
                            startDate = currentTime - weekInMillis,
                            endDate = currentTime
                        )
                    )
                ),
                FilterPreset(
                    name = "Este mes",
                    filter = TransactionFilter(
                        dateRange = DateRange(
                            startDate = currentTime - monthInMillis,
                            endDate = currentTime
                        )
                    )
                ),
                FilterPreset(
                    name = "Solo ingresos",
                    filter = TransactionFilter(
                        transactionTypes = listOf("Ingreso")
                    )
                ),
                FilterPreset(
                    name = "Solo gastos",
                    filter = TransactionFilter(
                        transactionTypes = listOf("Gasto")
                    )
                ),
                FilterPreset(
                    name = "Gastos grandes",
                    filter = TransactionFilter(
                        transactionTypes = listOf("Gasto"),
                        amountRange = AmountRange(
                            minAmount = 100.0,
                            maxAmount = Double.MAX_VALUE
                        )
                    )
                )
            )
        }
    }
}