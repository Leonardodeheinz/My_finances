package com.example.my_finances.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.my_finances.data.model.Debt
import com.example.my_finances.data.model.DebtStatus

@Composable
fun AddDebtDialog(
    onDismiss: () -> Unit,
    onSave: (Debt) -> Unit,
    debt: Debt? = null
) {
    var creditor by remember { mutableStateOf(debt?.creditor ?: "") }
    var description by remember { mutableStateOf(debt?.description ?: "") }
    var amount by remember { mutableStateOf(debt?.amount?.toString() ?: "") }
    var repaymentRate by remember { mutableStateOf(debt?.repaymentRate?.toString() ?: "") }
    var paidAmount by remember { mutableStateOf(debt?.paidAmount?.toString() ?: "0") }
    var status by remember { mutableStateOf(debt?.status ?: DebtStatus.OPEN) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (debt == null) "Add Debt" else "Edit Debt")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
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

                // Paid amount input (only show if editing)
                if (debt != null) {
                    OutlinedTextField(
                        value = paidAmount,
                        onValueChange = { paidAmount = it },
                        label = { Text("Paid Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Status selection (only show if editing)
                if (debt != null) {
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    val repaymentValue = repaymentRate.toDoubleOrNull() ?: 0.0
                    val paidValue = paidAmount.toDoubleOrNull() ?: 0.0

                    if (amountValue > 0 && creditor.isNotBlank() && repaymentValue > 0) {
                        onSave(
                            Debt(
                                id = debt?.id ?: "",
                                userId = debt?.userId ?: "",
                                creditor = creditor,
                                description = description,
                                amount = amountValue,
                                repaymentRate = repaymentValue,
                                paidAmount = paidValue,
                                status = status,
                                dueDate = debt?.dueDate
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
}
