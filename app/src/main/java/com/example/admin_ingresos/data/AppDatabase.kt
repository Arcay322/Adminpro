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
    entities = [Category::class, PaymentMethod::class, Transaction::class, Budget::class, SavingsGoal::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add color column to categories table
                database.execSQL("ALTER TABLE categories ADD COLUMN color TEXT NOT NULL DEFAULT '#85C1E9'")
                
                // Add icon column to payment_methods table
                database.execSQL("ALTER TABLE payment_methods ADD COLUMN icon TEXT NOT NULL DEFAULT '💰'")
            }
        }
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create budgets table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS budgets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        categoryId INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        period TEXT NOT NULL,
                        startDate INTEGER NOT NULL,
                        endDate INTEGER NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE CASCADE
                    )
                """)
                
                // Create index for categoryId
                database.execSQL("CREATE INDEX IF NOT EXISTS index_budgets_categoryId ON budgets(categoryId)")
            }
        }
        
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create savings_goals table
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
    }
}
