package com.example.my_finances.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.my_finances.data.model.Budget
import com.example.my_finances.data.model.Category
import com.example.my_finances.data.model.Contract
import com.example.my_finances.data.model.Debt
import com.example.my_finances.data.model.DebtStatus
import com.example.my_finances.data.model.Transaction
import com.example.my_finances.data.model.TransactionType
import com.example.my_finances.ui.components.AddBudgetDialog
import com.example.my_finances.ui.components.AddContractDialog
import com.example.my_finances.ui.components.CategoryManagementDialog
import com.example.my_finances.ui.components.AddDebtDialog
import com.example.my_finances.ui.components.AddTransactionDialog
import com.example.my_finances.ui.components.BudgetDetailDialog
import com.example.my_finances.ui.components.BudgetOverviewCard
import com.example.my_finances.ui.components.ExportDialog
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToFilteredList: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var showAddContractDialog by remember { mutableStateOf(false) }
    var showAddDebtDialog by remember { mutableStateOf(false) }
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }
    var showCategoryManagementDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var editingContract by remember { mutableStateOf<Contract?>(null) }
    var editingDebt by remember { mutableStateOf<Debt?>(null) }
    var editingBudget by remember { mutableStateOf<Budget?>(null) }

    var showBudgetDetailDialog by remember { mutableStateOf(false) }
    var viewingBudget by remember { mutableStateOf<Budget?>(null) }

    var expandedContracts by remember { mutableStateOf(false) }
    var expandedDebts by remember { mutableStateOf(false) }

    if (showAddTransactionDialog) {
        AddTransactionDialog(
            onDismiss = {
                showAddTransactionDialog = false
                editingTransaction = null
            },
            onSave = { transaction ->
                if (editingTransaction != null) {
                    viewModel.updateTransaction(transaction)
                } else {
                    viewModel.addTransaction(transaction)
                }
                editingTransaction = null
            },
            transaction = editingTransaction,
            categories = uiState.categories,
            onAddCategory = { category ->
                viewModel.addCategory(category)
            },
            getCategorySuggestion = { description ->
                viewModel.getCategorySuggestion(description)
            }
        )
    }

    if (showAddContractDialog) {
        AddContractDialog(
            onDismiss = {
                showAddContractDialog = false
                editingContract = null
            },
            onSave = { contract ->
                if (editingContract != null) {
                    viewModel.updateContract(contract)
                } else {
                    viewModel.addContract(contract)
                }
                editingContract = null
            },
            contract = editingContract,
            categories = uiState.categories
        )
    }

    if (showAddDebtDialog) {
        AddDebtDialog(
            onDismiss = {
                showAddDebtDialog = false
                editingDebt = null
            },
            onSave = { debt ->
                if (editingDebt != null) {
                    viewModel.updateDebt(debt)
                } else {
                    viewModel.addDebt(debt)
                }
                editingDebt = null
            },
            debt = editingDebt,
            categories = uiState.categories
        )
    }

    if (showCategoryManagementDialog) {
        CategoryManagementDialog(
            categories = uiState.categories,
            onDismiss = { showCategoryManagementDialog = false },
            onAddCategory = { category ->
                viewModel.addCategory(category)
            },
            onUpdateCategory = { category ->
                viewModel.updateCategory(category)
            },
            onDeleteCategory = { categoryId ->
                viewModel.deleteCategory(categoryId)
            }
        )
    }

    if (showAddBudgetDialog) {
        AddBudgetDialog(
            onDismiss = {
                showAddBudgetDialog = false
                editingBudget = null
            },
            onSave = { budget ->
                if (editingBudget != null) {
                    viewModel.updateBudget(budget)
                } else {
                    viewModel.addBudget(budget)
                }
                editingBudget = null
            },
            budget = editingBudget,
            categories = uiState.categories
        )
    }

    if (showBudgetDetailDialog && viewingBudget != null) {
        BudgetDetailDialog(
            budget = viewingBudget!!,
            categories = uiState.categories,
            transactions = uiState.allTransactions,
            debts = uiState.allDebts,
            contracts = uiState.allContracts,
            onDismiss = {
                showBudgetDetailDialog = false
                viewingBudget = null
            }
        )
    }

    if (showExportDialog) {
        ExportDialog(
            transactions = uiState.allTransactions.ifEmpty { uiState.recentTransactions },
            debts = uiState.allDebts.ifEmpty { uiState.openDebts },
            contracts = uiState.allContracts.ifEmpty { uiState.activeContracts },
            categories = uiState.categories,
            onDismiss = {
                showExportDialog = false
                viewModel.clearExportData()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Welcome back,",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = uiState.userName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.loadAllDataForExport()
                            showExportDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export Data"
                        )
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
                DropdownMenu(
                    expanded = showFabMenu,
                    onDismissRequest = { showFabMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Add Transaction") },
                        onClick = {
                            showFabMenu = false
                            showAddTransactionDialog = true
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Receipt, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add Contract") },
                        onClick = {
                            showFabMenu = false
                            showAddContractDialog = true
                        },
                        leadingIcon = {
                            Icon(Icons.Default.CreditCard, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add Debt") },
                        onClick = {
                            showFabMenu = false
                            showAddDebtDialog = true
                        },
                        leadingIcon = {
                            Icon(Icons.Default.AccountBalance, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add Budget") },
                        onClick = {
                            showFabMenu = false
                            showAddBudgetDialog = true
                        },
                        leadingIcon = {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Manage Categories") },
                        onClick = {
                            showFabMenu = false
                            showCategoryManagementDialog = true
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Label, contentDescription = null)
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Balance Card
                item {
                    BalanceCard(
                        balance = uiState.totalBalance,
                        income = uiState.monthlyIncome,
                        expenses = uiState.monthlyExpenses,
                        onClick = onNavigateToFilteredList
                    )
                }

                // Total Counts Row
                item {
                    TotalCountsCard(
                        transactionCount = uiState.totalTransactionCount,
                        debtCount = uiState.totalDebtCount,
                        contractCount = uiState.totalContractCount,
                        budgetCount = uiState.totalBudgetCount
                    )
                }

                // Budget Overview
                if (uiState.budgetsWithSpending.isNotEmpty()) {
                    item {
                        BudgetOverviewCard(
                            budgets = uiState.budgetsWithSpending,
                            categories = uiState.categories,
                            onBudgetClick = { budget ->
                                viewingBudget = budget
                                showBudgetDetailDialog = true
                            },
                            onEditBudget = { budget ->
                                editingBudget = uiState.budgets.find { it.id == budget.id }
                                showAddBudgetDialog = true
                            },
                            onDeleteBudget = { budget ->
                                viewModel.deleteBudget(budget.id)
                            }
                        )
                    }
                }

                // Contracts Section
                item {
                    ExpandableSection(
                        title = "Contracts",
                        count = uiState.activeContracts.size,
                        expanded = expandedContracts,
                        onExpandChange = { expandedContracts = it }
                    )
                }

                if (expandedContracts) {
                    if (uiState.activeContracts.isEmpty()) {
                        item {
                            EmptyStateCard(message = "No active contracts")
                        }
                    } else {
                        items(uiState.activeContracts) { contract ->
                            ContractListItem(
                                contract = contract,
                                onEdit = {
                                    editingContract = contract
                                    showAddContractDialog = true
                                },
                                onDelete = {
                                    viewModel.deleteContract(contract.id)
                                }
                            )
                        }
                    }
                }

                // Debts Section
                item {
                    ExpandableSection(
                        title = "Debts",
                        count = uiState.openDebts.size,
                        expanded = expandedDebts,
                        onExpandChange = { expandedDebts = it }
                    )
                }

                if (expandedDebts) {
                    if (uiState.openDebts.isEmpty()) {
                        item {
                            EmptyStateCard(message = "No open debts")
                        }
                    } else {
                        items(uiState.openDebts) { debt ->
                            DebtListItem(
                                debt = debt,
                                onEdit = {
                                    editingDebt = debt
                                    showAddDebtDialog = true
                                },
                                onDelete = {
                                    viewModel.deleteDebt(debt.id)
                                }
                            )
                        }
                    }
                }

                // Recent Transactions Section
                item {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (uiState.recentTransactions.isEmpty()) {
                    item {
                        EmptyStateCard(message = "No recent transactions")
                    }
                } else {
                    val categoriesMap = uiState.categories.associateBy { it.id }
                    items(uiState.recentTransactions) { transaction ->
                        val category = categoriesMap[transaction.categoryId]
                        TransactionListItem(
                            transaction = transaction,
                            category = category,
                            onEdit = {
                                editingTransaction = transaction
                                showAddTransactionDialog = true
                            },
                            onDelete = {
                                viewModel.deleteTransaction(transaction.id)
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun BalanceCard(
    balance: Double,
    income: Double,
    expenses: Double,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatCurrency(balance),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BalanceItem(
                        label = "Income",
                        amount = income,
                        icon = Icons.Default.TrendingUp,
                        isPositive = true
                    )
                    BalanceItem(
                        label = "Expenses",
                        amount = expenses,
                        icon = Icons.Default.TrendingDown,
                        isPositive = false
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceItem(
    label: String,
    amount: Double,
    icon: ImageVector,
    isPositive: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = formatCurrency(amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun TotalCountsCard(
    transactionCount: Int,
    debtCount: Int,
    contractCount: Int,
    budgetCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CountItem(
                count = transactionCount,
                label = "Transactions",
                icon = Icons.Default.Receipt
            )
            CountItem(
                count = debtCount,
                label = "Debts",
                icon = Icons.Default.AccountBalance
            )
            CountItem(
                count = contractCount,
                label = "Contracts",
                icon = Icons.Default.CreditCard
            )
            CountItem(
                count = budgetCount,
                label = "Budgets",
                icon = Icons.Default.AccountBalanceWallet
            )
        }
    }
}

@Composable
private fun CountItem(
    count: Int,
    label: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
    return format.format(amount)
}

@Composable
private fun ExpandableSection(
    title: String,
    count: Int,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandChange(!expanded) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "($count)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand"
            )
        }
    }
}

@Composable
private fun TransactionListItem(
    transaction: Transaction,
    category: Category?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = category?.let {
        try {
            Color(android.graphics.Color.parseColor(it.color))
        } catch (e: Exception) {
            MaterialTheme.colorScheme.primary
        }
    } ?: MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category color indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(categoryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (category?.name?.firstOrNull() ?.uppercase() ?: "?"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(transaction.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    if (category != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .background(
                                    categoryColor.copy(alpha = 0.2f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = categoryColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = formatCurrency(transaction.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == TransactionType.INCOME)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ContractListItem(
    contract: Contract,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contract.name.ifEmpty { "Contract" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (contract.description.isNotEmpty()) {
                        Text(
                            text = contract.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formatCurrency(contract.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Start: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(contract.startDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "End: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(contract.endDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DebtListItem(
    debt: Debt,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val remaining = debt.amount - debt.paidAmount
    val progress = if (debt.amount > 0) (debt.paidAmount / debt.amount * 100).toInt() else 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = debt.creditor,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (debt.description.isNotEmpty()) {
                        Text(
                            text = debt.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(remaining),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "$progress% paid",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            val progressFloat = if (debt.amount > 0) (debt.paidAmount / debt.amount).toFloat().coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(
                progress = { progressFloat },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    progress >= 100 -> Color(0xFF4CAF50)
                    progress >= 50 -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.error
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Paid: ${formatCurrency(debt.paidAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Monthly: ${formatCurrency(debt.repaymentRate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Total: ${formatCurrency(debt.amount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
