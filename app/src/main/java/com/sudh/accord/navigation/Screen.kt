package com.sudh.accord.navigation

sealed class Screen (val route: String) {
    object LoginScreen : Screen("login_screen")
    object OnboardingScreen : Screen("onboarding_screen")
    object HomeScreen : Screen("home_screen")
    object AnalyticsScreen : Screen("analytics_screen")
    object QrScannerScreen : Screen("qr_scanner_screen")
    object AmountInputScreen : Screen("amount_input_screen")
    object PaymentConfirmScreen : Screen("payment_confirm_screen")
}