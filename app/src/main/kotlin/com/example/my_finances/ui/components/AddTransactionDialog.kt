package com.example.my_finances.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.my_finances.data.model.Category
import com.example.my_finances.data.model.Transaction
import com.example.my_finances.data.model.TransactionType
import java.util.Date

@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    transaction: Transaction? = null,
    categories: List<Category> = emptyList(),
    onAddCategory: (Category) -> Unit = {},
    getCategorySuggestion: (String) -> Category? = { null }
) {
    var amount by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
    var description by remember { mutableStateOf(transaction?.description ?: "") }
    var type by remember { mutableStateOf(transaction?.type ?: TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf<Category?>(
        categories.firstOrNull { it.id == transaction?.categoryId }
    ) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var suggestedCategory by remember { mutableStateOf<Category?>(null) }

    // Get category suggestion when description changes
    LaunchedEffect(description) {
        if (description.length > 2) {
            suggestedCategory = getCategorySuggestion(description)
        }
    }

    // Show add category dialog
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onSave = { category ->
                onAddCategory(category)
                selectedCategory = category
                showAddCategoryDialog = false
            },
            transactionType = type
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (transaction == null) "Add Transaction" else "Edit Transaction")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type selection
                Text(
                    text = "Type",
                    style = MaterialTheme.typography.labelMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == TransactionType.INCOME,
                        onClick = {
                            type = TransactionType.INCOME
                            selectedCategory = null
                        },
                        label = { Text("Income") }
                    )
                    FilterChip(
                        selected = type == TransactionType.EXPENSE,
                        onClick = {
                            type = TransactionType.EXPENSE
                            selectedCategory = null
                        },
                        label = { Text("Expense") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Amount input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Description input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Category suggestion
                if (suggestedCategory != null && selectedCategory?.id != suggestedCategory?.id) {
                    Text(
                        text = "Suggested category:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    SuggestionChip(
                        onClick = { selectedCategory = suggestedCategory },
                        label = { Text(suggestedCategory?.name ?: "") }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Category selection
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium
                )

                val filteredCategories = categories.filter { it.type == type }

                if (filteredCategories.isEmpty()) {
                    Text(
                        text = "No categories available. Create one below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredCategories) { category ->
                            FilterChip(
                                selected = selectedCategory?.id == category.id,
                                onClick = { selectedCategory = category },
                                label = { Text(category.name) }
                            )
                        }
                    }
                }

                // Add category button
                OutlinedButton(
                    onClick = { showAddCategoryDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Create New Category")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    println("💾 Save button clicked: amount=$amountValue, desc=$description, category=${selectedCategory?.name}")
                    if (amountValue > 0 && description.isNotBlank()) {
                        val newTransaction = Transaction(
                            id = transaction?.id ?: "",
                            amount = amountValue,
                            type = type,
                            description = description,
                            date = transaction?.date ?: Date(),
                            categoryId = selectedCategory?.id ?: "",
                            userId = transaction?.userId ?: ""
                        )
                        println("💾 Calling onSave with transaction: $newTransaction")
                        onSave(newTransaction)
                        onDismiss()
                    } else {
                        println("❌ Validation failed: amount=$amountValue, desc='$description'")
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

