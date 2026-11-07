# MoneyBee — Android Expense Tracker

**MoneyBee** is a clean, offline-first Android expense management application designed for college students and personal budget tracking. Built using modern Android architecture with Kotlin and Jetpack Compose, MoneyBee adheres strictly to Material Design 3 guidelines with a custom Teal theme (`0xFF00897B`).

---

## 📱 Project Overview

MoneyBee helps users track their daily spending with an itemized transaction ledger, category analytics, and robust input validation. It runs completely offline with zero API keys or external server dependencies.

### Key Capabilities
- **Four-State List Flow**: Seamlessly handles Loading, Error (with quick Retry), Empty, and Data states.
- **Strict Input Validation**: Validates amount (> ₹0 and ≤ ₹1,00,00,000), required category (8 options), date picker (no future dates allowed), and notes (3–100 characters).
- **Comprehensive Details**: Displays full unabbreviated notes, timestamps, categories, with in-place editing and deletion protected by a confirmation dialog.
- **Spending Summary & Analytics**: Aggregates totals by category sorted highest-to-lowest, with percentages and animated M3 progress indicators.
- **Presenter Demo Mode**: Dedicated toggle in the top bar allows live presentation of the error view and "Retry" recovery mechanism.

---

## 📸 Screenshots

| 1. Expense List (Data & Empty) | 2. Add / Edit Expense Form | 3. Spending Analytics Summary |
|:---:|:---:|:---:|
| ![Expense List](screenshots/expense_list.png) | ![Add Expense Form](screenshots/add_expense.png) | ![Spending Summary](screenshots/spending_summary.png) |
| *Total card, recent transactions, and empty state* | *Validated form fields and date picker* | *Sorted category totals and progress bars* |

---

## 💾 Data Persistence & Architecture

- **Data Source**: On-device persistent storage using Android `SharedPreferences` storing serialized JSON data.
- **Privacy & Offline Guarantee**: 100% offline. Zero network calls, zero third-party telemetry, zero cloud keys.
- **Lifecycle Persistence**: All created, edited, and deleted records survive app restarts and device reboots.

---

## 👥 College Group Project Members

| Member Name | Student ID / Roll No | Role & Contributions |
|---|---|---|
| *Member 1 (Lead)* | *e.g. 248X1A0517* | Architecture, StateFlow ViewModel, and JSON Storage Service |
| *Member 2* | *Student ID* | Expense List Screen (4 states) and Theme (`0xFF00897B`) |
| *Member 3* | *Student ID* | Expense Form Screen with multi-rule validation & DatePicker |
| *Member 4* | *Student ID* | Detail Screen (Edit/Delete confirmation) and Summary Screen |

---

## 🚀 How to Build and Run

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 17 or JDK 21
- Android SDK 34+

### Steps to Run
1. Open the project folder in Android Studio.
2. Allow Gradle sync to complete.
3. Select an emulator or connected physical Android device.
4. Click **Run 'app'** (`Shift + F10`).

### Running Tests
To run local JVM unit and Robolectric tests:
```bash
gradle :app:testDebugUnitTest
```

---

## 📦 Building a Release APK (GP10)

### 1. Build the Release APK
Run the following Gradle command from the root directory:
```bash
gradle :app:assembleRelease
```
The output APK will be located at:
```
app/build/outputs/apk/release/app-release.apk
```

### 2. Install on a Physical Android Phone via ADB
Ensure **USB Debugging** is enabled on your phone (under Developer Options), connect the device via USB cable, and run:
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

## 🎭 Demo Guide: Showing the Error State on Purpose

To demonstrate the **Error State** and **Retry** recovery during college evaluation:
1. Open MoneyBee on the home screen.
2. Tap the **Bug icon (`ic_bug_report`)** in the top-right app bar.
3. The app immediately displays the **Error View** with an alert icon, explaining the simulated error state.
4. Point out the **"Retry"** button to the evaluator.
5. Tap **"Retry"** (or tap the bug icon again) to immediately recover your ledger data.
