package com.example.my_finances.ui.screens.home

import com.example.my_finances.data.model.Budget
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
    val recentTransactions: List<Transaction> = emptyList(),
    val activeContracts: List<Contract> = emptyList(),
    val totalDebt: Double = 0.0,
    val openDebts: List<Debt> = emptyList()
)
