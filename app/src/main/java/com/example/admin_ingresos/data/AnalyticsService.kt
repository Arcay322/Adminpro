package com.example.admin_ingresos.data

import java.util.*
import kotlin.math.abs

class AnalyticsService {
    
    fun calculateMonthlyTrends(transactions: List<Transaction>): List<MonthlyTrend> {
        val calendar = Calendar.getInstance()
        val monthlyData = mutableMapOf<String, MonthlyData>()
        
        transactions.forEach { transaction ->
            calendar.timeInMillis = transaction.date
            val monthKey = "${calendar.get(Calendar.YEAR)}-${String.format("%02d", calendar.get(Calendar.MONTH) + 1)}"
            
            val existing = monthlyData[monthKey] ?: MonthlyData(
                year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH) + 1,
                income = 0.0,
                expenses = 0.0,
                transactionCount = 0
            )
            
            monthlyData[monthKey] = when (transaction.type) {
                "Ingreso" -> existing.copy(
                    income = existing.income + transaction.amount,
                    transactionCount = existing.transactionCount + 1
                )
                "Gasto" -> existing.copy(
                    expenses = existing.expenses + transaction.amount,
                    transactionCount = existing.transactionCount + 1
                )
                else -> existing
            }
        }
        
        val sortedData = monthlyData.values.sortedBy { "${it.year}-${String.format("%02d", it.month)}" }
        
        return sortedData.mapIndexed { index, current ->
            val previous = if (index > 0) sortedData[index - 1] else null
            
            MonthlyTrend(
                monthlyData = current,
                incomeChange = calculatePercentageChange(previous?.income, current.income),
                expenseChange = calculatePercentageChange(previous?.expenses, current.expenses),
                balanceChange = calculatePercentageChange(
                    previous?.let { it.income - it.expenses },
                    current.income - current.expenses
                ),
                transactionCountChange = calculatePercentageChange(
                    previous?.transactionCount?.toDouble(),
                    current.transactionCount.toDouble()
                )
            )
        }
    }
    
    fun calculateCategoryTrends(
        transactions: List<Transaction>,
        categories: List<Category>
    ): List<CategoryTrend> {
        val calendar = Calendar.getInstance()
        val categoryMonthlyData = mutableMapOf<Int, MutableMap<String, Double>>()
        
        transactions.filter { it.type == "Gasto" }.forEach { transaction ->
            calendar.timeInMillis = transaction.date
            val monthKey = "${calendar.get(Calendar.YEAR)}-${String.format("%02d", calendar.get(Calendar.MONTH) + 1)}"
            
            val categoryData = categoryMonthlyData.getOrPut(transaction.categoryId) { mutableMapOf() }
            categoryData[monthKey] = (categoryData[monthKey] ?: 0.0) + transaction.amount
        }
        
        return categories.mapNotNull { category ->
            val categoryData = categoryMonthlyData[category.id]
            if (categoryData.isNullOrEmpty()) return@mapNotNull null
            
            val sortedMonths = categoryData.keys.sorted()
            if (sortedMonths.size < 2) return@mapNotNull null
            
            val currentMonth = categoryData[sortedMonths.last()] ?: 0.0
            val previousMonth = categoryData[sortedMonths[sortedMonths.size - 2]] ?: 0.0
            val averageSpending = categoryData.values.average()
            
            CategoryTrend(
                category = category,
                currentMonthSpending = currentMonth,
                previousMonthSpending = previousMonth,
                averageMonthlySpending = averageSpending,
                trend = calculatePercentageChange(previousMonth, currentMonth),
                isIncreasing = currentMonth > previousMonth,
                monthlyData = categoryData.toMap()
            )
        }
    }
    
    fun detectSpendingPatterns(transactions: List<Transaction>): SpendingPatterns {
        val calendar = Calendar.getInstance()
        val dayOfWeekSpending = mutableMapOf<Int, Double>()
        val hourOfDaySpending = mutableMapOf<Int, Double>()
        val monthlySpending = mutableMapOf<Int, Double>()
        
        transactions.filter { it.type == "Gasto" }.forEach { transaction ->
            calendar.timeInMillis = transaction.date
            
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            val month = calendar.get(Calendar.MONTH)
            
            dayOfWeekSpending[dayOfWeek] = (dayOfWeekSpending[dayOfWeek] ?: 0.0) + transaction.amount
            hourOfDaySpending[hourOfDay] = (hourOfDaySpending[hourOfDay] ?: 0.0) + transaction.amount
            monthlySpending[month] = (monthlySpending[month] ?: 0.0) + transaction.amount
        }
        
        return SpendingPatterns(
            mostExpensiveDayOfWeek = dayOfWeekSpending.maxByOrNull { it.value }?.key ?: Calendar.SUNDAY,
            mostExpensiveHour = hourOfDaySpending.maxByOrNull { it.value }?.key ?: 12,
            mostExpensiveMonth = monthlySpending.maxByOrNull { it.value }?.key ?: Calendar.JANUARY,
            dayOfWeekSpending = dayOfWeekSpending,
            hourOfDaySpending = hourOfDaySpending,
            monthlySeasonalSpending = monthlySpending
        )
    }
    
    fun generateFinancialInsights(
        transactions: List<Transaction>,
        categories: List<Category>,
        budgets: List<BudgetProgress>
    ): List<FinancialInsight> {
        val insights = mutableListOf<FinancialInsight>()
        
        // Analyze spending trends
        val monthlyTrends = calculateMonthlyTrends(transactions)
        if (monthlyTrends.size >= 2) {
            val latestTrend = monthlyTrends.last()
            
            if (latestTrend.expenseChange > 20) {
                insights.add(
                    FinancialInsight(
                        type = InsightType.WARNING,
                        title = "Aumento significativo en gastos",
                        description = "Tus gastos han aumentado un ${String.format("%.1f", latestTrend.expenseChange)}% este mes",
                        recommendation = "Revisa tus gastos recientes y considera ajustar tu presupuesto"
                    )
                )
            }
            
            if (latestTrend.incomeChange < -10) {
                insights.add(
                    FinancialInsight(
                        type = InsightType.WARNING,
                        title = "Disminución en ingresos",
                        description = "Tus ingresos han disminuido un ${String.format("%.1f", abs(latestTrend.incomeChange))}% este mes",
                        recommendation = "Considera diversificar tus fuentes de ingreso"
                    )
                )
            }
            
            // Positive trends
            if (latestTrend.balanceChange > 15) {
                insights.add(
                    FinancialInsight(
                        type = InsightType.TIP,
                        title = "Mejora en tu balance financiero",
                        description = "Tu balance ha mejorado un ${String.format("%.1f", latestTrend.balanceChange)}% este mes",
                        recommendation = "¡Excelente! Considera ahorrar o invertir este excedente"
                    )
                )
            }
        }
        
        // Analyze budget performance
        budgets.forEach { budget ->
            when {
                budget.percentage >= 1.2 -> {
                    insights.add(
                        FinancialInsight(
                            type = InsightType.ALERT,
                            title = "Presupuesto muy excedido",
                            description = "Has gastado ${String.format("%.0f", budget.percentage * 100)}% del presupuesto de ${budget.category.name}",
                            recommendation = "Reduce los gastos en esta categoría o ajusta el presupuesto"
                        )
                    )
                }
                budget.percentage >= 0.8 -> {
                    insights.add(
                        FinancialInsight(
                            type = InsightType.TIP,
                            title = "Presupuesto cerca del límite",
                            description = "Has usado ${String.format("%.0f", budget.percentage * 100)}% del presupuesto de ${budget.category.name}",
                            recommendation = "Controla los gastos en esta categoría para no exceder el presupuesto"
                        )
                    )
                }
                budget.percentage < 0.5 && budget.daysRemaining < 10 -> {
                    insights.add(
                        FinancialInsight(
                            type = InsightType.INFO,
                            title = "Presupuesto subutilizado",
                            description = "Solo has usado ${String.format("%.0f", budget.percentage * 100)}% del presupuesto de ${budget.category.name}",
                            recommendation = "Podrías reasignar parte de este presupuesto a otras categorías"
                        )
                    )
                }
            }
        }
        
        // Analyze category spending
        val categoryTrends = calculateCategoryTrends(transactions, categories)
        categoryTrends.forEach { trend ->
            if (trend.trend > 50) {
                insights.add(
                    FinancialInsight(
                        type = InsightType.INFO,
                        title = "Aumento en ${trend.category.name}",
                        description = "Tus gastos en ${trend.category.name} han aumentado ${String.format("%.1f", trend.trend)}%",
                        recommendation = "Revisa si este aumento es justificado o si puedes optimizar estos gastos"
                    )
                )
            }
        }
        
        // Detect unusual spending patterns
        insights.addAll(detectUnusualSpending(transactions, categories))
        
        // Analyze spending efficiency
        insights.addAll(analyzeSpendingEfficiency(transactions, categories))
        
        // Generate personalized recommendations
        insights.addAll(generatePersonalizedRecommendations(transactions, categories, budgets))
        
        return insights.take(10) // Limit to top 10 insights
    }
    
    private fun detectUnusualSpending(
        transactions: List<Transaction>,
        categories: List<Category>
    ): List<FinancialInsight> {
        val insights = mutableListOf<FinancialInsight>()
        val calendar = Calendar.getInstance()
        
        // Get last 30 days transactions
        val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        val recentTransactions = transactions.filter { it.date >= thirtyDaysAgo && it.type == "Gasto" }
        
        if (recentTransactions.isEmpty()) return insights
        
        // Calculate average transaction amount
        val averageAmount = recentTransactions.map { it.amount }.average()
        val highValueTransactions = recentTransactions.filter { it.amount > averageAmount * 2 }
        
        if (highValueTransactions.isNotEmpty()) {
            val totalHighValue = highValueTransactions.sumOf { it.amount }
            val percentage = (totalHighValue / recentTransactions.sumOf { it.amount }) * 100
            
            if (percentage > 30) {
                insights.add(
                    FinancialInsight(
                        type = InsightType.WARNING,
                        title = "Gastos inusuales detectados",
                        description = "${highValueTransactions.size} transacciones representan el ${String.format("%.1f", percentage)}% de tus gastos recientes",
                        recommendation = "Revisa estos gastos grandes para asegurar que sean necesarios"
                    )
                )
            }
        }
        
        // Detect weekend vs weekday spending patterns
        val weekendSpending = recentTransactions.filter { transaction ->
            calendar.timeInMillis = transaction.date
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
        }.sumOf { it.amount }
        
        val weekdaySpending = recentTransactions.sumOf { it.amount } - weekendSpending
        val weekendDays = recentTransactions.filter { transaction ->
            calendar.timeInMillis = transaction.date
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
        }.map { transaction ->
            calendar.timeInMillis = transaction.date
            "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
        }.distinct().size
        
        val weekdays = recentTransactions.size - weekendDays
        
        if (weekendDays > 0 && weekdays > 0) {
            val weekendAverage = weekendSpending / weekendDays
            val weekdayAverage = weekdaySpending / weekdays
            
            if (weekendAverage > weekdayAverage * 1.5) {
                insights.add(
                    FinancialInsight(
                        type = InsightType.TIP,
                        title = "Gastos elevados en fines de semana",
                        description = "Gastas ${String.format("%.1f", (weekendAverage / weekdayAverage - 1) * 100)}% más los fines de semana",
                        recommendation = "Planifica un presupuesto específico para entretenimiento de fin de semana"
                    )
                )
            }
        }
        
        return insights
    }
    
    private fun analyzeSpendingEfficiency(
        transactions: List<Transaction>,
        categories: List<Category>
    ): List<FinancialInsight> {
        val insights = mutableListOf<FinancialInsight>()
        
        // Analyze transaction frequency vs amount
        val categorySpending = transactions.filter { it.type == "Gasto" }
            .groupBy { it.categoryId }
            .mapValues { (_, transactions) ->
                CategorySpendingAnalysis(
                    totalAmount = transactions.sumOf { it.amount },
                    transactionCount = transactions.size,
                    averageAmount = transactions.map { it.amount }.average()
                )
            }
        
        categorySpending.forEach { (categoryId, analysis) ->
            val category = categories.find { it.id == categoryId }
            if (category != null && analysis.transactionCount > 5) {
                // Many small transactions might indicate inefficient spending
                if (analysis.averageAmount < 20 && analysis.transactionCount > 10) {
                    insights.add(
                        FinancialInsight(
                            type = InsightType.TIP,
                            title = "Muchas transacciones pequeñas en ${category.name}",
                            description = "${analysis.transactionCount} transacciones con promedio de $${String.format("%.2f", analysis.averageAmount)}",
                            recommendation = "Considera comprar en mayor cantidad para aprovechar descuentos"
                        )
                    )
                }
            }
        }
        
        return insights
    }
    
    private fun generatePersonalizedRecommendations(
        transactions: List<Transaction>,
        categories: List<Category>,
        budgets: List<BudgetProgress>
    ): List<FinancialInsight> {
        val insights = mutableListOf<FinancialInsight>()
        
        val totalIncome = transactions.filter { it.type == "Ingreso" }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.type == "Gasto" }.sumOf { it.amount }
        
        if (totalIncome > 0) {
            val savingsRate = ((totalIncome - totalExpenses) / totalIncome) * 100
            
            when {
                savingsRate < 10 -> {
                    insights.add(
                        FinancialInsight(
                            type = InsightType.WARNING,
                            title = "Tasa de ahorro baja",
                            description = "Solo estás ahorrando ${String.format("%.1f", savingsRate)}% de tus ingresos",
                            recommendation = "Intenta ahorrar al menos 20% de tus ingresos mensuales"
                        )
                    )
                }
                savingsRate > 30 -> {
                    insights.add(
                        FinancialInsight(
                            type = InsightType.TIP,
                            title = "Excelente tasa de ahorro",
                            description = "Estás ahorrando ${String.format("%.1f", savingsRate)}% de tus ingresos",
                            recommendation = "Considera invertir parte de estos ahorros para hacerlos crecer"
                        )
                    )
                }
            }
        }
        
        // Recommend budget creation for categories without budgets
        val categoriesWithBudgets = budgets.map { it.category.id }.toSet()
        val expensesByCategory = transactions.filter { it.type == "Gasto" }
            .groupBy { it.categoryId }
            .mapValues { it.value.sumOf { transaction -> transaction.amount } }
            .toList()
            .sortedByDescending { it.second }
        
        expensesByCategory.take(3).forEach { (categoryId, amount) ->
            if (!categoriesWithBudgets.contains(categoryId)) {
                val category = categories.find { it.id == categoryId }
                if (category != null && amount > 100) {
                    insights.add(
                        FinancialInsight(
                            type = InsightType.TIP,
                            title = "Crear presupuesto para ${category.name}",
                            description = "Has gastado $${String.format("%.2f", amount)} en esta categoría",
                            recommendation = "Crear un presupuesto te ayudará a controlar mejor estos gastos"
                        )
                    )
                }
            }
        }
        
        return insights
    }
    
    private data class CategorySpendingAnalysis(
        val totalAmount: Double,
        val transactionCount: Int,
        val averageAmount: Double
    )
    
    fun generateFinancialProjections(
        transactions: List<Transaction>,
        categories: List<Category>,
        budgets: List<BudgetProgress>
    ): FinancialProjections {
        val monthlyTrends = calculateMonthlyTrends(transactions)
        
        if (monthlyTrends.size < 3) {
            return FinancialProjections(
                nextMonthIncome = 0.0,
                nextMonthExpenses = 0.0,
                nextMonthBalance = 0.0,
                projectedSavings = 0.0,
                budgetProjections = emptyList(),
                confidence = 0.0
            )
        }
        
        // Calculate average growth rates
        val incomeGrowthRates = monthlyTrends.takeLast(3).map { it.incomeChange }
        val expenseGrowthRates = monthlyTrends.takeLast(3).map { it.expenseChange }
        
        val avgIncomeGrowth = incomeGrowthRates.average() / 100
        val avgExpenseGrowth = expenseGrowthRates.average() / 100
        
        val lastMonth = monthlyTrends.last().monthlyData
        
        // Project next month
        val projectedIncome = lastMonth.income * (1 + avgIncomeGrowth)
        val projectedExpenses = lastMonth.expenses * (1 + avgExpenseGrowth)
        val projectedBalance = projectedIncome - projectedExpenses
        
        // Calculate confidence based on trend consistency
        val incomeVariance = calculateVariance(incomeGrowthRates)
        val expenseVariance = calculateVariance(expenseGrowthRates)
        val confidence = (1.0 / (1.0 + incomeVariance + expenseVariance)).coerceIn(0.0, 1.0)
        
        // Project budget performance
        val budgetProjections = budgets.map { budget ->
            val currentSpendingRate = budget.spent / (budget.budget.amount.coerceAtLeast(1.0))
            val projectedSpending = budget.spent * (1 + avgExpenseGrowth)
            val projectedPercentage = (projectedSpending / budget.budget.amount).toFloat()
            
            BudgetProjection(
                budget = budget,
                projectedSpending = projectedSpending,
                projectedPercentage = projectedPercentage,
                willExceedBudget = projectedPercentage > 1.0
            )
        }
        
        // Calculate 6-month savings projection
        val monthlySavings = projectedBalance
        val projectedSavings = if (monthlySavings > 0) monthlySavings * 6 else 0.0
        
        return FinancialProjections(
            nextMonthIncome = projectedIncome,
            nextMonthExpenses = projectedExpenses,
            nextMonthBalance = projectedBalance,
            projectedSavings = projectedSavings,
            budgetProjections = budgetProjections,
            confidence = confidence
        )
    }
    
    fun generateSavingsGoalProjections(
        transactions: List<Transaction>,
        targetAmount: Double,
        targetMonths: Int
    ): SavingsGoalProjection {
        val monthlyTrends = calculateMonthlyTrends(transactions)
        
        if (monthlyTrends.isEmpty()) {
            return SavingsGoalProjection(
                targetAmount = targetAmount,
                targetMonths = targetMonths,
                monthlyRequiredSavings = targetAmount / targetMonths,
                currentMonthlySavings = 0.0,
                isAchievable = false,
                monthsToAchieve = 0,
                recommendedAdjustments = emptyList()
            )
        }
        
        val recentBalance = monthlyTrends.takeLast(3).map { it.monthlyData.balance }.average()
        val monthlyRequiredSavings = targetAmount / targetMonths
        
        val isAchievable = recentBalance >= monthlyRequiredSavings
        val monthsToAchieve = if (recentBalance > 0) (targetAmount / recentBalance).toInt() else 0
        
        val recommendations = mutableListOf<String>()
        
        if (!isAchievable) {
            val deficit = monthlyRequiredSavings - recentBalance
            recommendations.add("Necesitas ahorrar $${String.format("%.2f", deficit)} más por mes")
            
            if (deficit > recentBalance * 0.5) {
                recommendations.add("Considera extender el plazo o reducir la meta")
            } else {
                recommendations.add("Revisa tus gastos para encontrar áreas de ahorro")
            }
        } else {
            recommendations.add("¡Tu meta es alcanzable con tu ritmo actual de ahorro!")
            if (recentBalance > monthlyRequiredSavings * 1.2) {
                recommendations.add("Podrías alcanzar tu meta antes de tiempo")
            }
        }
        
        return SavingsGoalProjection(
            targetAmount = targetAmount,
            targetMonths = targetMonths,
            monthlyRequiredSavings = monthlyRequiredSavings,
            currentMonthlySavings = recentBalance,
            isAchievable = isAchievable,
            monthsToAchieve = monthsToAchieve,
            recommendedAdjustments = recommendations
        )
    }
    
    private fun calculateVariance(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        val mean = values.average()
        return values.map { (it - mean) * (it - mean) }.average()
    }
    
    fun generateComparativeAnalysis(
        transactions: List<Transaction>,
        categories: List<Category>
    ): ComparativeAnalysis {
        val monthlyTrends = calculateMonthlyTrends(transactions)
        
        // Generate period comparisons (last 6 months vs previous 6 months)
        val periodComparisons = generatePeriodComparisons(monthlyTrends)
        
        // Generate category comparisons
        val categoryComparisons = generateCategoryComparisons(transactions, categories)
        
        // Calculate spending efficiency metrics
        val efficiencyMetrics = calculateSpendingEfficiencyMetrics(transactions)
        
        return ComparativeAnalysis(
            periodComparisons = periodComparisons,
            categoryComparisons = categoryComparisons,
            spendingEfficiencyMetrics = efficiencyMetrics
        )
    }
    
    private fun generatePeriodComparisons(monthlyTrends: List<MonthlyTrend>): List<PeriodComparison> {
        if (monthlyTrends.size < 6) return emptyList()
        
        val comparisons = mutableListOf<PeriodComparison>()
        
        // Compare last 3 months vs previous 3 months
        val recent3Months = monthlyTrends.takeLast(3)
        val previous3Months = monthlyTrends.dropLast(3).takeLast(3)
        
        if (previous3Months.size == 3) {
            val recentIncome = recent3Months.sumOf { it.monthlyData.income }
            val recentExpenses = recent3Months.sumOf { it.monthlyData.expenses }
            val previousIncome = previous3Months.sumOf { it.monthlyData.income }
            val previousExpenses = previous3Months.sumOf { it.monthlyData.expenses }
            
            comparisons.add(
                PeriodComparison(
                    period1 = "Últimos 3 meses",
                    period2 = "3 meses anteriores",
                    income1 = recentIncome,
                    income2 = previousIncome,
                    expenses1 = recentExpenses,
                    expenses2 = previousExpenses,
                    incomeChange = calculatePercentageChange(previousIncome, recentIncome),
                    expenseChange = calculatePercentageChange(previousExpenses, recentExpenses),
                    balanceChange = calculatePercentageChange(
                        previousIncome - previousExpenses,
                        recentIncome - recentExpenses
                    )
                )
            )
        }
        
        // Compare this month vs last month
        if (monthlyTrends.size >= 2) {
            val thisMonth = monthlyTrends.last()
            val lastMonth = monthlyTrends[monthlyTrends.size - 2]
            
            comparisons.add(
                PeriodComparison(
                    period1 = "Este mes",
                    period2 = "Mes pasado",
                    income1 = thisMonth.monthlyData.income,
                    income2 = lastMonth.monthlyData.income,
                    expenses1 = thisMonth.monthlyData.expenses,
                    expenses2 = lastMonth.monthlyData.expenses,
                    incomeChange = thisMonth.incomeChange,
                    expenseChange = thisMonth.expenseChange,
                    balanceChange = thisMonth.balanceChange
                )
            )
        }
        
        return comparisons
    }
    
    private fun generateCategoryComparisons(
        transactions: List<Transaction>,
        categories: List<Category>
    ): List<CategoryComparison> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        // Get previous month
        calendar.add(Calendar.MONTH, -1)
        val previousMonth = calendar.get(Calendar.MONTH)
        val previousYear = calendar.get(Calendar.YEAR)
        
        val currentMonthSpending = mutableMapOf<Int, Double>()
        val previousMonthSpending = mutableMapOf<Int, Double>()
        
        transactions.filter { it.type == "Gasto" }.forEach { transaction ->
            calendar.timeInMillis = transaction.date
            val transactionMonth = calendar.get(Calendar.MONTH)
            val transactionYear = calendar.get(Calendar.YEAR)
            
            when {
                transactionYear == currentYear && transactionMonth == currentMonth -> {
                    currentMonthSpending[transaction.categoryId] = 
                        (currentMonthSpending[transaction.categoryId] ?: 0.0) + transaction.amount
                }
                transactionYear == previousYear && transactionMonth == previousMonth -> {
                    previousMonthSpending[transaction.categoryId] = 
                        (previousMonthSpending[transaction.categoryId] ?: 0.0) + transaction.amount
                }
            }
        }
        
        val allCategoryIds = (currentMonthSpending.keys + previousMonthSpending.keys).distinct()
        
        val comparisons = allCategoryIds.mapNotNull { categoryId ->
            val category = categories.find { it.id == categoryId } ?: return@mapNotNull null
            val current = currentMonthSpending[categoryId] ?: 0.0
            val previous = previousMonthSpending[categoryId] ?: 0.0
            val change = current - previous
            val changePercentage = calculatePercentageChange(previous, current)
            
            CategoryComparison(
                category = category,
                currentPeriodSpending = current,
                previousPeriodSpending = previous,
                change = change,
                changePercentage = changePercentage,
                rank = 0 // Will be set after sorting
            )
        }.sortedByDescending { it.currentPeriodSpending }
            .mapIndexed { index, comparison ->
                comparison.copy(rank = index + 1)
            }
        
        return comparisons
    }
    
    private fun calculateSpendingEfficiencyMetrics(transactions: List<Transaction>): SpendingEfficiencyMetrics {
        val expenseTransactions = transactions.filter { it.type == "Gasto" }
        
        if (expenseTransactions.isEmpty()) {
            return SpendingEfficiencyMetrics(
                averageTransactionAmount = 0.0,
                transactionFrequency = 0.0,
                categoryDiversification = 0.0,
                spendingConsistency = 0.0
            )
        }
        
        // Average transaction amount
        val averageAmount = expenseTransactions.map { it.amount }.average()
        
        // Transaction frequency (transactions per day)
        val daySpan = if (expenseTransactions.size > 1) {
            val oldestDate = expenseTransactions.minOf { it.date }
            val newestDate = expenseTransactions.maxOf { it.date }
            ((newestDate - oldestDate) / (24 * 60 * 60 * 1000)).coerceAtLeast(1)
        } else 1
        
        val transactionFrequency = expenseTransactions.size.toDouble() / daySpan
        
        // Category diversification (number of unique categories / total categories)
        val uniqueCategories = expenseTransactions.map { it.categoryId }.distinct().size
        val totalPossibleCategories = 10 // Assume 10 as a reasonable number
        val categoryDiversification = uniqueCategories.toDouble() / totalPossibleCategories
        
        // Spending consistency (inverse of coefficient of variation)
        val amounts = expenseTransactions.map { it.amount }
        val standardDeviation = kotlin.math.sqrt(
            amounts.map { (it - averageAmount) * (it - averageAmount) }.average()
        )
        val coefficientOfVariation = if (averageAmount > 0) standardDeviation / averageAmount else 0.0
        val spendingConsistency = (1.0 / (1.0 + coefficientOfVariation)).coerceIn(0.0, 1.0)
        
        return SpendingEfficiencyMetrics(
            averageTransactionAmount = averageAmount,
            transactionFrequency = transactionFrequency,
            categoryDiversification = categoryDiversification,
            spendingConsistency = spendingConsistency
        )
    }
    
    private fun calculatePercentageChange(previous: Double?, current: Double): Double {
        if (previous == null || previous == 0.0) return 0.0
        return ((current - previous) / previous) * 100
    }
}

data class MonthlyData(
    val year: Int,
    val month: Int,
    val income: Double,
    val expenses: Double,
    val transactionCount: Int
) {
    val balance: Double get() = income - expenses
    val monthName: String get() = getMonthName(month)
    
    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "Enero"
            2 -> "Febrero"
            3 -> "Marzo"
            4 -> "Abril"
            5 -> "Mayo"
            6 -> "Junio"
            7 -> "Julio"
            8 -> "Agosto"
            9 -> "Septiembre"
            10 -> "Octubre"
            11 -> "Noviembre"
            12 -> "Diciembre"
            else -> "Mes $month"
        }
    }
}

data class MonthlyTrend(
    val monthlyData: MonthlyData,
    val incomeChange: Double,
    val expenseChange: Double,
    val balanceChange: Double,
    val transactionCountChange: Double
)

data class CategoryTrend(
    val category: Category,
    val currentMonthSpending: Double,
    val previousMonthSpending: Double,
    val averageMonthlySpending: Double,
    val trend: Double,
    val isIncreasing: Boolean,
    val monthlyData: Map<String, Double>
)

data class SpendingPatterns(
    val mostExpensiveDayOfWeek: Int,
    val mostExpensiveHour: Int,
    val mostExpensiveMonth: Int,
    val dayOfWeekSpending: Map<Int, Double>,
    val hourOfDaySpending: Map<Int, Double>,
    val monthlySeasonalSpending: Map<Int, Double>
)

data class FinancialInsight(
    val type: InsightType,
    val title: String,
    val description: String,
    val recommendation: String
)

enum class InsightType {
    INFO,
    TIP,
    WARNING,
    ALERT
}
data c
lass FinancialProjections(
    val nextMonthIncome: Double,
    val nextMonthExpenses: Double,
    val nextMonthBalance: Double,
    val projectedSavings: Double,
    val budgetProjections: List<BudgetProjection>,
    val confidence: Double
)

data class BudgetProjection(
    val budget: BudgetProgress,
    val projectedSpending: Double,
    val projectedPercentage: Float,
    val willExceedBudget: Boolean
)

data class SavingsGoalProjection(
    val targetAmount: Double,
    val targetMonths: Int,
    val monthlyRequiredSavings: Double,
    val currentMonthlySavings: Double,
    val isAchievable: Boolean,
    val monthsToAchieve: Int,
    val recommendedAdjustments: List<String>
)

data class ComparativeAnalysis(
    val periodComparisons: List<PeriodComparison>,
    val categoryComparisons: List<CategoryComparison>,
    val spendingEfficiencyMetrics: SpendingEfficiencyMetrics
)

data class PeriodComparison(
    val period1: String,
    val period2: String,
    val income1: Double,
    val income2: Double,
    val expenses1: Double,
    val expenses2: Double,
    val incomeChange: Double,
    val expenseChange: Double,
    val balanceChange: Double
)

data class CategoryComparison(
    val category: Category,
    val currentPeriodSpending: Double,
    val previousPeriodSpending: Double,
    val change: Double,
    val changePercentage: Double,
    val rank: Int
)

data class SpendingEfficiencyMetrics(
    val averageTransactionAmount: Double,
    val transactionFrequency: Double,
    val categoryDiversification: Double,
    val spendingConsistency: Double
)