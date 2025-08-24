package com.example.admin_ingresos.data

import android.content.Context
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyUtils {
    private fun localeForCurrency(code: String): Locale = when (code.uppercase()) {
        "PEN" -> Locale("es", "PE")
        "USD" -> Locale.US
        "EUR" -> Locale("es", "ES")
        "GBP" -> Locale("en", "GB")
        "JPY" -> Locale("ja", "JP")
        "INR" -> Locale("en", "IN")
        "BRL" -> Locale("pt", "BR")
        "MXN" -> Locale("es", "MX")
        "CAD" -> Locale("en", "CA")
        "AUD" -> Locale("en", "AU")
        "CNY" -> Locale("zh", "CN")
        "CHF" -> Locale("de", "CH")
        else -> Locale.getDefault()
    }
    fun getCurrencyFormatter(context: Context): NumberFormat {
        val prefs = PreferencesManager(context)
        val code = prefs.currency.ifBlank { "USD" }
        // Try to choose a Locale that corresponds to the currency so NumberFormat
        // can render the appropriate symbol (for example PEN -> "S/").
        val locale = localeForCurrency(code)

        val formatter = try {
            NumberFormat.getCurrencyInstance(locale)
        } catch (e: Exception) {
            NumberFormat.getCurrencyInstance()
        }

        try {
            formatter.currency = Currency.getInstance(code)
        } catch (e: Exception) {
            // fallback: leave formatter as created
        }
        return formatter
    }

    fun format(amount: Double, context: Context): String {
        val prefs = PreferencesManager(context)
        val formatter = getCurrencyFormatter(context)
        val formatted = try {
            formatter.format(amount)
        } catch (e: Exception) {
            // fallback to plain number
            NumberFormat.getNumberInstance().format(amount)
        }

        // We want the symbol ALWAYS prefixed. Build a locale-aware numeric string then prefix the symbol.
        val code = prefs.currency.ifBlank { "USD" }
        val currency = try { Currency.getInstance(code) } catch (e: Exception) { null }

        // Prefer user-provided symbol if present
        val userSymbol = prefs.currencySymbol
        val symbol = if (!userSymbol.isNullOrBlank()) {
            userSymbol
        } else {
            // try currency symbol for the formatter's locale
            try {
                if (currency != null) currency.getSymbol(Locale.getDefault()) else formatter.currency?.getSymbol(Locale.getDefault()) ?: currency?.currencyCode ?: ""
            } catch (e: Exception) {
                currency?.currencyCode ?: ""
            }
        }

        // Create a number formatter matching the currency formatter's fraction settings but without the currency symbol
        val numberFormatter = try {
            val nf = NumberFormat.getNumberInstance(localeForCurrency(code))
            nf.minimumFractionDigits = formatter.minimumFractionDigits
            nf.maximumFractionDigits = formatter.maximumFractionDigits
            nf
        } catch (e: Exception) {
            val nf = NumberFormat.getNumberInstance()
            nf.minimumFractionDigits = formatter.minimumFractionDigits
            nf.maximumFractionDigits = formatter.maximumFractionDigits
            nf
        }

        val numeric = try { numberFormatter.format(amount) } catch (_: Exception) { amount.toString() }

        // Ensure there's a space between symbol and number for readability (e.g. "S/ 1.234,56")
        return if (symbol.isBlank()) numeric else "$symbol $numeric"
    }
}
