package com.example.admin_ingresos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val icon: String = "📦", // emoji icon or icon name
    val color: String = "#85C1E9" // hex color code
)
