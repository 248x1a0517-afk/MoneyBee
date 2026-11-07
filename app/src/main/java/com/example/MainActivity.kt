package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ExpenseUiState
import com.example.service.ExpenseStorageService
import com.example.ui.screens.ExpenseDetailScreen
import com.example.ui.screens.ExpenseFormScreen
import com.example.ui.screens.ExpenseListScreen
import com.example.ui.screens.ExpenseSummaryScreen
import com.example.ui.theme.MoneyBeeTheme
import com.example.viewmodel.ExpenseViewModel

/**
 * Screen navigation routes for MoneyBee.
 */
sealed interface Screen {
    data object List : Screen
    data class Detail(val expenseId: String) : Screen
    data class Form(val expenseId: String? = null) : Screen
    data object Summary : Screen
}

class MainActivity : ComponentActivity() {

    private val viewModel: ExpenseViewModel by viewModels {
        val storageService = ExpenseStorageService(applicationContext)
        ExpenseViewModel.provideFactory(storageService)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MoneyBeeTheme {
                MoneyBeeApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MoneyBeeApp(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }

    when (val screen = currentScreen) {
        is Screen.List -> {
            ExpenseListScreen(
                uiState = uiState,
                isDemoErrorActive = viewModel.isDemoErrorEnabled(),
                onAddExpenseClick = { currentScreen = Screen.Form(null) },
                onExpenseClick = { expense -> currentScreen = Screen.Detail(expense.id) },
                onSummaryClick = { currentScreen = Screen.Summary },
                onToggleDemoError = { viewModel.toggleDemoError() },
                onRetryClick = { viewModel.retry() },
                modifier = modifier.fillMaxSize()
            )
        }

        is Screen.Detail -> {
            BackHandler {
                currentScreen = Screen.List
            }
            val expense = viewModel.getExpense(screen.expenseId)
            ExpenseDetailScreen(
                expense = expense,
                onBackClick = { currentScreen = Screen.List },
                onEditClick = { expenseId -> currentScreen = Screen.Form(expenseId) },
                onDeleteClick = { expenseId ->
                    viewModel.deleteExpense(expenseId) {
                        currentScreen = Screen.List
                    }
                },
                modifier = modifier.fillMaxSize()
            )
        }

        is Screen.Form -> {
            BackHandler {
                currentScreen = if (screen.expenseId != null) {
                    Screen.Detail(screen.expenseId)
                } else {
                    Screen.List
                }
            }
            val existingExpense = screen.expenseId?.let { viewModel.getExpense(it) }
            ExpenseFormScreen(
                existingExpense = existingExpense,
                onBackClick = {
                    currentScreen = if (screen.expenseId != null) {
                        Screen.Detail(screen.expenseId)
                    } else {
                        Screen.List
                    }
                },
                onSaveExpense = { amount, category, dateEpochMillis, note ->
                    if (screen.expenseId != null) {
                        viewModel.updateExpense(
                            id = screen.expenseId,
                            amount = amount,
                            category = category,
                            dateEpochMillis = dateEpochMillis,
                            note = note
                        ) {
                            currentScreen = Screen.Detail(screen.expenseId)
                        }
                    } else {
                        viewModel.addExpense(
                            amount = amount,
                            category = category,
                            dateEpochMillis = dateEpochMillis,
                            note = note
                        ) {
                            currentScreen = Screen.List
                        }
                    }
                },
                modifier = modifier.fillMaxSize()
            )
        }

        is Screen.Summary -> {
            BackHandler {
                currentScreen = Screen.List
            }
            val (totalSpent, breakdown) = when (val state = uiState) {
                is ExpenseUiState.Success -> Pair(state.totalSpent, state.categoryBreakdown)
                else -> Pair(0.0, emptyList())
            }
            ExpenseSummaryScreen(
                totalSpent = totalSpent,
                categoryBreakdowns = breakdown,
                onBackClick = { currentScreen = Screen.List },
                modifier = modifier.fillMaxSize()
            )
        }
    }
}
