package com.example.admin_ingresos.ui.profile

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.admin_ingresos.ui.profile.SecurityUtils
import com.example.admin_ingresos.ui.theme.BackgroundStart
import androidx.compose.ui.graphics.toArgb
import com.example.admin_ingresos.ui.theme.AppThemeManager

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

    private val _name = MutableStateFlow(prefs.getString("name", "Usuario") ?: "Usuario")
    val name: StateFlow<String> = _name

    private val _email = MutableStateFlow(prefs.getString("email", "") ?: "")
    val email: StateFlow<String> = _email

    private val _avatarUri = MutableStateFlow(prefs.getString("avatar_uri", null))
    val avatarUri: StateFlow<String?> = _avatarUri

    private val _currency = MutableStateFlow(prefs.getString("currency", "PEN") ?: "PEN")
    val currency: StateFlow<String> = _currency

    private val _darkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val darkMode: StateFlow<Boolean> = _darkMode

    // Additional profile fields
    private val _phone = MutableStateFlow(prefs.getString("phone", "") ?: "")
    val phone: StateFlow<String> = _phone

    private val _bio = MutableStateFlow(prefs.getString("bio", "") ?: "")
    val bio: StateFlow<String> = _bio

    private val _language = MutableStateFlow(prefs.getString("language", "es") ?: "es")
    val language: StateFlow<String> = _language

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications_enabled", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    private val _fingerprintEnabled = MutableStateFlow(prefs.getBoolean("fingerprint_enabled", false))
    val fingerprintEnabled: StateFlow<Boolean> = _fingerprintEnabled

    private val _pinCode = MutableStateFlow(prefs.getString("pin_code", "") ?: "")
    // stored encrypted; expose decrypted when needed via function
    val pinCode: StateFlow<String> = _pinCode

    // Background color preference (stored as ARGB int)
    private val _backgroundColor = MutableStateFlow(prefs.getInt("background_color", BackgroundStart.toArgb()))
    val backgroundColor: StateFlow<Int> = _backgroundColor

    private val _forceLight = MutableStateFlow(prefs.getBoolean("force_light_mode", false))
    val forceLight: StateFlow<Boolean> = _forceLight

    private val PIN_ALIAS = "adminpro_pin_alias"

    fun saveProfile(
        name: String,
        email: String,
        avatarUri: String?,
        currency: String,
        darkMode: Boolean,
        phone: String = _phone.value,
        bio: String = _bio.value,
        language: String = _language.value,
        notificationsEnabled: Boolean = _notificationsEnabled.value,
        fingerprintEnabled: Boolean = _fingerprintEnabled.value,
        pinCode: String = _pinCode.value
    ) {
        prefs.edit()
            .putString("name", name)
            .putString("email", email)
            .putString("avatar_uri", avatarUri)
            .putString("currency", currency)
            .putBoolean("dark_mode", darkMode)
            .putString("phone", phone)
            .putString("bio", bio)
            .putString("language", language)
            .putBoolean("notifications_enabled", notificationsEnabled)
            .putBoolean("fingerprint_enabled", fingerprintEnabled)
            .putString("pin_code", SecurityUtils.encrypt(PIN_ALIAS, pinCode) ?: "")
            .putInt("background_color", _backgroundColor.value)
            .apply()

        _name.value = name
        _email.value = email
        _avatarUri.value = avatarUri
        _currency.value = currency
        _darkMode.value = darkMode
        _phone.value = phone
        _bio.value = bio
        _language.value = language
        _notificationsEnabled.value = notificationsEnabled
        _fingerprintEnabled.value = fingerprintEnabled
    _pinCode.value = SecurityUtils.encrypt(PIN_ALIAS, pinCode) ?: ""
    }

    fun setBackgroundColor(colorInt: Int) {
        prefs.edit().putInt("background_color", colorInt).apply()
        _backgroundColor.value = colorInt
    // Push immediately to the global theme manager so UI updates without restart
    AppThemeManager.setBackgroundColor(colorInt)
    }

    fun setForceLightMode(value: Boolean) {
        prefs.edit().putBoolean("force_light_mode", value).apply()
        _forceLight.value = value
        AppThemeManager.setForceLight(value)
    }

    fun setDarkMode(value: Boolean) {
        // value == true -> dark mode
        prefs.edit().putBoolean("dark_mode", value).apply()
        _darkMode.value = value
        // forceLight should be the inverse of dark mode
        val forceLight = !value
        prefs.edit().putBoolean("force_light_mode", forceLight).apply()
        _forceLight.value = forceLight
        AppThemeManager.setForceLight(forceLight)
    }

    fun signOut() {
        // Clear local profile data (keep as simple sign-out)
        prefs.edit().clear().apply()
        _name.value = "Usuario"
        _email.value = ""
        _avatarUri.value = null
        _currency.value = "PEN"
        _darkMode.value = false
    _phone.value = ""
    _bio.value = ""
    _language.value = "es"
    _notificationsEnabled.value = true
    _fingerprintEnabled.value = false
    _pinCode.value = ""
    _backgroundColor.value = BackgroundStart.toArgb()
    }
}
