package com.sudh.accord.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object LoginScreen       : Screen("login_screen")
    object OnboardingScreen  : Screen("onboarding_screen")
    object HomeScreen        : Screen("home_screen")
    object AnalyticsScreen   : Screen("analytics_screen")
    object QrScannerScreen   : Screen("qr_scanner_screen")

    object AmountInputScreen : Screen("amount_input_screen/{merchantName}/{upiId}") {
        const val ARG_MERCHANT_NAME = "merchantName"
        const val ARG_UPI_ID        = "upiId"

        fun routeWith(merchantName: String, upiId: String): String =
            "amount_input_screen/${merchantName.encode()}/${upiId.encode()}"

        val arguments = listOf(
            navArgument(ARG_MERCHANT_NAME) { type = NavType.StringType },
            navArgument(ARG_UPI_ID) { type = NavType.StringType }
        )
    }

    object PaymentConfirmScreen : Screen("payment_confirm_screen/{merchantName}/{upiId}/{amount}") {
        const val ARG_MERCHANT_NAME = "merchantName"
        const val ARG_UPI_ID        = "upiId"
        const val ARG_AMOUNT        = "amount"

        fun routeWith(merchantName: String, upiId: String, amount: Double): String =
            "payment_confirm_screen/${merchantName.encode()}/${upiId.encode()}/$amount"

        val arguments = listOf(
            navArgument(ARG_MERCHANT_NAME) { type = NavType.StringType },
            navArgument(ARG_UPI_ID)        { type = NavType.StringType },
            navArgument(ARG_AMOUNT)        { type = NavType.StringType  }
        )
    }
}

/** URL-encodes spaces and other special chars so they survive the route string. */
private fun String.encode(): String = java.net.URLEncoder.encode(this, "UTF-8")