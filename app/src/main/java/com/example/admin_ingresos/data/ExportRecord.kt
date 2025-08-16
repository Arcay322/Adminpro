package com.example.admin_ingresos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "export_records")
data class ExportRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val uri: String,
    val type: String,
    val createdAt: Long = System.currentTimeMillis()
)
