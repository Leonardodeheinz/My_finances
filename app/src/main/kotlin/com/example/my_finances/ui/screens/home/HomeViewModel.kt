package com.example.my_finances.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_finances.data.model.AuthResult
import com.example.my_finances.data.model.Budget
import com.example.my_finances.data.model.BudgetPeriodType
import com.example.my_finances.data.model.Category
import com.example.my_finances.data.model.Contract
import com.example.my_finances.data.model.ContractStatus
import com.example.my_finances.data.model.Debt
import com.example.my_finances.data.model.DebtStatus
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
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

            // Load all budgets with real-time updates
            launch {
                budgetRepository.observeAllBudgets().collect { result ->
                    if (result is AuthResult.Success) {
                        println("📊 Loaded ${result.data.size} budgets")
                        _uiState.update {
                            it.copy(
                                budgets = result.data,
                                totalBudgetCount = result.data.size
                            )
                        }
                        // Recalculate budget spending when budgets are loaded
                        calculateBudgetSpending()
                    }
                }
            }

            // Load all transactions for budget tracking
            launch {
                transactionRepository.getAll().collect { result ->
                    if (result is AuthResult.Success) {
                        println("💰 Loaded ${result.data.size} total transactions")
                        _uiState.update {
                            it.copy(
                                allTransactions = result.data,
                                recentTransactions = result.data.take(10),
                                totalTransactionCount = result.data.size
                            )
                        }

                        // Calculate monthly income and expenses (current month only)
                        val currentMonthTransactions = result.data.filter { transaction ->
                            val transactionCal = Calendar.getInstance().apply { time = transaction.date }
                            transactionCal.get(Calendar.MONTH) + 1 == currentMonth &&
                            transactionCal.get(Calendar.YEAR) == currentYear
                        }

                        val income = currentMonthTransactions
                            .filter { it.type == TransactionType.INCOME }
                            .sumOf { it.amount }
                        val expenses = currentMonthTransactions
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
                        // Recalculate budget spending when transactions are loaded
                        calculateBudgetSpending()
                    } else if (result is AuthResult.Error) {
                        println("❌ Error loading transactions: ${result.message}")
                    }
                }
            }

            // Load active contracts (for display) with real-time updates
            launch {
                contractRepository.observeContracts().collect { result ->
                    if (result is AuthResult.Success) {
                        val activeContracts = result.data.filter { it.status == ContractStatus.OPEN }
                        println("📝 Loaded ${activeContracts.size} active contracts")
                        _uiState.update { it.copy(activeContracts = activeContracts) }
                    }
                }
            }

            // Load all contracts (for budget calculation and total count) with real-time updates
            launch {
                contractRepository.observeAll().collect { result ->
                    if (result is AuthResult.Success) {
                        println("📝 Loaded ${result.data.size} total contracts")
                        _uiState.update {
                            it.copy(
                                allContracts = result.data,
                                totalContractCount = result.data.size
                            )
                        }
                        calculateBudgetSpending()
                    }
                }
            }

            // Load open debts (for display) with real-time updates
            launch {
                debtRepository.observeDebts().collect { result ->
                    if (result is AuthResult.Success) {
                        val openDebts = result.data.filter {
                            it.status == DebtStatus.OPEN ||
                            it.status == DebtStatus.PARTIALLY_PAID ||
                            it.status == DebtStatus.OVERDUE
                        }
                        println("💳 Loaded ${openDebts.size} open debts")
                        _uiState.update { it.copy(openDebts = openDebts) }
                    }
                }
            }

            // Load all debts (for budget calculation and total count) with real-time updates
            launch {
                debtRepository.observeAll().collect { result ->
                    if (result is AuthResult.Success) {
                        println("💳 Loaded ${result.data.size} total debts")
                        _uiState.update {
                            it.copy(
                                allDebts = result.data,
                                totalDebtCount = result.data.size
                            )
                        }
                        calculateBudgetSpending()
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

            // Load categories with real-time updates
            launch {
                categoryRepository.observeAll().collect { result ->
                    if (result is AuthResult.Success) {
                        println("🏷️ Loaded ${result.data.size} categories")
                        _uiState.update { it.copy(categories = result.data) }
                    }
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Calculate spent amount for each budget based on transactions, debts, and contracts
     * that match the budget's categories and time period
     */
    private fun calculateBudgetSpending() {
        val state = _uiState.value
        val budgets = state.budgets
        val transactions = state.allTransactions
        val debts = state.allDebts
        val contracts = state.allContracts

        if (budgets.isEmpty()) {
            _uiState.update { it.copy(budgetsWithSpending = emptyList()) }
            return
        }

        val budgetsWithSpending = budgets.map { budget ->
            val spent = calculateSpentForBudget(budget, transactions, debts, contracts)
            budget.copy(spent = spent)
        }

        _uiState.update { it.copy(budgetsWithSpending = budgetsWithSpending) }
        println("📊 Calculated spending for ${budgetsWithSpending.size} budgets")
    }

    private fun calculateSpentForBudget(
        budget: Budget,
        transactions: List<Transaction>,
        debts: List<Debt>,
        contracts: List<Contract>
    ): Double {
        var totalSpent = 0.0
        val categoryIds = budget.categoryIds.toSet()

        // Filter transactions by budget period and categories
        val relevantTransactions = transactions.filter { transaction ->
            val matchesCategory = categoryIds.isEmpty() || transaction.categoryId in categoryIds
            val matchesPeriod = isDateInBudgetPeriod(transaction.date, budget)
            matchesCategory && matchesPeriod
        }

        // Sum up expenses from transactions
        totalSpent += relevantTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        // Subtract income (income reduces the "spent" amount as it's money coming in)
        totalSpent -= relevantTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        // Add debt amounts for matching categories
        val relevantDebts = debts.filter { debt ->
            val matchesCategory = categoryIds.isEmpty() || debt.categoryId in categoryIds
            matchesCategory
        }
        totalSpent += relevantDebts.sumOf { it.amount - it.paidAmount }

        // Add contract amounts for matching categories within the budget period
        val relevantContracts = contracts.filter { contract ->
            val matchesCategory = categoryIds.isEmpty() || contract.categoryId in categoryIds
            val matchesPeriod = isDateInBudgetPeriod(contract.startDate, budget) ||
                               isDateInBudgetPeriod(contract.endDate, budget)
            matchesCategory && matchesPeriod
        }
        totalSpent += relevantContracts.sumOf { it.amount }

        return maxOf(0.0, totalSpent)
    }

    private fun isDateInBudgetPeriod(date: Date, budget: Budget): Boolean {
        val calendar = Calendar.getInstance().apply { time = date }
        val dateMonth = calendar.get(Calendar.MONTH) + 1
        val dateYear = calendar.get(Calendar.YEAR)

        return when (budget.periodType) {
            BudgetPeriodType.MONTHLY -> {
                dateMonth == budget.month && dateYear == budget.year
            }
            BudgetPeriodType.YEARLY -> {
                dateYear == budget.year
            }
            BudgetPeriodType.CUSTOM -> {
                !date.before(budget.startDate) && !date.after(budget.endDate)
            }
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
                    // Create expense transaction for the contract payment
                    createContractPaymentTransaction(contract)
                    loadFinancialData()
                }
            }
        }
    }

    private fun createContractPaymentTransaction(contract: Contract) {
        viewModelScope.launch {
            // Create transaction date for the payment day of current month
            val calendar = Calendar.getInstance()
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            val paymentDay = contract.paymentDayOfMonth.coerceIn(1, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))

            // If payment day has passed this month, set for next month
            if (currentDay > paymentDay) {
                calendar.add(Calendar.MONTH, 1)
            }
            calendar.set(Calendar.DAY_OF_MONTH, paymentDay)

            val transaction = Transaction(
                categoryId = contract.categoryId,
                amount = contract.amount,
                description = "Contract payment: ${contract.name}",
                type = TransactionType.EXPENSE,
                date = calendar.time
            )

            transactionRepository.insert(transaction).collect { transResult ->
                if (transResult is AuthResult.Success) {
                    println("✅ Created expense transaction for contract: ${contract.name}")
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
                    // If auto-create transaction is enabled, create an expense transaction
                    if (debt.autoCreateTransaction && debt.repaymentRate > 0) {
                        createDebtPaymentTransaction(debt)
                    }
                    loadFinancialData()
                }
            }
        }
    }

    private fun createDebtPaymentTransaction(debt: Debt) {
        viewModelScope.launch {
            // Create transaction date for the payment day of current month
            val calendar = Calendar.getInstance()
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            val paymentDay = debt.paymentDayOfMonth.coerceIn(1, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))

            // If payment day has passed this month, set for next month
            if (currentDay > paymentDay) {
                calendar.add(Calendar.MONTH, 1)
            }
            calendar.set(Calendar.DAY_OF_MONTH, paymentDay)

            val transaction = Transaction(
                categoryId = debt.categoryId,
                amount = debt.repaymentRate,
                description = "Debt payment: ${debt.creditor}",
                type = TransactionType.EXPENSE,
                date = calendar.time
            )

            transactionRepository.insert(transaction).collect { transResult ->
                if (transResult is AuthResult.Success) {
                    println("✅ Created expense transaction for debt: ${debt.creditor}")
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

    // Budget management functions
    fun addBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.insert(budget).collect { result ->
                when (result) {
                    is AuthResult.Success -> {
                        println("✅ Budget added successfully: ${result.data}")
                        loadFinancialData()
                    }
                    is AuthResult.Error -> {
                        println("❌ Error adding budget: ${result.message}")
                    }
                    is AuthResult.Loading -> {
                        println("⏳ Adding budget...")
                    }
                }
            }
        }
    }

    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.update(budget.id, budget).collect { result ->
                if (result is AuthResult.Success) {
                    println("✅ Budget updated successfully")
                    loadFinancialData()
                }
            }
        }
    }

    fun deleteBudget(id: String) {
        viewModelScope.launch {
            budgetRepository.delete(id).collect { result ->
                if (result is AuthResult.Success) {
                    println("✅ Budget deleted successfully")
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

    /**
     * Load all data for export (transactions, debts, contracts)
     */
    fun loadAllDataForExport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingExportData = true) }

            // Load all transactions
            launch {
                transactionRepository.getAll().collect { result ->
                    if (result is AuthResult.Success) {
                        println("📊 Export: Loaded ${result.data.size} transactions")
                        _uiState.update { it.copy(allTransactions = result.data) }
                    }
                }
            }

            // Load all debts
            launch {
                debtRepository.getAll().collect { result ->
                    if (result is AuthResult.Success) {
                        println("📊 Export: Loaded ${result.data.size} debts")
                        _uiState.update { it.copy(allDebts = result.data) }
                    }
                }
            }

            // Load all contracts
            launch {
                contractRepository.getAll().collect { result ->
                    if (result is AuthResult.Success) {
                        println("📊 Export: Loaded ${result.data.size} contracts")
                        _uiState.update { it.copy(allContracts = result.data) }
                    }
                }
            }

            _uiState.update { it.copy(isLoadingExportData = false) }
        }
    }

    /**
     * Clear export data when dialog is dismissed
     */
    fun clearExportData() {
        _uiState.update {
            it.copy(
                allTransactions = emptyList(),
                allDebts = emptyList(),
                allContracts = emptyList()
            )
        }
    }
}
