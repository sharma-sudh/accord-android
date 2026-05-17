// NavGraph.kt
package com.sudh.accord.navigation

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sudh.accord.model.Task
import com.sudh.accord.screens.*
import com.sudh.accord.components.*

// ── Fake data ────────────────────────────────────────────────────────────────

private val fakeTasks = listOf(
    Task(id = 1, title = "Morning workout",    value = 50.0,  isRecurring = true,  recurrenceType = "daily"),
    Task(id = 2, title = "Read for 30 mins",   value = 30.0,  isRecurring = true,  recurrenceType = "daily"),
    Task(id = 3, title = "No junk food today", value = 20.0,  isRecurring = true,  recurrenceType = "daily"),
    Task(id = 4, title = "Submit assignment",   value = 100.0, isRecurring = false,
        dueDate = "2025-05-20", description = "DAA assignment, upload on vtop"),
    Task(id = 5, title = "Call home",           value = 25.0,  isRecurring = false),
)

private const val FAKE_WALLET_BALANCE = 340.0
private const val FAKE_AMOUNT_SPENT   = 160.0
private const val FAKE_MONTHLY_BUDGET = 2000.0
private const val FAKE_STREAK_DAYS    = 7
private const val FAKE_TOTAL_EARNED    = 2450.0
private const val FAKE_COMPLETION_RATE = 0.72f

// ── NavGraph ─────────────────────────────────────────────────────────────────

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    var isFabExpanded      by remember { mutableStateOf(false) }
    var isAddTaskSheetOpen by remember { mutableStateOf(false) }
    var tasks              by remember { mutableStateOf(fakeTasks) }

    val onTaskComplete: (Task) -> Unit = { completed -> tasks = tasks.filterNot { it.id == completed.id } }
    val onTaskDelete:   (Task) -> Unit = { deleted   -> tasks = tasks.filterNot { it.id == deleted.id   } }

    val bottomNavRoutes = listOf(Screen.HomeScreen.route, Screen.AnalyticsScreen.route)

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isFabExpanded) Modifier.height(220.dp) else Modifier.wrapContentHeight())
                ) {
                    if (isFabExpanded) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 72.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ExtendedFloatingActionButton(
                                onClick = { isFabExpanded = false; isAddTaskSheetOpen = true },
                                icon    = { Icon(Icons.Default.Add, contentDescription = "Add Task") },
                                text    = { Text("Add Task") }
                            )
                            ExtendedFloatingActionButton(
                                onClick = {
                                    isFabExpanded = false
                                    navController.navigate(Screen.QrScannerScreen.route)
                                },
                                icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "QR Scanner") },
                                text = { Text("QR Scanner") }
                            )
                        }
                    }

                    Surface(
                        tonalElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(onClick = {
                                isFabExpanded = false
                                navController.navigate(Screen.HomeScreen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true; restoreState = true
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Home",
                                    tint = if (currentRoute == Screen.HomeScreen.route)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            FloatingActionButton(
                                onClick      = { isFabExpanded = !isFabExpanded },
                                shape        = CircleShape,
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Icon(
                                    imageVector    = Icons.Default.Add,
                                    contentDescription = if (isFabExpanded) "Collapse" else "Expand"
                                )
                            }

                            IconButton(onClick = {
                                isFabExpanded = false
                                navController.navigate(Screen.AnalyticsScreen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true; restoreState = true
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = "Analytics",
                                    tint = if (currentRoute == Screen.AnalyticsScreen.route)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isFabExpanded) {
            Box(modifier = Modifier.fillMaxSize().clickable { isFabExpanded = false })
        }

        NavHost(
            navController    = navController,
            startDestination = Screen.LoginScreen.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Screen.LoginScreen.route)      { LoginScreen(navController) }
            composable(Screen.OnboardingScreen.route) { OnboardingScreen(navController) }

            composable(Screen.HomeScreen.route) {
                HomeScreen(
                    tasks         = tasks,
                    walletBalance = FAKE_WALLET_BALANCE,
                    amountSpent   = FAKE_AMOUNT_SPENT,
                    monthlyBudget = FAKE_MONTHLY_BUDGET,
                    streakDays    = FAKE_STREAK_DAYS,
                    onTaskComplete = onTaskComplete,
                    onTaskDelete   = onTaskDelete
                )
            }

            composable(Screen.AnalyticsScreen.route) {
                AnalyticsScreen(
                    totalEarned    = FAKE_TOTAL_EARNED,
                    totalSpent     = FAKE_AMOUNT_SPENT,
                    completionRate = FAKE_COMPLETION_RATE,
                    streakDays     = FAKE_STREAK_DAYS,
                    tasks          = tasks
                )
            }

            // ── QR Scanner — no args ──────────────────────────────────────────
            composable(Screen.QrScannerScreen.route) {
                QrScannerScreen(
                    navController = navController,
                    onQrDecoded   = { merchantName, upiId ->
                        navController.navigate(
                            Screen.AmountInputScreen.routeWith(merchantName, upiId)
                        )
                    }
                )
            }

            // ── Amount Input — merchantName + upiId ───────────────────────────
            composable(
                route     = Screen.AmountInputScreen.route,
                arguments = Screen.AmountInputScreen.arguments
            ) { backStackEntry ->
                val merchantName = backStackEntry.arguments
                    ?.getString(Screen.AmountInputScreen.ARG_MERCHANT_NAME).orEmpty()
                val upiId = backStackEntry.arguments
                    ?.getString(Screen.AmountInputScreen.ARG_UPI_ID).orEmpty()

                AmountInputScreen(
                    navController = navController,
                    merchantName  = merchantName,
                    upiId         = upiId,
                    walletBalance = FAKE_WALLET_BALANCE,
                    onConfirm     = { amount ->
                        navController.navigate(
                            Screen.PaymentConfirmScreen.routeWith(merchantName, upiId, amount)
                        )
                    }
                )
            }

            // ── Payment Confirm — merchantName + upiId + amount ───────────────
            composable(
                route     = Screen.PaymentConfirmScreen.route,
                arguments = Screen.PaymentConfirmScreen.arguments
            ) { backStackEntry ->
                val args = backStackEntry.arguments
                val merchantName = args?.getString(Screen.PaymentConfirmScreen.ARG_MERCHANT_NAME).orEmpty()
                val upiId        = args?.getString(Screen.PaymentConfirmScreen.ARG_UPI_ID).orEmpty()
                // NavType.FloatType — cast back to Double for the screen
                val amount = args?.getString(Screen.PaymentConfirmScreen.ARG_AMOUNT)?.toDoubleOrNull() ?: 0.0
                
                PaymentConfirmScreen(
                    navController = navController,
                    merchantName  = merchantName,
                    upiId         = upiId,
                    amount        = amount
                )
            }
        }

        if (isAddTaskSheetOpen) {
            AddTaskSheet(
                onDismiss   = { isAddTaskSheetOpen = false },
                onTaskAdded = { newTask ->
                    tasks = tasks + newTask
                    isAddTaskSheetOpen = false
                }
            )
        }
    }
}