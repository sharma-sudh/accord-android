package com.sudh.accord.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sudh.accord.model.Task
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

// The backend parses dueDate with LocalDate.parse (ISO "yyyy-MM-dd"), so the
// picker below stores millis and only ever converts to that exact format —
// no free-text entry that could send something it can't parse.
private val displayDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

private fun Long.toIsoDateString(): String =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate().toString()

private fun Long.toDisplayDateString(): String =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate().format(displayDateFormatter)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskSheet(
    onDismiss: () -> Unit,
    onTaskAdded: (Task) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var taskName        by remember { mutableStateOf("") }
    var taskValue       by remember { mutableStateOf("") }
    var isRecurring     by remember { mutableStateOf(true) }
    var recurrenceType  by remember { mutableStateOf("daily") }
    var dueDateMillis   by remember { mutableStateOf<Long?>(null) }
    var showDatePicker  by remember { mutableStateOf(false) }
    var description     by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Add Task", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = taskName,
                onValueChange = { taskName = it },
                label = { Text("Task name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = taskValue,
                onValueChange = { taskValue = it },
                label = { Text("Reward (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // Recurring vs One-off toggle
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf("Recurring" to true, "One-off" to false).forEachIndexed { index, (label, value) ->
                    SegmentedButton(
                        selected = isRecurring == value,
                        onClick  = { isRecurring = value },
                        shape    = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                        label    = { Text(label) }
                    )
                }
            }

            if (isRecurring) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf("Daily", "Weekly", "Monthly").forEachIndexed { index, label ->
                        SegmentedButton(
                            selected = recurrenceType == label.lowercase(),
                            onClick  = { recurrenceType = label.lowercase() },
                            shape    = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                            label    = { Text(label) }
                        )
                    }
                }
            } else {
                OutlinedTextField(
                    value = dueDateMillis?.toDisplayDateString() ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Due date") },
                    placeholder = { Text("Select a date") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pick due date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    val task = Task(
                        id             = UUID.randomUUID().toString(),
                        title          = taskName,
                        value          = taskValue.toDoubleOrNull() ?: 0.0,
                        isRecurring    = isRecurring,
                        recurrenceType = if (isRecurring) recurrenceType.uppercase() else null,
                        dueDate        = if (!isRecurring) dueDateMillis?.toIsoDateString() else null,
                        description    = description.takeIf { it.isNotBlank() }
                    )
                    onTaskAdded(task)
                    scope.launch { sheetState.hide() }
                        .invokeOnCompletion { onDismiss() }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Task")
            }

            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.ime))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}