package com.sudh.accord.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sudh.accord.navigation.Screen

private enum class ConfirmState { Idle, RetryPrompt }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentConfirmScreen(
    navController: NavController,
    merchantName: String,
    upiId: String,
    amount: Double
) {
    var state by remember { mutableStateOf(ConfirmState.Idle) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm Payment") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
        ) {
            // ── Payment summary card ──────────────────────────────────────────
            Surface(
                shape          = MaterialTheme.shapes.large,
                tonalElevation = 2.dp,
                modifier       = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier            = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text  = "₹${"%.2f".format(amount)}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    SummaryRow(label = "To",  value = merchantName)
                    SummaryRow(label = "UPI", value = upiId)
                }
            }

            // ── Instruction text ──────────────────────────────────────────────
            Text(
                text      = "Complete the payment in your UPI app,\nthen confirm below.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // ── Animated bottom section ───────────────────────────────────────
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    fadeIn() + slideInVertically { it / 2 } togetherWith
                            fadeOut() + slideOutVertically { -it / 2 }
                },
                label = "confirmState"
            ) { currentState ->
                when (currentState) {
                    ConfirmState.Idle -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text  = "Did the payment go through?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                modifier            = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Yes — deduct from wallet and go Home
                                Button(
                                    onClick  = {
                                        // TODO: call ViewModel to deduct amount from Room wallet
                                        navController.navigate(Screen.HomeScreen.route) {
                                            popUpTo(Screen.HomeScreen.route) { inclusive = false }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        imageVector  = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier     = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Yes, paid")
                                }

                                // No — surface retry prompt
                                OutlinedButton(
                                    onClick  = { state = ConfirmState.RetryPrompt },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                ) {
                                    Icon(
                                        imageVector  = Icons.Default.Cancel,
                                        contentDescription = null,
                                        modifier     = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("No")
                                }
                            }
                        }
                    }

                    ConfirmState.RetryPrompt -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text      = "No problem — nothing has been deducted.",
                                style     = MaterialTheme.typography.bodyMedium,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick  = { state = ConfirmState.Idle },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Text("Try payment again")
                            }

                            TextButton(
                                onClick  = {
                                    navController.navigate(Screen.HomeScreen.route) {
                                        popUpTo(Screen.HomeScreen.route) { inclusive = false }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel — go back home")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}