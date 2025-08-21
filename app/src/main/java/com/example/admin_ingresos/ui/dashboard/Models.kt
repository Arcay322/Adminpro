package com.example.admin_ingresos.ui.dashboard

data class DayData(
    val day: String,
    val income: Double,
    val expense: Double,
    val transfers: Double = 0.0
)
