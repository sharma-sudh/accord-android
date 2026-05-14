package com.sudh.accord.navigation

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
import com.sudh.accord.screens.*
import com.sudh.accord.components.*

@Composable
fun NavGraph(){
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    var isFabExpanded by remember { mutableStateOf(false) }
    var isAddTaskSheetOpen by remember { mutableStateOf(false) }

    val bottomNavRoute = listOf(Screen.HomeScreen.route, Screen.AnalyticsScreen.route)

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoute){
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if(isFabExpanded)
                                Modifier.height(220.dp)
                            else
                                Modifier.wrapContentHeight()
                        )
                ) {
                    if (isFabExpanded) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 72.dp),  // clears the bar height
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Add Task option
                            ExtendedFloatingActionButton(
                                onClick = {
                                    isFabExpanded = false
                                    isAddTaskSheetOpen = true
                                },
                                icon = {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add Task"
                                    )
                                },
                                text = { Text("Add Task") }
                            )
                            ExtendedFloatingActionButton(
                                onClick = {
                                    isFabExpanded = false
                                    navController.navigate(Screen.QrScannerScreen.route)
                                },
                                icon = {
                                    Icon(
                                        Icons.Default.QrCodeScanner,
                                        contentDescription = "QR Scanner"
                                    )
                                },
                                text = { Text("QR Scanner") }
                            )
                        }
                    }

                    // The bar itself
                    Surface(
                        tonalElevation = 3.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = {
                                    isFabExpanded = false
                                    navController.navigate(Screen.HomeScreen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Home",
                                    tint = if (currentRoute == Screen.HomeScreen.route)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Center FAB — toggles isFabExpanded
                            FloatingActionButton(
                                onClick = { isFabExpanded = !isFabExpanded },
                                shape = CircleShape,
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = if (isFabExpanded) "Collapse" else "Expand"
                                )
                            }

                            IconButton(
                                onClick = {
                                    isFabExpanded = false
                                    navController.navigate(Screen.AnalyticsScreen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = "Analytics",
                                    tint = if (currentRoute == Screen.AnalyticsScreen.route)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        // Transparent full-screen overlay — dismisses FAB on outside tap
        if (isFabExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isFabExpanded = false }
            )
        }
        NavHost(
            navController = navController,
            startDestination  = Screen.LoginScreen.route,
            modifier = Modifier.padding(innerPadding)
        ){
            composable(Screen.LoginScreen.route){
                LoginScreen(navController = navController)
            }
            composable(Screen.OnboardingScreen.route){
                OnboardingScreen(navController = navController)
            }
            composable(Screen.HomeScreen.route){
                HomeScreen()
            }
            composable(Screen.AnalyticsScreen.route){
                AnalyticsScreen()
            }
            composable(Screen.QrScannerScreen.route){
                QrScannerScreen(navController = navController)
            }
            composable(Screen.AmountInputScreen.route){
                AmountInputScreen(navController = navController)
            }
            composable(Screen.PaymentConfirmScreen.route){
                PaymentConfirmScreen(navController = navController)
            }
        }
        if (isAddTaskSheetOpen) {
            AddTaskSheet(onDismiss = { isAddTaskSheetOpen = false })
        }
    }
}