# PocketLedger — Android Expense Tracker

An offline-first Android expense management app for college students and personal budget tracking, built natively in Kotlin and Jetpack Compose with Material 3. Features local JSON persistence, robust form validation, dynamic category analytics, and state handling.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions were aligned during the interactive clarification:

- **Framework & Runtime**: Native Android with Kotlin and Jetpack Compose running live in the AI Studio browser emulator. (Provides live interactive testing and rapid iteration, adhering to the project's group and grading goals).
- **Currency & Formatting**: Indian Rupee (`₹` INR) formatting with standard locale separators for all totals, breakdowns, and line items.
- **Zero API Keys & Fully Offline**: Zero API keys or cloud secrets. The app operates 100% offline using local device storage with no external network requests.
- **Demo Error State Trigger**: A dedicated toggle icon in the top app bar action menu allows group presenters to switch to a simulated error state on demand to showcase the error view and "Retry" recovery button during live evaluation.
- **Offline Persistence**: Local device persistence using `SharedPreferences` with JSON serialization, ensuring all expense records survive app restarts with zero cloud or network dependencies.

---

## 1. Overview & Core Concept

- **What It Does**: PocketLedger lets users record daily expenditures with an amount, category, date, and descriptive note. It aggregates total spending into a prominent header card, provides an itemized transaction list with 4 distinct UI states (loading, error, empty, data), offers a detail view with edit and delete capabilities, and computes an automated category breakdown with sorted percentages and progress bars on a summary screen.
- **Target Audience / Persona**: College students and individuals tracking personal expenses on Android who require an offline, privacy-preserving, and fast ledger.
- **Key Value**: Simple, clean tracking with validation against bad input, clear navigation, and immediate visual insights into where money is spent.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Expense List (Home Screen)**:
   - Top App Bar with title **PocketLedger**, summary analytics icon (`ic_insights`), and demo mode toggle (`ic_bug_report`).
   - Total Spend Summary Card displaying current aggregate balance in `₹`.
   - Dynamic Content Area presenting one of four states:
     - *Loading State*: Circular progress indicator with "Loading your ledger...".
     - *Empty State*: Illustrated icon, friendly greeting ("No expenses yet!"), and prompt to add your first expense.
     - *Error State*: Alert icon, error description, and a primary "Retry" button.
     - *Data State*: Scrollable list of expenses, each showing category icon/chip, truncated note, formatted date, and amount.
   - Floating Action Button (FAB) labeled "+ Add Expense" anchored to bottom-right with double-tap guard.

2. **Expense Detail Screen**:
   - Reached by tapping any list item.
   - Full-bleed card displaying category badge, prominent amount (`₹`), calendar date, and complete unabbreviated note text.
   - Action buttons for **Edit** (navigates to Form populated with this expense) and **Delete** (triggers M3 Confirmation Dialog: "Are you sure you want to delete this expense?").
   - Up-navigation back to the list screen.

3. **Add/Edit Expense Form Screen**:
   - Accessible via FAB (create) or Detail screen (edit mode).
   - Validated fields:
     - **Amount**: Numerical keyboard, validates required, parseable positive number, reasonable upper bound (max ₹10,000,000).
     - **Category**: Dropdown/Selector with 8 categories (Food, Transport, Shopping, Bills, Health, Entertainment, Education, Other).
     - **Date Picker**: M3 DatePicker dialog preventing future dates.
     - **Note**: Outlined text field enforcing 3 to 100 character length with character counter.
   - Primary action button ("Save Expense" / "Update Expense") with double-tap debouncing.

4. **Summary Screen**:
   - Reached via Top App Bar button.
   - Grand total expenditure banner.
   - Sorted list of categories (highest spending first).
   - Each category row includes category icon, category name, total amount spent (`₹`), percentage of total, and a smooth M3 linear progress bar.

### Visual Identity & Theme
- **Color Theme**: Material 3 generated from single seed color **Teal (`0xFF00897B`)** defined in `Theme.kt`. Consistent primary, secondary, surface, and tonal container tokens.
- **Typography**: Clean, readable sans-serif hierarchy with `TitleLarge`, `HeadlineMedium`, and `BodyMedium` using standard scalable `sp` units.
- **Shapes & Elevation**: M3 medium rounded corners (`16.dp` for cards, `12.dp` for input fields, `28.dp` for FAB).
- **Responsive Layout**: Scrollable columns with `verticalScroll` ensuring zero clipping when keyboard is open or phone is rotated horizontally.

---

## 3. Key Product Decisions & Trade-Offs

- **State Management (MVVM with StateFlow)**:
  - *Chosen Approach*: `ExpenseViewModel` managing a single `ExpenseUiState` sealed interface (`Loading`, `Error`, `Empty`, `Success`) exposed via `StateFlow` and observed with `collectAsStateWithLifecycle()`.
  - *Why*: Direct native equivalent to Flutter's `ChangeNotifier`/`Provider`, offering lifecycle awareness, clear separation of concerns, and clean testing without race conditions.
- **Local Persistence via SharedPreferences & JSON**:
  - *Chosen Approach*: `ExpenseStorageService` serializing a list of `Expense` data objects into JSON string using Kotlin standard libraries / Moshi, stored in Android `SharedPreferences`.
  - *Why*: Matches GP2 explicit offline persistence requirement, is easily inspectable, survives app restarts, and has zero external server dependencies.
- **Double-Tap & Rapid Input Guards**:
  - *Chosen Approach*: Button click debouncing and atomic state transition flags in ViewModel to prevent accidental duplicate saves or concurrent navigations.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────┐
│                      UI Layer                          │
│                                                        │
│  ┌──────────────────┐           ┌───────────────────┐  │
│  │ ExpenseListScreen│◄─────────►│ ExpenseDetailView │  │
│  └────────┬─────────┘           └─────────┬─────────┘  │
│           │                               │            │
│  ┌────────▼─────────┐           ┌─────────▼─────────┐  │
│  │ ExpenseFormScreen│           │   SummaryScreen   │  │
│  └────────┬─────────┘           └─────────┬─────────┘  │
└───────────┼───────────────────────────────┼────────────┘
            │                               │
            ▼                               ▼
┌────────────────────────────────────────────────────────┐
│             ExpenseViewModel (StateFlow)               │
│  - uiState: StateFlow<ExpenseUiState>                  │
│  - addExpense(), updateExpense(), deleteExpense()      │
│  - toggleDemoError(), retryLoading()                   │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│               ExpenseStorageService                    │
│  - loadExpenses(): List<Expense>                       │
│  - saveExpenses(list: List<Expense>)                   │
│  - Android SharedPreferences (JSON storage)            │
└────────────────────────────────────────────────────────┘
```

### Data Models & State Structure

```kotlin
data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val category: ExpenseCategory,
    val dateEpochMillis: Long,
    val note: String
)

enum class ExpenseCategory(val displayName: String, val iconRes: ImageVector) {
    FOOD("Food", Icons.Default.Restaurant),
    TRANSPORT("Transport", Icons.Default.DirectionsBus),
    SHOPPING("Shopping", Icons.Default.ShoppingBag),
    BILLS("Bills", Icons.Default.ReceiptLong),
    HEALTH("Health", Icons.Default.MedicalServices),
    ENTERTAINMENT("Entertainment", Icons.Default.TheaterComedy),
    EDUCATION("Education", Icons.Default.School),
    OTHER("Other", Icons.Default.MoreHoriz)
}

sealed interface ExpenseUiState {
    data object Loading : ExpenseUiState
    data class Error(val message: String) : ExpenseUiState
    data object Empty : ExpenseUiState
    data class Success(
        val expenses: List<Expense>,
        val totalSpent: Double,
        val categoryBreakdown: List<CategorySpend>
    ) : ExpenseUiState
}
```

### Project Documentation & Deliverables

1. **`README.md`**: Complete project overview, topic, architecture, screenshots placeholders, data persistence details, build and run instructions, and team contribution table.
2. **`DEFECT_LOG.md`**: Formatted defect tracking table with columns (`Defect ID`, `Description`, `Steps to Reproduce`, `Severity`, `Fixed (Yes/No)`).
3. **`ExampleRobolectricTest.kt`**: Local JVM tests verifying JSON serialization, validation rules, category summation, and state transitions via `gradle :app:testDebugUnitTest`.
4. **Demo Presentation Guide**: Step-by-step instructions on demonstrating the 4 UI states, validation errors, and edit/delete flows during project evaluation.
