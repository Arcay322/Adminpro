package com.example.admin_ingresos.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    companion object {
        private const val PREFS_NAME = "admin_ingresos_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_SETUP_COMPLETED = "setup_completed"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_BUDGET_ALERTS_ENABLED = "budget_alerts_enabled"
        private const val KEY_SAMPLE_DATA_IMPORTED = "sample_data_imported"
    }
    
    var isOnboardingCompleted: Boolean
        get() = sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()
    
    var isFirstLaunch: Boolean
        get() = sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()
    
    var themeMode: String
        get() = sharedPreferences.getString(KEY_THEME_MODE, "system") ?: "system"
        set(value) = sharedPreferences.edit().putString(KEY_THEME_MODE, value).apply()
    
    var currency: String
        get() = sharedPreferences.getString(KEY_CURRENCY, "USD") ?: "USD"
        set(value) = sharedPreferences.edit().putString(KEY_CURRENCY, value).apply()
    
    var notificationsEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()
    
    var budgetAlertsEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_BUDGET_ALERTS_ENABLED, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_BUDGET_ALERTS_ENABLED, value).apply()
    
    var currencySymbol: String
        get() = sharedPreferences.getString(KEY_CURRENCY_SYMBOL, "$") ?: "$"
        set(value) = sharedPreferences.edit().putString(KEY_CURRENCY_SYMBOL, value).apply()
    
    var sampleDataImported: Boolean
        get() = sharedPreferences.getBoolean(KEY_SAMPLE_DATA_IMPORTED, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_SAMPLE_DATA_IMPORTED, value).apply()
    
    fun isSetupCompleted(): Boolean {
        return sharedPreferences.getBoolean(KEY_SETUP_COMPLETED, false)
    }
    
    fun setSetupCompleted(completed: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_SETUP_COMPLETED, completed).apply()
    }
    
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}