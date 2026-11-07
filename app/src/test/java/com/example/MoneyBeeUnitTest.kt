package com.example

import com.example.model.Expense
import com.example.model.ExpenseCategory
import com.example.viewmodel.ExpenseViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Local JVM Unit Tests for MoneyBee (GP11).
 * Tests business logic, currency formatting, category aggregation, and validation.
 */
class MoneyBeeUnitTest {

    @Test
    fun testCurrencyFormattingContainsRupeeSymbol() {
        val expense = Expense(
            amount = 1250.50,
            category = ExpenseCategory.FOOD,
            dateEpochMillis = 1700000000000L,
            note = "Team celebration lunch"
        )
        val formatted = expense.formattedAmount()
        assertTrue("Formatted amount should contain Rupee symbol", formatted.contains("₹"))
    }

    @Test
    fun testAllEightCategoriesExist() {
        val expectedCategories = listOf(
            "Food", "Transport", "Shopping", "Bills",
            "Health", "Entertainment", "Education", "Other"
        )
        val actualDisplayNames = ExpenseCategory.entries.map { it.displayName }
        assertEquals(expectedCategories.size, actualDisplayNames.size)
        assertTrue(actualDisplayNames.containsAll(expectedCategories))
    }

    @Test
    fun testCategoryFromStringFallback() {
        assertEquals(ExpenseCategory.FOOD, ExpenseCategory.fromString("food"))
        assertEquals(ExpenseCategory.TRANSPORT, ExpenseCategory.fromString("Transport"))
        assertEquals(ExpenseCategory.OTHER, ExpenseCategory.fromString("NonExistentCategory"))
    }

    @Test
    fun testCategoryBreakdownComputationAndHighestSorting() {
        val expenses = listOf(
            Expense(amount = 200.0, category = ExpenseCategory.FOOD, dateEpochMillis = 1L, note = "Snacks"),
            Expense(amount = 800.0, category = ExpenseCategory.FOOD, dateEpochMillis = 2L, note = "Groceries"),
            Expense(amount = 500.0, category = ExpenseCategory.TRANSPORT, dateEpochMillis = 3L, note = "Cab"),
            Expense(amount = 500.0, category = ExpenseCategory.SHOPPING, dateEpochMillis = 4L, note = "Shoes")
        )

        val totalSpent = 2000.0
        val breakdowns = ExpenseViewModel.computeCategoryBreakdown(expenses, totalSpent)

        assertEquals(3, breakdowns.size)
        // First must be Food (1000 = 50%)
        assertEquals(ExpenseCategory.FOOD, breakdowns[0].category)
        assertEquals(1000.0, breakdowns[0].totalAmount, 0.01)
        assertEquals(0.50f, breakdowns[0].percentage, 0.01f)
        assertEquals(2, breakdowns[0].transactionCount)
        assertEquals("50.0%", breakdowns[0].formattedPercentage())

        // Next are Transport and Shopping (500 each = 25%)
        assertEquals(500.0, breakdowns[1].totalAmount, 0.01)
        assertEquals(0.25f, breakdowns[1].percentage, 0.01f)
        assertEquals(500.0, breakdowns[2].totalAmount, 0.01)
        assertEquals(0.25f, breakdowns[2].percentage, 0.01f)
    }

    @Test
    fun testCategoryBreakdownEmptyList() {
        val breakdowns = ExpenseViewModel.computeCategoryBreakdown(emptyList(), 0.0)
        assertTrue(breakdowns.isEmpty())
    }

    @Test
    fun testExpenseShortAndFullDateFormatting() {
        val expense = Expense(
            amount = 100.0,
            category = ExpenseCategory.BILLS,
            dateEpochMillis = 1700000000000L, // Nov 14, 2023
            note = "WiFi bill"
        )
        assertNotNull(expense.formattedShortDate())
        assertNotNull(expense.formattedFullDate())
        assertTrue(expense.formattedShortDate().isNotEmpty())
        assertTrue(expense.formattedFullDate().isNotEmpty())
    }
}
