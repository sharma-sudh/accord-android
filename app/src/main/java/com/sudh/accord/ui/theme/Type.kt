package com.sudh.accord.ui.theme

import android.annotation.SuppressLint
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.sudh.accord.R

// ── Font ─────────────────────────────────────────────────────────────────
// DM Sans, loaded as a downloadable Google Font (per the design spec) so it
// doesn't have to be bundled as static assets. Requires
// androidx.compose.ui:ui-text-google-fonts — see build.gradle.kts notes.
@SuppressLint("PrivateResource")
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.android.vending",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val dmSans = GoogleFont("DM Sans")

private val DMSansFamily = FontFamily(
    Font(googleFont = dmSans, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = dmSans, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = dmSans, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = dmSans, fontProvider = provider, weight = FontWeight.Bold),
)

// ── Type scale ───────────────────────────────────────────────────────────
// Standard M3 scale, all on DM Sans. Sizing/tracking follows the language
// LoginScreen already established by hand (tight tracking on display-ish
// text, wide tracking on small labels).
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = DMSansFamily, fontWeight = FontWeight.Bold,
        fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = DMSansFamily, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp
    ),
    displaySmall = TextStyle(
        fontFamily = DMSansFamily, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = DMSansFamily, fontWeight = FontWeight.Bold,
        fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.25).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DMSansFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = DMSansFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = DMSansFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = DMSansFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = DMSansFamily, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = DMSansFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = DMSansFamily, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.15.sp
    ),
    bodySmall = TextStyle(
        fontFamily = DMSansFamily, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = DMSansFamily, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = DMSansFamily, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = DMSansFamily, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 1.5.sp
    ),
)