package com.sudh.accord.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sudh.accord.auth.SessionManager
import com.sudh.accord.screens.*
import com.sudh.accord.components.*
import com.sudh.accord.viewmodel.AnalyticsViewModel
import com.sudh.accord.viewmodel.ForgotPasswordViewModel
import com.sudh.accord.viewmodel.HomeViewModel
import com.sudh.accord.viewmodel.OnboardingViewModel
import com.sudh.accord.viewmodel.PaymentViewModel
import com.sudh.accord.viewmodel.ThemeViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val homeViewModel: HomeViewModel               = viewModel()
    val analyticsViewModel: AnalyticsViewModel     = viewModel()
    val onboardingViewModel: OnboardingViewModel   = viewModel()
    val forgotPasswordViewModel: ForgotPasswordViewModel = viewModel()
    // Same activity-scoped instance MainActivity's setContent root reads —
    // see the comment there — so toggling here updates the actual applied
    // color scheme, not a second disconnected copy of it.
    val themeViewModel: ThemeViewModel              = viewModel()

    val homeUiState      by homeViewModel.uiState.collectAsStateWithLifecycle()
    val analyticsUiState by analyticsViewModel.uiState.collectAsStateWithLifecycle()
    val darkThemeOverride by themeViewModel.darkThemeOverride.collectAsStateWithLifecycle()
    val isDarkTheme = darkThemeOverride ?: isSystemInDarkTheme()

    var isFabExpanded      by remember { mutableStateOf(false) }
    var isAddTaskSheetOpen by remember { mutableStateOf(false) }
    var isSettingsSheetOpen by remember { mutableStateOf(false) }

    // Backs the swipeable Home/Analytics pager (see the Screen.HomeScreen.route
    // composable below). Hoisted here, not inside that composable, so the
    // bottom dock — which lives in the Scaffold's bottomBar slot, a sibling
    // of the NavHost — can both read the current page (for its selected
    // state) and drive it (tapping a dock item scrolls the pager instead of
    // navigating, since Home/Analytics are now pages, not separate routes).
    val pagerState = rememberPagerState(pageCount = { 2 })
    val pagerScope = rememberCoroutineScope()

    // Forced logout: emitted by TokenAuthenticator (on an OkHttp background
    // thread) when a refresh attempt fails because the refresh token itself
    // is expired or revoked. Clear the back stack so the user can't navigate
    // "back" into now-stale authenticated screens.
    LaunchedEffect(Unit) {
        SessionManager.sessionExpired.collect {
            navController.navigate(Screen.LoginScreen.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val bottomNavRoutes = listOf(Screen.HomeScreen.route)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // No gear icon existed anywhere in the app before this — there
            // was no top bar on Home/Analytics at all, so this adds one
            // rather than wiring up a pre-existing tap handler.
            if (currentRoute in bottomNavRoutes) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = { themeViewModel.toggleDarkTheme(isDarkTheme) }) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDarkTheme) "Switch to light mode" else "Switch to dark mode"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSettingsSheetOpen = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isFabExpanded) Modifier.height(240.dp) else Modifier.height(112.dp))
                ) {
                    if (isFabExpanded) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 104.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ExtendedFloatingActionButton(
                                onClick = { isFabExpanded = false; isAddTaskSheetOpen = true },
                                icon    = { Icon(Icons.Default.Add, contentDescription = "Add Task") },
                                text    = { Text("Add Task") },
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                            )
                            ExtendedFloatingActionButton(
                                onClick = {
                                    isFabExpanded = false
                                    navController.navigate(Screen.QrScannerScreen.route)
                                },
                                icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "QR Scanner") },
                                text = { Text("QR Scanner") },
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                            )
                        }
                    }

                    // Floating pill dock — inset from the edges instead of a
                    // flush edge-to-edge bar, with the FAB raised above it.
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 28.dp)
                            .padding(bottom = 24.dp)
                            .fillMaxWidth()
                            .height(64.dp),
                        shape           = RoundedCornerShape(32.dp),
                        color           = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation  = 3.dp,
                        shadowElevation = 10.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DockNavItem(
                                icon      = Icons.Default.Home,
                                label     = "Home",
                                selected  = pagerState.currentPage == 0,
                                onClick   = {
                                    isFabExpanded = false
                                    pagerScope.launch { pagerState.animateScrollToPage(0) }
                                }
                            )

                            Spacer(Modifier.width(64.dp)) // room for the raised FAB

                            DockNavItem(
                                icon      = Icons.Default.BarChart,
                                label     = "Analytics",
                                selected  = pagerState.currentPage == 1,
                                onClick   = {
                                    isFabExpanded = false
                                    pagerScope.launch { pagerState.animateScrollToPage(1) }
                                }
                            )
                        }
                    }

                    // Raised FAB, floating above the dock's notch.
                    FloatingActionButton(
                        onClick        = { isFabExpanded = !isFabExpanded },
                        shape          = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor   = MaterialTheme.colorScheme.onPrimary,
                        elevation      = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 40.dp)
                            .size(60.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Add,
                            contentDescription = if (isFabExpanded) "Collapse" else "Expand"
                        )
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
            composable(Screen.SignUpScreen.route)     { SignUpScreen(navController) }
            composable(Screen.ForgotPasswordScreen.route) {
                ForgotPasswordScreen(navController, forgotPasswordViewModel)
            }
            composable(Screen.OtpVerifyScreen.route) {
                OtpVerifyScreen(navController, forgotPasswordViewModel)
            }
            composable(Screen.ResetPasswordScreen.route) {
                ResetPasswordScreen(navController, forgotPasswordViewModel)
            }
            composable(Screen.OnboardingScreen.route) { OnboardingScreen(navController, onboardingViewModel) }

            composable(Screen.HomeScreen.route) {
                // Home and Analytics are pages of one pager, not separate
                // routes — deferred as tap-only in the design doc originally,
                // now wired up. Landing here always lands on page 0 (Home);
                // Analytics is reached by swiping or tapping its dock item.
                HorizontalPager(
                    state    = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> {
                            // Fires once when this page first enters
                            // composition — same load-on-arrival timing the
                            // old separate Home route had via LaunchedEffect(Unit).
                            LaunchedEffect(Unit) {
                                homeViewModel.loadData()
                            }
                            HomeScreen(
                                tasks              = homeUiState.tasks,
                                walletBalance      = homeUiState.walletBalance,
                                amountSpent        = homeUiState.amountSpent,
                                monthlyBudget      = homeUiState.monthlyBudget,
                                streakDays         = homeUiState.streakDays,
                                isLoading          = homeUiState.isLoading,
                                error              = homeUiState.error,
                                actionError        = homeUiState.actionError,
                                showNotificationNudge      = homeUiState.showNotificationNudge,
                                hasEverAddedTask   = homeUiState.hasEverAddedTask,
                                onDismissNotificationNudge = homeViewModel::dismissNotificationNudge,
                                onRetry            = homeViewModel::loadData,
                                onTaskComplete     = homeViewModel::completeTask,
                                onTaskDelete       = homeViewModel::deleteTask,
                                onActionErrorShown = homeViewModel::clearActionError,
                            )
                        }

                        1 -> {
                            LaunchedEffect(Unit) {
                                analyticsViewModel.loadAnalytics()
                            }
                            AnalyticsScreen(
                                selectedRange  = analyticsUiState.selectedRange,
                                totalEarned    = analyticsUiState.totalEarned,
                                totalSpent     = analyticsUiState.totalSpent,
                                completionRate = analyticsUiState.completionRate,
                                streakDays     = analyticsUiState.streakDays,
                                series         = analyticsUiState.series,
                                taskBreakdown  = analyticsUiState.taskBreakdown,
                                isEmptyState   = analyticsUiState.isEmptyState,
                                isLoading      = analyticsUiState.isLoading,
                                error          = analyticsUiState.error,
                                onRangeSelect  = analyticsViewModel::selectRange,
                                // loadAnalytics has a default parameter, and a bare method
                                // reference doesn't apply defaults — needs a lambda to satisfy
                                // the () -> Unit shape onRetry expects.
                                onRetry        = { analyticsViewModel.loadAnalytics() },
                            )
                        }
                    }
                }
            }

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
                    walletBalance = homeUiState.walletBalance,
                    onConfirm     = { amount ->
                        navController.navigate(
                            Screen.PaymentConfirmScreen.routeWith(merchantName, upiId, amount)
                        )
                    }
                )
            }

            composable(
                route     = Screen.PaymentConfirmScreen.route,
                arguments = Screen.PaymentConfirmScreen.arguments
            ) { backStackEntry ->
                val args         = backStackEntry.arguments
                val merchantName = args?.getString(Screen.PaymentConfirmScreen.ARG_MERCHANT_NAME).orEmpty()
                val upiId        = args?.getString(Screen.PaymentConfirmScreen.ARG_UPI_ID).orEmpty()
                val amount       = args?.getString(Screen.PaymentConfirmScreen.ARG_AMOUNT)?.toDoubleOrNull() ?: 0.0

                // Scoped to this back stack entry — payment state shouldn't
                // outlive the confirmation screen itself.
                val paymentViewModel: PaymentViewModel = viewModel()
                val paymentUiState by paymentViewModel.uiState.collectAsStateWithLifecycle()

                PaymentConfirmScreen(
                    navController    = navController,
                    merchantName     = merchantName,
                    upiId            = upiId,
                    amount           = amount,
                    isSubmitting     = paymentUiState.isSubmitting,
                    submitError      = paymentUiState.error,
                    onConfirmPayment = {
                        paymentViewModel.confirmPayment(merchantName, amount) {
                            navController.navigate(Screen.HomeScreen.route) {
                                popUpTo(Screen.HomeScreen.route) { inclusive = false }
                            }
                        }
                    }
                )
            }
        }

        if (isAddTaskSheetOpen) {
            AddTaskSheet(
                onDismiss   = { isAddTaskSheetOpen = false },
                onTaskAdded = { newTask ->
                    homeViewModel.addTask(newTask)
                    isAddTaskSheetOpen = false
                }
            )
        }

        if (isSettingsSheetOpen) {
            SettingsSheet(
                onDismiss   = { isSettingsSheetOpen = false },
                onSignedOut = {
                    isSettingsSheetOpen = false
                    // Same pattern as the forced-logout path above: pop the
                    // whole back stack so a signed-out user can't navigate
                    // "back" into now-stale authenticated screens.
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}