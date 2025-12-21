package com.example.my_finances.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_finances.data.model.AuthResult
import com.example.my_finances.data.model.Category
import com.example.my_finances.data.model.Contract
import com.example.my_finances.data.model.Debt
import com.example.my_finances.data.model.Transaction
import com.example.my_finances.data.model.TransactionType
import com.example.my_finances.data.repository.BudgetRepository
import com.example.my_finances.data.repository.ContractRepository
import com.example.my_finances.data.repository.DebtRepository
import com.example.my_finances.data.repository.FirebaseAuthDataSource
import com.example.my_finances.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val contractRepository: ContractRepository,
    private val debtRepository: DebtRepository,
    private val categoryRepository: com.example.my_finances.data.repository.CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserInfo()
        loadFinancialData()
    }

    private fun loadUserInfo() {
        val user = authDataSource.currentUser
        println("🔐 Current user: ${user?.uid} - ${user?.email}")
        val displayName = user?.displayName ?: user?.email?.substringBefore("@") ?: "User"
        _uiState.update { it.copy(userName = displayName) }
    }

    private fun loadFinancialData() {
        viewModelScope.launch {
            println("🔄 Loading financial data...")
            _uiState.update { it.copy(isLoading = true) }

            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH) + 1
            val currentYear = calendar.get(Calendar.YEAR)

            // Load budgets for current month
            launch {
                budgetRepository.getByMonth(currentMonth, currentYear).collect { result ->
                    if (result is AuthResult.Success) {
                        println("📊 Loaded ${result.data.size} budgets")
                        _uiState.update { it.copy(budgets = result.data) }
                    }
                }
            }

            // Load recent transactions
            launch {
                transactionRepository.getByMonth(currentMonth, currentYear).collect { result ->
                    if (result is AuthResult.Success) {
                        println("💰 Loaded ${result.data.size} transactions")
                        _uiState.update { it.copy(recentTransactions = result.data.take(10)) }

                        // Calculate monthly income and expenses
                        val income = result.data
                            .filter { it.type == TransactionType.INCOME }
                            .sumOf { it.amount }
                        val expenses = result.data
                            .filter { it.type == TransactionType.EXPENSE }
                            .sumOf { it.amount }

                        println("💵 Income: $income, Expenses: $expenses, Balance: ${income - expenses}")

                        _uiState.update {
                            it.copy(
                                monthlyIncome = income,
                                monthlyExpenses = expenses,
                                totalBalance = income - expenses
                            )
                        }
                    } else if (result is AuthResult.Error) {
                        println("❌ Error loading transactions: ${result.message}")
                    }
                }
            }

            // Load active contracts
            launch {
                contractRepository.getActiveContracts().collect { result ->
                    if (result is AuthResult.Success) {
                        println("📝 Loaded ${result.data.size} contracts")
                        _uiState.update { it.copy(activeContracts = result.data) }
                    }
                }
            }

            // Load debts
            launch {
                debtRepository.getOpenDebts().collect { result ->
                    if (result is AuthResult.Success) {
                        println("💳 Loaded ${result.data.size} debts")
                        _uiState.update { it.copy(openDebts = result.data) }
                    }
                }
            }

            launch {
                debtRepository.getTotalDebt().collect { result ->
                    if (result is AuthResult.Success) {
                        println("💸 Total debt: ${result.data}")
                        _uiState.update { it.copy(totalDebt = result.data) }
                    }
                }
            }

            // Load categories
            launch {
                categoryRepository.getAll().collect { result ->
                    if (result is AuthResult.Success) {
                        println("🏷️ Loaded ${result.data.size} categories")
                        _uiState.update { it.copy(categories = result.data) }
                    }
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.insert(transaction).collect { result ->
                when (result) {
                    is AuthResult.Success -> {
                        println("✅ Transaction added successfully: ${result.data}")
                        loadFinancialData()
                    }
                    is AuthResult.Error -> {
                        println("❌ Error adding transaction: ${result.message}")
                    }
                    is AuthResult.Loading -> {
                        println("⏳ Adding transaction...")
                    }
                }
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.update(transaction.id, transaction).collect { result ->
                if (result is AuthResult.Success) {
                    loadFinancialData()
                }
            }
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            transactionRepository.delete(id).collect { result ->
                if (result is AuthResult.Success) {
                    loadFinancialData()
                }
            }
        }
    }

    fun addContract(contract: Contract) {
        viewModelScope.launch {
            contractRepository.insert(contract).collect { result ->
                if (result is AuthResult.Success) {
                    loadFinancialData()
                }
            }
        }
    }

    fun updateContract(contract: Contract) {
        viewModelScope.launch {
            contractRepository.update(contract.id, contract).collect { result ->
                if (result is AuthResult.Success) {
                    loadFinancialData()
                }
            }
        }
    }

    fun deleteContract(id: String) {
        viewModelScope.launch {
            contractRepository.delete(id).collect { result ->
                if (result is AuthResult.Success) {
                    loadFinancialData()
                }
            }
        }
    }

    fun addDebt(debt: Debt) {
        viewModelScope.launch {
            debtRepository.insert(debt).collect { result ->
                if (result is AuthResult.Success) {
                    loadFinancialData()
                }
            }
        }
    }

    fun updateDebt(debt: Debt) {
        viewModelScope.launch {
            debtRepository.update(debt.id, debt).collect { result ->
                if (result is AuthResult.Success) {
                    loadFinancialData()
                }
            }
        }
    }

    fun deleteDebt(id: String) {
        viewModelScope.launch {
            debtRepository.delete(id).collect { result ->
                if (result is AuthResult.Success) {
                    loadFinancialData()
                }
            }
        }
    }

    // Category management functions
    fun addCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.insert(category).collect { result ->
                if (result is AuthResult.Success) {
                    println("✅ Category added successfully: ${result.data}")
                    loadFinancialData()
                }
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.update(category.id, category).collect { result ->
                if (result is AuthResult.Success) {
                    loadFinancialData()
                }
            }
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            categoryRepository.delete(id).collect { result ->
                if (result is AuthResult.Success) {
                    loadFinancialData()
                }
            }
        }
    }

    /**
     * Get category suggestion based on transaction description
     * Looks for previous transactions with similar descriptions
     */
    fun getCategorySuggestion(description: String): Category? {
        val transactions = _uiState.value.recentTransactions
        val matchingTransaction = transactions
            .filter { it.description.contains(description, ignoreCase = true) && it.categoryId.isNotEmpty() }
            .maxByOrNull { it.createdAt }

        return matchingTransaction?.let { transaction ->
            _uiState.value.categories.firstOrNull { it.id == transaction.categoryId }
        }
    }
}
