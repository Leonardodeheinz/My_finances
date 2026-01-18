package com.example.my_finances.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.my_finances.data.model.Category
import com.example.my_finances.data.model.Debt
import com.example.my_finances.data.model.DebtStatus
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtDialog(
    onDismiss: () -> Unit,
    onSave: (Debt) -> Unit,
    debt: Debt? = null,
    categories: List<Category> = emptyList()
) {
    var creditor by remember { mutableStateOf(debt?.creditor ?: "") }
    var description by remember { mutableStateOf(debt?.description ?: "") }
    var amount by remember { mutableStateOf(debt?.amount?.toString() ?: "") }
    var repaymentRate by remember { mutableStateOf(debt?.repaymentRate?.toString() ?: "") }
    var paidAmount by remember { mutableStateOf(debt?.paidAmount?.toString() ?: "0") }
    var status by remember { mutableStateOf(debt?.status ?: DebtStatus.OPEN) }
    var selectedCategory by remember { mutableStateOf<Category?>(
        categories.firstOrNull { it.id == debt?.categoryId }
    ) }

    // Payment day of month
    var paymentDayOfMonth by remember { mutableStateOf(debt?.paymentDayOfMonth?.toString() ?: "1") }
    var autoCreateTransaction by remember { mutableStateOf(debt?.autoCreateTransaction ?: false) }

    // Due date state
    var dueDate by remember { mutableStateOf(debt?.dueDate?.toDate()) }
    var dueDateText by remember {
        mutableStateOf(
            debt?.dueDate?.toDate()?.let {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
            } ?: ""
        )
    }
    var showDueDatePicker by remember { mutableStateOf(false) }

    // Reminder state
    var reminderEnabled by remember { mutableStateOf(debt?.reminderEnabled ?: false) }
    var reminderDaysBefore by remember { mutableStateOf(debt?.reminderDaysBefore?.toString() ?: "3") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (debt == null) "Add Debt" else "Edit Debt")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Creditor input
                OutlinedTextField(
                    value = creditor,
                    onValueChange = { creditor = it },
                    label = { Text("Creditor") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Description input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                // Category selection
                if (categories.isNotEmpty()) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory?.id == category.id,
                                onClick = {
                                    selectedCategory = if (selectedCategory?.id == category.id) null else category
                                },
                                label = { Text(category.name) }
                            )
                        }
                    }
                }

                // Amount input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Total Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Repayment rate input
                OutlinedTextField(
                    value = repaymentRate,
                    onValueChange = { repaymentRate = it },
                    label = { Text("Monthly Repayment Rate") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Payment day of month
                OutlinedTextField(
                    value = paymentDayOfMonth,
                    onValueChange = {
                        val day = it.toIntOrNull()
                        if (it.isEmpty() || (day != null && day in 1..31)) {
                            paymentDayOfMonth = it
                        }
                    },
                    label = { Text("Payment Day of Month (1-31)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("Day when the monthly payment is taken") }
                )

                // Auto-create transaction toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-create Expense",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Automatically create monthly expense transaction",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoCreateTransaction,
                        onCheckedChange = { autoCreateTransaction = it }
                    )
                }

                // Due Date input with DatePicker
                OutlinedTextField(
                    value = dueDateText,
                    onValueChange = { },
                    label = { Text("Due Date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDueDatePicker = true },
                    enabled = false,
                    readOnly = true,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    trailingIcon = {
                        IconButton(onClick = { showDueDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select due date")
                        }
                    },
                    placeholder = { Text("Select due date") }
                )

                // Reminder toggle
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enable Reminder",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it }
                    )
                }

                // Reminder days input (only show when reminder is enabled)
                if (reminderEnabled) {
                    OutlinedTextField(
                        value = reminderDaysBefore,
                        onValueChange = { reminderDaysBefore = it },
                        label = { Text("Remind days before due date") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Paid amount input - always show
                OutlinedTextField(
                    value = paidAmount,
                    onValueChange = { paidAmount = it },
                    label = { Text("Already Paid Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        val paid = paidAmount.toDoubleOrNull() ?: 0.0
                        val total = amount.toDoubleOrNull() ?: 0.0
                        if (total > 0) {
                            val percentage = ((paid / total) * 100).toInt().coerceIn(0, 100)
                            Text("$percentage% paid")
                        }
                    }
                )

                // Status selection
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.labelMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = status == DebtStatus.OPEN,
                        onClick = { status = DebtStatus.OPEN },
                        label = { Text("Open") }
                    )
                    FilterChip(
                        selected = status == DebtStatus.PAID,
                        onClick = { status = DebtStatus.PAID },
                        label = { Text("Paid") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    val repaymentValue = repaymentRate.toDoubleOrNull() ?: 0.0
                    val paidValue = paidAmount.toDoubleOrNull() ?: 0.0
                    val reminderDays = reminderDaysBefore.toIntOrNull() ?: 3
                    val paymentDay = paymentDayOfMonth.toIntOrNull() ?: 1

                    // Auto-update status based on paid amount
                    val finalStatus = when {
                        paidValue >= amountValue -> DebtStatus.PAID
                        paidValue > 0 -> DebtStatus.PARTIALLY_PAID
                        else -> status
                    }

                    if (amountValue > 0 && creditor.isNotBlank() && repaymentValue > 0) {
                        onSave(
                            Debt(
                                id = debt?.id ?: "",
                                userId = debt?.userId ?: "",
                                categoryId = selectedCategory?.id ?: "",
                                creditor = creditor,
                                description = description,
                                amount = amountValue,
                                repaymentRate = repaymentValue,
                                paidAmount = paidValue,
                                status = finalStatus,
                                dueDate = dueDate?.let { Timestamp(it) },
                                paymentDayOfMonth = paymentDay.coerceIn(1, 31),
                                autoCreateTransaction = autoCreateTransaction,
                                reminderEnabled = reminderEnabled,
                                reminderDaysBefore = reminderDays,
                                lastReminderSent = debt?.lastReminderSent
                            )
                        )
                        onDismiss()
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

    // Due Date Picker Dialog
    if (showDueDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate?.time ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDueDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            dueDate = Date(millis)
                            dueDateText = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dueDate)
                        }
                        showDueDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDueDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
