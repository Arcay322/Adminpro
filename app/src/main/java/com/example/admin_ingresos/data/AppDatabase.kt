package com.example.admin_ingresos.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Category::class, PaymentMethod::class, Transaction::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun transactionDao(): TransactionDao
}
