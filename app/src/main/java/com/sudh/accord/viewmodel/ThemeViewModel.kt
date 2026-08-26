package com.sudh.accord.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sudh.accord.AccordApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val themePreferences = getApplication<AccordApplication>().themePreferences

    // null = no manual override yet — the composable falls back to
    // isSystemInDarkTheme() for that case. WhileSubscribed(5000) rather than
    // Eagerly since this is read from both MainActivity's setContent root
    // and NavGraph's top bar, and neither should keep the DataStore flow
    // alive indefinitely after both stop collecting (e.g. process death).
    val darkThemeOverride: StateFlow<Boolean?> = themePreferences.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Takes the currently-effective value (override, or the system default
    // when there's no override yet) rather than reading darkThemeOverride
    // itself, so the very first tap flips relative to what's on screen
    // instead of relative to a null override.
    fun toggleDarkTheme(currentlyDark: Boolean) {
        viewModelScope.launch {
            themePreferences.setDarkTheme(!currentlyDark)
        }
    }
}