package com.example.model

/**
 * Breakdown data item used on the Summary Screen (GP1, GP4).
 * Represents total spending aggregated for a specific category, sorted highest first.
 */
data class CategoryBreakdown(
    val category: ExpenseCategory,
    val totalAmount: Double,
    val percentage: Float, // value between 0.0f and 1.0f for progress bar
    val transactionCount: Int
) {
    fun formattedPercentage(): String {
        return String.format(java.util.Locale.getDefault(), "%.1f%%", percentage * 100f)
    }

    fun formattedTotal(): String {
        return Expense.formatCurrency(totalAmount)
    }
}
