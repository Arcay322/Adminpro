package com.example.admin_ingresos.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Category::class, PaymentMethod::class, Transaction::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun transactionDao(): TransactionDao
    
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add color column to categories table
                database.execSQL("ALTER TABLE categories ADD COLUMN color TEXT NOT NULL DEFAULT '#85C1E9'")
                
                // Add icon column to payment_methods table
                database.execSQL("ALTER TABLE payment_methods ADD COLUMN icon TEXT NOT NULL DEFAULT '💰'")
            }
        }
    }
}
