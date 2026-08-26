package com.sudh.accord

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.sudh.accord.navigation.NavGraph
import com.sudh.accord.ui.theme.AccordTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AccordApplication

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

        setContent {
            AccordTheme {
                NavGraph()
            }
        }
    }
}