package com.example.service

import android.content.Context
import android.content.SharedPreferences
import com.example.model.Expense
import com.example.model.ExpenseCategory
import org.json.JSONArray
import org.json.JSONObject

/**
 * Service responsible for persisting and loading expense records
 * on-device using Android SharedPreferences as JSON strings (GP2).
 * Fully offline, zero external dependencies, survives app restart.
 */
class ExpenseStorageService(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Loads all saved expenses from SharedPreferences.
     * Returns an empty list if no expenses have been saved yet (no hardcoded sample data).
     */
    fun loadExpenses(): List<Expense> {
        val jsonString = sharedPreferences.getString(KEY_EXPENSES, null) ?: return emptyList()
        return deserializeExpenses(jsonString)
    }

    /**
     * Persists the given list of expenses to SharedPreferences as a JSON array string.
     */
    fun saveExpenses(expenses: List<Expense>): Boolean {
        val jsonString = serializeExpenses(expenses)
        return sharedPreferences.edit()
            .putString(KEY_EXPENSES, jsonString)
            .commit()
    }

    /**
     * Clears all stored expenses (useful for testing or full reset).
     */
    fun clearAll(): Boolean {
        return sharedPreferences.edit().remove(KEY_EXPENSES).commit()
    }

    /**
     * Serializes a list of Expense objects into a JSON array string.
     */
    fun serializeExpenses(expenses: List<Expense>): String {
        val jsonArray = JSONArray()
        for (expense in expenses) {
            val jsonObject = JSONObject().apply {
                put("id", expense.id)
                put("amount", expense.amount)
                put("category", expense.category.name)
                put("dateEpochMillis", expense.dateEpochMillis)
                put("note", expense.note)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    /**
     * Deserializes a JSON array string into a list of Expense objects.
     * Safely ignores corrupt individual records instead of crashing.
     */
    fun deserializeExpenses(jsonString: String): List<Expense> {
        val list = mutableListOf<Expense>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                val id = obj.optString("id", "")
                val amount = obj.optDouble("amount", 0.0)
                val categoryName = obj.optString("category", "OTHER")
                val dateEpochMillis = obj.optLong("dateEpochMillis", System.currentTimeMillis())
                val note = obj.optString("note", "")

                if (id.isNotEmpty() && amount > 0) {
                    list.add(
                        Expense(
                            id = id,
                            amount = amount,
                            category = ExpenseCategory.fromString(categoryName),
                            dateEpochMillis = dateEpochMillis,
                            note = note
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // In case of parsing error, return whatever could be recovered
        }
        // Return sorted by date (newest first)
        return list.sortedByDescending { it.dateEpochMillis }
    }

    companion object {
        private const val PREFS_NAME = "moneybee_expenses_prefs"
        private const val KEY_EXPENSES = "expenses_json_data"
    }
}
