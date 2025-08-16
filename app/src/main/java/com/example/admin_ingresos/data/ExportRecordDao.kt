package com.example.admin_ingresos.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExportRecordDao {
    @Insert
    suspend fun insert(record: ExportRecord): Long

    @Query("SELECT * FROM export_records ORDER BY createdAt DESC LIMIT 50")
    suspend fun getRecentExports(): List<ExportRecord>

    @Query("DELETE FROM export_records WHERE id = :id")
    suspend fun deleteById(id: Int): Int
}
