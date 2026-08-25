package com.sudh.accord.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sudh.accord.util.UpiPayment


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountInputScreen(
    navController: NavController,
    merchantName:  String,
    upiId:         String,
    walletBalance: Double,
    onConfirm:     (Double) -> Unit
) {
    val context      = LocalContext.current
    var rawInput     by remember { mutableStateOf("") }
    var showError    by remember { mutableStateOf(false) }
    var launchError  by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    val parsedAmount: Double? = rawInput.toDoubleOrNull()
    val isOverspend   = parsedAmount != null && parsedAmount > walletBalance
    val isInputValid  = parsedAmount != null && parsedAmount > 0.0

    val errorMessage = when {
        showError && rawInput.isBlank()   -> "Please enter an amount"
        showError && parsedAmount == null -> "Enter a valid number"
        launchError != null                -> launchError
        else                               -> null
    }

    fun attemptConfirm() {
        showError   = true
        launchError = null
        val amount  = parsedAmount
        if (!isInputValid || amount == null) return

        val error = UpiPayment.launch(context, merchantName, upiId, amount)
        if (error != null) {
            launchError = error
            return
        }

        focusManager.clearFocus()
        // Navigate after a successful launch: by the time the user comes
        // back from the UPI app (GPay/PhonePe/etc, chosen via the OS's own
        // disambiguation dialog since we don't force a chooser),
        // PaymentConfirmScreen is already the back-stack head.
        onConfirm(amount)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enter Amount") },
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
            // ── Merchant card ─────────────────────────────────────────────────
            Surface(
                shape         = MaterialTheme.shapes.large,
                tonalElevation = 2.dp,
                modifier      = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier            = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text  = "Paying",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text      = merchantName,
                        style     = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center
                    )
                    Text(
                        text  = upiId,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Wallet balance chip ───────────────────────────────────────────
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text  = "Wallet balance",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text       = "₹${"%.2f".format(walletBalance)}",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // ── Amount field ──────────────────────────────────────────────────
            OutlinedTextField(
                value         = rawInput,
                onValueChange = { input ->
                    // Allow digits and at most one decimal point, max 2 decimal places
                    val filtered = input.filter { it.isDigit() || it == '.' }
                    val dotIndex = filtered.indexOf('.')
                    val clamped  = if (dotIndex != -1) {
                        filtered.substring(0, minOf(dotIndex + 3, filtered.length))
                    } else filtered
                    rawInput    = clamped
                    showError   = false
                    launchError = null
                },
                modifier       = Modifier.fillMaxWidth(),
                label          = { Text("Amount (₹)") },
                prefix         = { Text("₹ ") },
                isError        = errorMessage != null,
                supportingText = errorMessage?.let { msg -> { Text(msg) } },
                singleLine     = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { attemptConfirm() }),
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    textAlign  = TextAlign.Start,
                    fontWeight = FontWeight.Medium
                )
            )

            // ── Overspend warning — friction, not a block. The pay button
            // below stays enabled and onConfirm still fires; this only
            // informs, matching the app's "no enforcement" design.
            if (isOverspend) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier           = Modifier.size(20.dp)
                        )
                        Text(
                            text  = "This is more than your wallet balance — you can still pay.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            // ── Confirm button ────────────────────────────────────────────────
            Button(
                onClick  = { attemptConfirm() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled  = rawInput.isNotBlank()
            ) {
                Text(
                    text  = if (parsedAmount != null && parsedAmount > 0)
                        "Pay ₹${"%.2f".format(parsedAmount)}"
                    else
                        "Enter amount to continue",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}