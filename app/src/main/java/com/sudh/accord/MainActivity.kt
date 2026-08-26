package com.sudh.accord

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sudh.accord.navigation.NavGraph
import com.sudh.accord.ui.theme.AccordTheme
import com.sudh.accord.viewmodel.ThemeViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var app: AccordApplication

    // Registered as a class property (not inline in onCreate) per the
    // activity-result contract requirement that it happen before onStart.
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            app.notificationPrefs.hasRequestedOnce = true
            if (!granted) {
                app.notificationPrefs.deniedAtMillis = System.currentTimeMillis()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        app = application as AccordApplication

        // Once per app session, not on task completion — a conscious
        // "opened the app today" signal. Guarded on the Application instance
        // so rotating the device (which recreates this Activity) doesn't
        // double-fire the network call.
        if (!app.hasCheckedInThisSession) {
            app.hasCheckedInThisSession = true
            lifecycleScope.launch {
                val token = app.tokenManager.getToken() ?: return@launch
                app.streakRepository.checkIn("Bearer $token")
                    .onSuccess { dto -> app.setCurrentStreak(dto.currentStreak) }
            }
        }

        maybeRequestNotificationPermission()

        setContent {
            // ThemeViewModel is obtained here (activity-scoped) and again
            // from within NavGraph's top bar — Compose resolves both to the
            // same instance via the default ViewModelStoreOwner, so the
            // toggle there and the color scheme applied here always agree.
            val themeViewModel: ThemeViewModel = viewModel()
            val darkThemeOverride by themeViewModel.darkThemeOverride.collectAsStateWithLifecycle()
            val useDarkTheme = darkThemeOverride ?: isSystemInDarkTheme()

            AccordTheme(darkTheme = useDarkTheme) {
                NavGraph()
            }
        }
    }

    // Per the design doc: ask for POST_NOTIFICATIONS exactly once, ever. If
    // denied, HomeScreen shows a single soft nudge after a week — see
    // HomeViewModel's notification-nudge handling — and we never ask again.
    private fun maybeRequestNotificationPermission() {
        if (app.notificationPrefs.hasRequestedOnce) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // Pre-33 there's no runtime dialog to show — mark it handled so
            // this whole check is a no-op on every later launch.
            app.notificationPrefs.hasRequestedOnce = true
        }
    }
}