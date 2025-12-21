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
import com.example.my_finances.data.model.Contract
import com.example.my_finances.data.model.ContractStatus
import java.util.Calendar
import java.util.Date

@Composable
fun AddContractDialog(
    onDismiss: () -> Unit,
    onSave: (Contract) -> Unit,
    contract: Contract? = null
) {
    var name by remember { mutableStateOf(contract?.name ?: "") }
    var description by remember { mutableStateOf(contract?.description ?: "") }
    var amount by remember { mutableStateOf(contract?.amount?.toString() ?: "") }
    var status by remember { mutableStateOf(contract?.status ?: ContractStatus.OPEN) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (contract == null) "Add Contract" else "Edit Contract")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contract Name") },
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

                // Amount input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monthly Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Status selection (only show if editing)
                if (contract != null) {
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
                            selected = status == ContractStatus.OPEN,
                            onClick = { status = ContractStatus.OPEN },
                            label = { Text("Open") }
                        )
                        FilterChip(
                            selected = status == ContractStatus.CLOSED,
                            onClick = { status = ContractStatus.CLOSED },
                            label = { Text("Closed") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (amountValue > 0 && name.isNotBlank()) {
                        val now = Date()
                        val calendar = Calendar.getInstance()
                        calendar.time = now

                        // Set end date to 1 year from now
                        val endCalendar = Calendar.getInstance()
                        endCalendar.add(Calendar.YEAR, 1)

                        onSave(
                            Contract(
                                id = contract?.id ?: "",
                                userid = contract?.userid ?: "",
                                categoryId = contract?.categoryId ?: "",
                                name = name,
                                description = description,
                                amount = amountValue,
                                month = calendar.get(Calendar.MONTH) + 1,
                                year = calendar.get(Calendar.YEAR),
                                startDate = contract?.startDate ?: now,
                                endDate = contract?.endDate ?: endCalendar.time,
                                status = status
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
