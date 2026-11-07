package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.model.ExpenseCategory

/**
 * Returns the appropriate Material Design icon for each expense category.
 */
fun getCategoryIcon(category: ExpenseCategory): ImageVector {
    return when (category) {
        ExpenseCategory.FOOD -> Icons.Default.Restaurant
        ExpenseCategory.TRANSPORT -> Icons.Default.DirectionsBus
        ExpenseCategory.SHOPPING -> Icons.Default.ShoppingBag
        ExpenseCategory.BILLS -> Icons.Default.ReceiptLong
        ExpenseCategory.HEALTH -> Icons.Default.MedicalServices
        ExpenseCategory.ENTERTAINMENT -> Icons.Default.TheaterComedy
        ExpenseCategory.EDUCATION -> Icons.Default.School
        ExpenseCategory.OTHER -> Icons.Default.MoreHoriz
    }
}
