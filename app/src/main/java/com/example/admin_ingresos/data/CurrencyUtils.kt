package com.example.admin_ingresos.data

import android.content.Context
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyUtils {
    fun getCurrencyFormatter(context: Context): NumberFormat {
        val prefs = PreferencesManager(context)
        val code = prefs.currency.ifBlank { "USD" }
        val formatter = NumberFormat.getCurrencyInstance()
        try {
            formatter.currency = Currency.getInstance(code)
        } catch (e: Exception) {
            // fallback: leave formatter as default
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

        // If user has a custom symbol set in prefs (e.g., "S/"), prefer it.
        val userSymbol = prefs.currencySymbol
        if (!userSymbol.isNullOrBlank()) {
            // Try to replace the formatter's symbol or currency code with the user's symbol.
            val code = prefs.currency.ifBlank { "USD" }
            val currency = try { Currency.getInstance(code) } catch (e: Exception) { null }
            val candidates = mutableListOf<String>()
            currency?.let {
                try {
                    candidates.add(it.getSymbol(Locale.getDefault()))
                } catch (_: Exception) {}
                try { candidates.add(it.currencyCode) } catch (_: Exception) {}
            }

            var result = formatted
            candidates.distinct().forEach { c ->
                if (!c.isNullOrBlank()) result = result.replace(c, userSymbol)
            }

            // If no replacement happened, as a last resort prefix the numeric portion with the user symbol.
            if (result == formatted) {
                // Extract numeric characters and keep formatting (very conservative): remove everything except digits, grouping, decimal and minus
                val numberOnly = NumberFormat.getNumberInstance().format(amount)
                return "$userSymbol$numberOnly"
            }

            return result
        }

        return formatted
    }
}
