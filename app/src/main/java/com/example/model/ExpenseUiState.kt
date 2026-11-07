package com.example.model

/**
 * UI State interface for the expense list screen (GP3).
 * Explicitly supports the four required states:
 * 1. Loading indicator
 * 2. Error message with a Retry button
 * 3. Empty message
 * 4. Data state
 */
sealed interface ExpenseUiState {
    data object Loading : ExpenseUiState

    data class Error(val message: String) : ExpenseUiState

    data object Empty : ExpenseUiState

    data class Success(
        val expenses: List<Expense>,
        val totalSpent: Double,
        val categoryBreakdown: List<CategoryBreakdown>
    ) : ExpenseUiState
}
