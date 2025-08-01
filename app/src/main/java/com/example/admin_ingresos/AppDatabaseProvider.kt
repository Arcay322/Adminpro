package com.example.admin_ingresos

import android.content.Context
import androidx.room.Room
import com.example.admin_ingresos.data.AppDatabase
import com.example.admin_ingresos.data.SampleDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AppDatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "admin_ingresos_db"
            )
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .build()
            
            INSTANCE = instance
            
            // Initialize sample data in background
            CoroutineScope(Dispatchers.IO).launch {
                SampleDataProvider.initializeSampleData(instance)
            }
            
            instance
        }
    }
}
