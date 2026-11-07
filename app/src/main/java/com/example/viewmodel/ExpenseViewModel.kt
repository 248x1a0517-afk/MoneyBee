package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.model.CategoryBreakdown
import com.example.model.Expense
import com.example.model.ExpenseCategory
import com.example.model.ExpenseUiState
import com.example.service.ExpenseStorageService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Shared state manager for MoneyBee (GP6).
 * Holds expenses and coordinates loading, adding, updating, and deleting records
 * across the List, Detail, Form, and Summary screens.
 */
class ExpenseViewModel(
    private val storageService: ExpenseStorageService
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExpenseUiState>(ExpenseUiState.Loading)
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    private var _cachedExpenses = mutableListOf<Expense>()
    
    // Toggleable demo error state for presentation evaluation (as agreed in clarification)
    private var isDemoErrorActive = false

    // Rapid double-tap guard
    private var isSavingInProgress = false

    init {
        loadExpenses()
    }

    /**
     * Loads saved expenses from SharedPreferences JSON storage.
     * Maps to one of the 4 required UI states (GP3):
     * Loading -> Error / Empty / Success
     */
    fun loadExpenses() {
        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading
            // Brief micro-delay to allow visual state transition if needed
            delay(150)

            if (isDemoErrorActive) {
                _uiState.value = ExpenseUiState.Error(
                    "Simulated error state for presentation. Tap 'Retry' to recover your ledger."
                )
                return@launch
            }

            try {
                val loaded = storageService.loadExpenses()
                _cachedExpenses.clear()
                _cachedExpenses.addAll(loaded)

                if (_cachedExpenses.isEmpty()) {
                    _uiState.value = ExpenseUiState.Empty
                } else {
                    updateSuccessState()
                }
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(
                    e.localizedMessage ?: "Failed to read expenses from storage."
                )
            }
        }
    }

    /**
     * Recovers from error state (e.g. user taps the "Retry" button).
     */
    fun retry() {
        isDemoErrorActive = false
        loadExpenses()
    }

    /**
     * Toggles demo error mode so the team can deliberately showcase the error state during project evaluation.
     */
    fun toggleDemoError() {
        isDemoErrorActive = !isDemoErrorActive
        if (isDemoErrorActive) {
            _uiState.value = ExpenseUiState.Error(
                "Simulated error state for presentation. Tap 'Retry' to recover your ledger."
            )
        } else {
            loadExpenses()
        }
    }

    fun isDemoErrorEnabled(): Boolean = isDemoErrorActive

    /**
     * Adds a newly validated expense. Guarded against rapid double-tap.
     */
    fun addExpense(
        amount: Double,
        category: ExpenseCategory,
        dateEpochMillis: Long,
        note: String,
        onSuccess: () -> Unit
    ) {
        if (isSavingInProgress) return
        isSavingInProgress = true

        val newExpense = Expense(
            amount = amount,
            category = category,
            dateEpochMillis = dateEpochMillis,
            note = note.trim()
        )

        _cachedExpenses.add(0, newExpense)
        // Sort descending by date
        _cachedExpenses.sortByDescending { it.dateEpochMillis }
        storageService.saveExpenses(_cachedExpenses)

        updateSuccessState()
        isSavingInProgress = false
        onSuccess()
    }

    /**
     * Updates an existing expense.
     */
    fun updateExpense(
        id: String,
        amount: Double,
        category: ExpenseCategory,
        dateEpochMillis: Long,
        note: String,
        onSuccess: () -> Unit
    ) {
        if (isSavingInProgress) return
        isSavingInProgress = true

        val index = _cachedExpenses.indexOfFirst { it.id == id }
        if (index != -1) {
            _cachedExpenses[index] = Expense(
                id = id,
                amount = amount,
                category = category,
                dateEpochMillis = dateEpochMillis,
                note = note.trim()
            )
            _cachedExpenses.sortByDescending { it.dateEpochMillis }
            storageService.saveExpenses(_cachedExpenses)
            updateSuccessState()
        }

        isSavingInProgress = false
        onSuccess()
    }

    /**
     * Deletes an expense by its unique ID.
     */
    fun deleteExpense(id: String, onDeleted: () -> Unit) {
        _cachedExpenses.removeAll { it.id == id }
        storageService.saveExpenses(_cachedExpenses)

        if (_cachedExpenses.isEmpty()) {
            _uiState.value = ExpenseUiState.Empty
        } else {
            updateSuccessState()
        }
        onDeleted()
    }

    /**
     * Retrieves an expense by ID for the detail or edit screen.
     */
    fun getExpense(id: String): Expense? {
        return _cachedExpenses.firstOrNull { it.id == id }
    }

    private fun updateSuccessState() {
        val totalSpent = _cachedExpenses.sumOf { it.amount }
        val categoryBreakdowns = computeCategoryBreakdown(_cachedExpenses, totalSpent)
        _uiState.value = ExpenseUiState.Success(
            expenses = _cachedExpenses.toList(),
            totalSpent = totalSpent,
            categoryBreakdown = categoryBreakdowns
        )
    }

    companion object {
        /**
         * Pure function to calculate category breakdown sorted highest first (GP4 / Summary Screen).
         */
        fun computeCategoryBreakdown(
            expenses: List<Expense>,
            totalSpent: Double
        ): List<CategoryBreakdown> {
            if (expenses.isEmpty() || totalSpent <= 0.0) return emptyList()

            return expenses.groupBy { it.category }
                .map { (cat, items) ->
                    val sum = items.sumOf { it.amount }
                    val fraction = (sum / totalSpent).toFloat().coerceIn(0f, 1f)
                    CategoryBreakdown(
                        category = cat,
                        totalAmount = sum,
                        percentage = fraction,
                        transactionCount = items.size
                    )
                }
                .sortedByDescending { it.totalAmount }
        }

        /**
         * Factory for creating instances of ExpenseViewModel.
         */
        fun provideFactory(
            storageService: ExpenseStorageService
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ExpenseViewModel(storageService) as T
            }
        }
    }
}
