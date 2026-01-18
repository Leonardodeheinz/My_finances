# MyFinances Application

## Introduction

MyFinances is an Android application for personal finance management. It provides tracking of income, expenses, budgets, debts, and contracts with real-time cloud synchronization via Firebase. Built with Kotlin and Jetpack Compose following modern Android development practices.

## Features

- **Transaction Management**: Track income and expenses with category assignment
- **Budget Tracking**: Set budgets (active/upcoming) and monitor spending with visual progress bars
- **Debt Management**: Track debts with payment schedules, progress tracking, and auto-transaction creation
- **Contract Tracking**: Manage recurring contracts and subscriptions
- **User Authentication**: Google Sign-In integration
- **Filtered Views**: Filter transactions by date range and categories
- **Push Notifications**: Payment reminders via WorkManager
- **Data Export**: Export financial data as PDF or CSV

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + StateFlow |
| DI | Dagger Hilt |
| Backend | Firebase (Firestore, Auth, FCM, Storage) |
| Background | WorkManager |
| Min SDK | API 24 |

## Architecture

The app follows **MVVM (Model-View-ViewModel)** with clean architecture principles:

```
┌────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                      │
│  ┌────────────┐  ┌────────────┐  ┌────────────────────┐   │
│  │  Screens   │──│ ViewModels │──│ UiState (StateFlow)│   │
│  │ (Compose)  │  │  (@Hilt)   │  │                    │   │
│  └────────────┘  └─────┬──────┘  └────────────────────┘   │
└────────────────────────┼───────────────────────────────────┘
                         │
┌────────────────────────┼───────────────────────────────────┐
│                    DATA LAYER                              │
│  ┌─────────────────────┴─────────────────────────────┐    │
│  │                 REPOSITORIES                       │    │
│  │  Transaction │ Budget │ Debt │ Contract │ Category │    │
│  │         (all extend BaseRepository)                │    │
│  └─────────────────────┬─────────────────────────────┘    │
│                        │ Flow<AuthResult<T>>               │
│  ┌─────────────────────┴─────────────────────────────┐    │
│  │   DATA MODELS: Transaction, Budget, Debt, etc.    │    │
│  └───────────────────────────────────────────────────┘    │
└────────────────────────┼───────────────────────────────────┘
                         │
┌────────────────────────┼───────────────────────────────────┐
│                 FIREBASE BACKEND                           │
│     Firestore │ Auth │ Cloud Messaging │ Storage           │
└────────────────────────────────────────────────────────────┘
```

**Layer Responsibilities:**

1. **Presentation Layer**: Jetpack Compose screens observe ViewModels via StateFlow. User interactions trigger ViewModel methods which update the UI state reactively.

2. **Data Layer**: Repositories abstract all Firebase operations. Each repository extends `BaseRepository` providing standardized CRUD operations. Data flows through `Flow<AuthResult<T>>` enabling Loading/Success/Error state handling.

3. **Firebase Backend**: Firestore handles data persistence with real-time sync via snapshot listeners (`observeAll()`). Firebase Auth manages user authentication with Google Sign-In support.

**Data Flow:**
1. User interacts with Compose UI
2. Screen calls ViewModel method
3. ViewModel invokes repository function
4. Repository performs Firebase operation and emits result via Flow
5. ViewModel updates UiState, triggering UI recomposition

## Directory Structure

```
app/src/main/kotlin/com/example/my_finances/
├── data/
│   ├── model/           # Data classes (Transaction, Budget, Debt, Contract, Category)
│   └── repository/      # Repository interfaces and Firebase implementations
├── di/                  # Hilt modules (AppModule, WorkerModule)
├── notification/        # Notification channel management
├── receiver/            # Broadcast receivers
├── ui/
│   ├── components/      # Reusable UI components (dialogs, cards)
│   ├── navigation/      # Navigation graph
│   ├── screens/
│   │   ├── auth/        # Login and registration
│   │   ├── home/        # Main dashboard
│   │   ├── profile/     # User profile
│   │   └── filteredlist/ # Transaction filtering
│   └── theme/           # Material theme
├── worker/              # Background tasks (PaymentReminderWorker)
└── MyFinancesApplication.kt
```

## Key Components

| Component | Purpose |
|-----------|---------|
| `HomeViewModel` | Manages dashboard state, transactions, budgets, debts |
| `BaseRepository` | Abstract CRUD operations for all data types |
| `AuthResult<T>` | Wrapper for Loading/Success/Error states |
| `BudgetOverviewCard` | Collapsible widget showing active/upcoming budgets |
| `DebtListItem` | Debt display with visual payment progress bar |

## Testing

The application was primarily tested through manual user testing. Unit tests cover critical data models and ViewModel logic using JUnit and MockK.

## Conclusion

MyFinances provides comprehensive personal finance management with real-time sync, intuitive UI, and robust architecture enabling future enhancements.
