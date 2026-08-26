package com.sudh.accord

import android.app.Application
import com.sudh.accord.auth.TokenManager
import com.sudh.accord.network.RetrofitClient
import com.sudh.accord.notifications.NarrativeScheduler
import com.sudh.accord.notifications.NotificationChannels
import com.sudh.accord.notifications.NotificationPrefs
import com.sudh.accord.notifications.NotificationTogglePrefs
import com.sudh.accord.notifications.WalletPressureScheduler
import com.sudh.accord.repository.AnalyticsRepository
import com.sudh.accord.repository.AuthRepository
import com.sudh.accord.repository.NarrativeRepository
import com.sudh.accord.repository.PaymentRepository
import com.sudh.accord.repository.StreakRepository
import com.sudh.accord.repository.TaskRepository
import com.sudh.accord.repository.UserRepository
import com.sudh.accord.ui.theme.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccordApplication : Application() {

    lateinit var tokenManager: TokenManager
    lateinit var taskRepository: TaskRepository
    lateinit var authRepository: AuthRepository
    lateinit var userRepository: UserRepository
    lateinit var analyticsRepository: AnalyticsRepository
    lateinit var paymentRepository: PaymentRepository
    lateinit var streakRepository: StreakRepository
    lateinit var narrativeRepository: NarrativeRepository
    lateinit var notificationPrefs: NotificationPrefs
    lateinit var notificationTogglePrefs: NotificationTogglePrefs
    lateinit var themePreferences: ThemePreferences

    // Session-scoped streak result, set once by the single checkin call made
    // at app open (see MainActivity). HomeViewModel and AnalyticsViewModel
    // both collect this rather than each owning their own copy, since a
    // single checkin should update both screens' streak displays. Null until
    // that first checkin call resolves.
    private val _currentStreak = MutableStateFlow<Int?>(null)
    val currentStreak: StateFlow<Int?> = _currentStreak.asStateFlow()

    fun setCurrentStreak(streak: Int) {
        _currentStreak.value = streak
    }

    // Guards the app-open checkin call against firing again on an Activity
    // recreation (e.g. rotation) within the same process — set right before
    // the call is dispatched, not after it resolves.
    var hasCheckedInThisSession = false

    override fun onCreate() {
        super.onCreate()
        tokenManager = TokenManager(this)
        RetrofitClient.init(tokenManager) // must run before any repository touches RetrofitClient.api
        taskRepository = TaskRepository()
        authRepository = AuthRepository()
        userRepository  = UserRepository()
        analyticsRepository = AnalyticsRepository()
        paymentRepository = PaymentRepository()
        streakRepository = StreakRepository()
        narrativeRepository = NarrativeRepository()
        notificationPrefs = NotificationPrefs(this)
        notificationTogglePrefs = NotificationTogglePrefs(this)
        themePreferences = ThemePreferences(this)
        NotificationChannels.ensureCreated(this)
        WalletPressureScheduler.ensureScheduled(this)
        NarrativeScheduler.ensureScheduled(this)
    }
}