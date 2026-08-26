package com.sudh.accord.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sudh.accord.viewmodel.SettingsEvent
import com.sudh.accord.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    onDismiss: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Sign-out is the one action here that leaves the app's authenticated
    // area entirely, so it drives its own dismiss-then-navigate sequence
    // rather than going through the per-field callbacks below.
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                SettingsEvent.SignedOut -> {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        onDismiss()
                        onSignedOut()
                    }
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Settings", style = MaterialTheme.typography.titleMedium)

            // ── Account info ─────────────────────────────────────────────────
            when {
                uiState.isLoadingProfile -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("Loading account…", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                uiState.profileError != null -> {
                    Text(
                        text = uiState.profileError ?: "Couldn't load your account",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = uiState.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = uiState.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider()

            // ── Monthly budget ───────────────────────────────────────────────
            Text("Monthly budget", style = MaterialTheme.typography.titleSmall)

            OutlinedTextField(
                value = uiState.budgetInput,
                onValueChange = viewModel::onBudgetInputChange,
                label = { Text("Budget (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.budgetSaveError != null,
                supportingText = {
                    val message = uiState.budgetSaveError
                        ?: if (uiState.budgetSaved) "Saved" else null
                    if (message != null) Text(message)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = viewModel::saveBudget,
                enabled = !uiState.isSavingBudget,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSavingBudget) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save budget")
                }
            }

            HorizontalDivider()

            // ── Notification toggles ─────────────────────────────────────────
            Text("Notifications", style = MaterialTheme.typography.titleSmall)

            NotificationToggleRow(
                label = "Wallet pressure nudges",
                checked = uiState.walletPressureEnabled,
                onCheckedChange = viewModel::setWalletPressureEnabled
            )
            NotificationToggleRow(
                label = "Sunday weekly recap",
                checked = uiState.sundayNarrativeEnabled,
                onCheckedChange = viewModel::setSundayNarrativeEnabled
            )
            NotificationToggleRow(
                label = "Task due-date reminders",
                checked = uiState.taskRemindersEnabled,
                onCheckedChange = viewModel::setTaskRemindersEnabled
            )

            HorizontalDivider()

            // ── Sign out ─────────────────────────────────────────────────────
            Button(
                onClick = viewModel::signOut,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Sign Out")
            }

            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.ime))
        }
    }
}

@Composable
private fun NotificationToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}