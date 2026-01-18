package com.example.my_finances.ui.screens.home

import com.example.my_finances.data.model.Budget
import com.example.my_finances.data.model.Category
import com.example.my_finances.data.model.Contract
import com.example.my_finances.data.model.Debt
import com.example.my_finances.data.model.Transaction

data class HomeUiState(
    val userName: String = "",
    val isLoading: Boolean = false,
    val totalBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val budgets: List<Budget> = emptyList(),
    val budgetsWithSpending: List<Budget> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val activeContracts: List<Contract> = emptyList(),
    val totalDebt: Double = 0.0,
    val openDebts: List<Debt> = emptyList(),
    val categories: List<Category> = emptyList(),
    // Total counts
    val totalTransactionCount: Int = 0,
    val totalDebtCount: Int = 0,
    val totalContractCount: Int = 0,
    val totalBudgetCount: Int = 0,
    // Export data (loaded on demand)
    val allTransactions: List<Transaction> = emptyList(),
    val allDebts: List<Debt> = emptyList(),
    val allContracts: List<Contract> = emptyList(),
    val isLoadingExportData: Boolean = false
)
