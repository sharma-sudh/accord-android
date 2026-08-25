package com.sudh.accord.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

// Single source of truth for the "upi://pay" deep link — used both for the
// initial pay attempt (AmountInputScreen) and for re-firing the same intent
// on retry (PaymentConfirmScreen), so the two can't drift apart.
object UpiPayment {

    fun buildIntent(merchantName: String, upiId: String, amount: Double): Intent {
        val upiUri = "upi://pay".toUri().buildUpon()
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", merchantName)
            .appendQueryParameter("am", "%.2f".format(amount))
            .appendQueryParameter("cu", "INR")
            .build()
        return Intent(Intent.ACTION_VIEW, upiUri)
    }

    // Attempts to launch a UPI app for this payment. Returns a user-facing
    // error message on failure, or null on apparent success.
    //
    // Deliberately does NOT gate on Intent.resolveActivity() first —
    // resolveActivity() is unreliable on modern Android (well-documented to
    // return null even when a matching app is installed, particularly once
    // more than one app can handle the intent and the package-visibility
    // <queries> declaration is involved). startActivity()'s own
    // ActivityNotFoundException is the only trustworthy signal here.
    fun launch(context: Context, merchantName: String, upiId: String, amount: Double): String? {
        val upiIntent = buildIntent(merchantName, upiId, amount)

        return try {
            context.startActivity(upiIntent)
            null
        } catch (e: ActivityNotFoundException) {
            "No UPI app found on this device"
        }
    }
}