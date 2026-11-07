package com.example.model

/**
 * Required expense categories per project specification:
 * Food, Transport, Shopping, Bills, Health, Entertainment, Education, Other
 */
enum class ExpenseCategory(val displayName: String) {
    FOOD("Food"),
    TRANSPORT("Transport"),
    SHOPPING("Shopping"),
    BILLS("Bills"),
    HEALTH("Health"),
    ENTERTAINMENT("Entertainment"),
    EDUCATION("Education"),
    OTHER("Other");

    companion object {
        fun fromString(value: String): ExpenseCategory {
            return entries.firstOrNull { 
                it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true)
            } ?: OTHER
        }
    }
}
