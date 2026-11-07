package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.Expense
import com.example.model.ExpenseCategory
import com.example.ui.getCategoryIcon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Add / Edit Expense Form Screen (GP1, GP4).
 * Enforces strict input validation:
 * - Amount: required, valid positive number, not absurdly large (<= 10,000,000)
 * - Category: required selection from the 8 allowed categories
 * - Date: required, date picker, not in the future
 * - Note: required, 3 to 100 characters
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseFormScreen(
    existingExpense: Expense?,
    onSaveExpense: (amount: Double, category: ExpenseCategory, dateEpochMillis: Long, note: String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditMode = existingExpense != null

    // Form fields
    var amountText by remember {
        mutableStateOf(existingExpense?.amount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "")
    }
    var selectedCategory by remember {
        mutableStateOf(existingExpense?.category ?: ExpenseCategory.FOOD)
    }
    var selectedDateMillis by remember {
        mutableLongStateOf(existingExpense?.dateEpochMillis ?: System.currentTimeMillis())
    }
    var noteText by remember {
        mutableStateOf(existingExpense?.note ?: "")
    }

    // Validation error states
    var amountError by remember { mutableStateOf<String?>(null) }
    var noteError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }

    // UI State for dropdown & date picker
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Format selected date
    val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }
    val formattedDisplayDate = remember(selectedDateMillis) {
        dateFormatter.format(Date(selectedDateMillis))
    }

    // Date Picker Dialog
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val picked = datePickerState.selectedDateMillis
                        if (picked != null) {
                            val todayEnd = System.currentTimeMillis() + 86400000L
                            if (picked > todayEnd) {
                                dateError = "Expense date cannot be in the future"
                            } else {
                                selectedDateMillis = picked
                                dateError = null
                            }
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Full Form Validator
    fun validate(): Boolean {
        var isValid = true

        // 1. Amount validation
        val trimmedAmount = amountText.trim()
        val parsedAmount = trimmedAmount.toDoubleOrNull()
        when {
            trimmedAmount.isEmpty() -> {
                amountError = "Amount is required"
                isValid = false
            }
            parsedAmount == null || parsedAmount <= 0.0 -> {
                amountError = "Enter a valid amount greater than ₹0"
                isValid = false
            }
            parsedAmount > 10_000_000.0 -> {
                amountError = "Amount must not exceed ₹1,00,00,000"
                isValid = false
            }
            else -> {
                amountError = null
            }
        }

        // 2. Date validation (must not be in the future)
        val todayEnd = System.currentTimeMillis() + 86400000L
        if (selectedDateMillis > todayEnd) {
            dateError = "Date cannot be in the future"
            isValid = false
        } else {
            dateError = null
        }

        // 3. Note validation (3 to 100 characters)
        val trimmedNote = noteText.trim()
        when {
            trimmedNote.isEmpty() -> {
                noteError = "Note is required (3 to 100 characters)"
                isValid = false
            }
            trimmedNote.length < 3 -> {
                noteError = "Note must be at least 3 characters (currently ${trimmedNote.length})"
                isValid = false
            }
            trimmedNote.length > 100 -> {
                noteError = "Note must not exceed 100 characters (currently ${trimmedNote.length})"
                isValid = false
            }
            else -> {
                noteError = null
            }
        }

        return isValid
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit Expense" else "Add Expense",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Amount Input Field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        // Clean input allowing digits and one dot
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amountText = input
                            if (amountError != null) amountError = null
                        }
                    },
                    label = { Text("Amount (₹) *") },
                    placeholder = { Text("0.00") },
                    prefix = {
                        Text(
                            text = "₹ ",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = amountError != null,
                    supportingText = {
                        if (amountError != null) {
                            Text(
                                text = amountError!!,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.testTag("amount_error")
                            )
                        } else {
                            Text("Required, positive number up to ₹1,00,00,000")
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amount_input")
                )
            }

            // Category Selection Dropdown
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ExposedDropdownMenuBox(
                    expanded = isCategoryDropdownExpanded,
                    onExpandedChange = { isCategoryDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category *") },
                        leadingIcon = {
                            Icon(
                                imageVector = getCategoryIcon(selectedCategory),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .testTag("category_dropdown")
                    )

                    ExposedDropdownMenu(
                        expanded = isCategoryDropdownExpanded,
                        onDismissRequest = { isCategoryDropdownExpanded = false }
                    ) {
                        ExpenseCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = getCategoryIcon(category),
                                            contentDescription = null,
                                            tint = if (category == selectedCategory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = category.displayName,
                                            fontWeight = if (category == selectedCategory) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    selectedCategory = category
                                    isCategoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Date Picker Field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = formattedDisplayDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date *") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    supportingText = {
                        if (dateError != null) {
                            Text(
                                text = dateError!!,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.testTag("date_error")
                            )
                        } else {
                            Text("Tap to pick date (cannot be in the future)")
                        }
                    },
                    isError = dateError != null,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePickerDialog = true }
                        .testTag("date_picker_button"),
                    enabled = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // Note Input Field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = {
                        if (it.length <= 100) {
                            noteText = it
                            if (noteError != null) noteError = null
                        }
                    },
                    label = { Text("Note / Description *") },
                    placeholder = { Text("e.g. Lunch with project team") },
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (noteError != null) {
                                Text(
                                    text = noteError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.testTag("note_error")
                                )
                            } else {
                                Text("3 to 100 characters required")
                            }
                            Text(
                                text = "${noteText.trim().length}/100",
                                color = if (noteText.trim().length in 3..100) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    isError = noteError != null,
                    minLines = 3,
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_input")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save / Update Action Button (guarded against rapid double-clicks)
            Button(
                onClick = {
                    if (!isSaving && validate()) {
                        isSaving = true
                        val amount = amountText.trim().toDouble()
                        onSaveExpense(
                            amount,
                            selectedCategory,
                            selectedDateMillis,
                            noteText.trim()
                        )
                    }
                },
                enabled = !isSaving,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_expense_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditMode) "Update Expense" else "Save Expense",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
