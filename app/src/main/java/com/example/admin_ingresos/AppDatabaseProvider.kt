package com.example.admin_ingresos

import android.content.Context
import androidx.room.Room
import com.example.admin_ingresos.data.AppDatabase

object AppDatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "admin_ingresos_db"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}
