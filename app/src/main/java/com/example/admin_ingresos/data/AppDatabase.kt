package com.example.admin_ingresos.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.admin_ingresos.data.model.SavingsGoal
import com.example.admin_ingresos.data.dao.SavingsGoalDao

@Database(
    entities = [Category::class, PaymentMethod::class, Transaction::class, Budget::class, SavingsGoal::class, com.example.admin_ingresos.data.ExportRecord::class],
        version = 14,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun exportRecordDao(): ExportRecordDao
    
    companion object {
        // Migration 1 -> 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE categories ADD COLUMN color TEXT NOT NULL DEFAULT '#85C1E9'")
                database.execSQL("ALTER TABLE payment_methods ADD COLUMN icon TEXT NOT NULL DEFAULT '💰'")
            }
        }

        // Migration 3 -> 4: create savings_goals table
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS savings_goals (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        targetAmount REAL NOT NULL,
                        currentAmount REAL NOT NULL DEFAULT 0.0,
                        emoji TEXT NOT NULL,
                        description TEXT,
                        targetDate INTEGER,
                        createdAt INTEGER NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        priority INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        // Migration 5 -> 6: add receipt photo uri to transactions
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transactions ADD COLUMN receiptPhotoUri TEXT")
            }
        }

        // Migration 6 -> 7: add ordering to categories
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE categories ADD COLUMN `order` INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Migration 11 -> 12: add export_records table
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS export_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        fileName TEXT NOT NULL,
                        uri TEXT NOT NULL,
                        type TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """)
            }
        }

        // Migration 12 -> 13: add goalId to transactions
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add nullable goalId column
                database.execSQL("ALTER TABLE transactions ADD COLUMN goalId INTEGER")
                // Convert existing transactions that are linked to a goal (if any) to the transfer/ahorro type
                // so historical contributions don't keep being counted as expenses.
                // Note: Transaction.TYPE_TRANSFER constant is in Kotlin code; in SQL we use the literal value.
                database.execSQL("UPDATE transactions SET type = 'Transfer' WHERE goalId IS NOT NULL")
            }
        }

        // Migration 13 -> 14: add categoryId to savings_goals so each goal can have its own AHORRO category
        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE savings_goals ADD COLUMN categoryId INTEGER")
                // Existing goals will have null categoryId; UI logic will create per-goal categories on-demand
            }
        }

        // Register all defined migrations here. Omit migrations that aren't declared in this file.
        val ALL_MIGRATIONS = arrayOf(
            MIGRATION_1_2,
            MIGRATION_3_4,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_11_12,
            MIGRATION_12_13,
            MIGRATION_13_14
        )
    }
}