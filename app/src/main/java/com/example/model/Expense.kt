package com.example.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Expense domain model representing an itemized expenditure.
 * Persisted locally on device as JSON in SharedPreferences (GP2).
 */
data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val category: ExpenseCategory,
    val dateEpochMillis: Long,
    val note: String
) {
    /**
     * Formats the amount in Indian Rupee format (e.g. ₹1,250.00).
     */
    fun formattedAmount(): String {
        return formatCurrency(amount)
    }

    /**
     * Formats the date for list items (e.g. "07 Nov 2026").
     */
    fun formattedShortDate(): String {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return formatter.format(Date(dateEpochMillis))
    }

    /**
     * Formats the full date for the detail screen (e.g. "Saturday, 07 November 2026").
     */
    fun formattedFullDate(): String {
        val formatter = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        return formatter.format(Date(dateEpochMillis))
    }

    companion object {
        fun formatCurrency(value: Double): String {
            val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            return try {
                format.format(value)
            } catch (e: Exception) {
                "₹" + String.format(Locale.getDefault(), "%,.2f", value)
            }
        }
    }
}
