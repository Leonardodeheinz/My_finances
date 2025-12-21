package com.example.my_finances.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContractDialog(
    onDismiss: () -> Unit,
    onSave: (Contract) -> Unit,
    contract: Contract? = null
) {
    var name by remember { mutableStateOf(contract?.name ?: "") }
    var description by remember { mutableStateOf(contract?.description ?: "") }
    var amount by remember { mutableStateOf(contract?.amount?.toString() ?: "") }


    val startDate by remember { mutableStateOf(contract?.startDate ?: "") }
    val startDateText by remember {
        mutableStateOf(
            contract?.startDate?.let {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
            } ?: ""

        )
    }

    var endDate by remember { mutableStateOf(contract?.endDate) }
    var endDateText by remember {
        mutableStateOf(
            contract?.endDate?.let {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
            } ?: ""
        )
    }
    var status by remember { mutableStateOf(contract?.status ?: ContractStatus.OPEN) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

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

                // Start Date input with DatePicker
                OutlinedTextField(
                    value = startDateText,
                    onValueChange = { },
                    label = { Text("Starting date of contract") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showStartDatePicker = true },
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
                        IconButton(onClick = { showStartDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select start date")
                        }
                    },
                    placeholder = { Text("Select start date") }
                )


                // End Date input with DatePicker
                OutlinedTextField(
                    value = endDateText,
                    onValueChange = { },
                    label = { Text("Ending date of contract") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showEndDatePicker = true },
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
                        IconButton(onClick = { showEndDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select date")
                        }
                    },
                    placeholder = { Text("Select end date") }
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

                        // Use selected end date or default to 1 year from now
                        val finalEndDate = endDate ?: run {
                            val endCalendar = Calendar.getInstance()
                            endCalendar.add(Calendar.YEAR, 1)
                            endCalendar.time
                        }

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
                                endDate = finalEndDate,
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

    // DatePickerDialog
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate?.time ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            endDate = Date(millis)
                            endDateText =
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(endDate)
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)


        }
    }
// End DatePickerDialog
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate?.time ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            endDate = Date(millis)
                            endDateText = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(endDate)
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}



