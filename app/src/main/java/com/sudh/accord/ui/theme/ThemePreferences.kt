package com.sudh.accord.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "accord_theme_prefs")

/**
 * Persists the user's manual light/dark override, set from the toggle in
 * NavGraph's top bar. DataStore (not SharedPreferences) deliberately — this
 * is new code and DataStore is the currently-recommended replacement:
 * Flow-based reads, no synchronous disk I/O on the calling thread, and a
 * transactional write API.
 *
 * `isDarkTheme` reads back null until the user has ever touched the
 * toggle — that "unset" state matters, since AccordTheme should keep
 * following the system setting until there's an explicit override to
 * honor instead.
 */
class ThemePreferences(context: Context) {

    private val appContext = context.applicationContext

    val isDarkTheme: Flow<Boolean?> = appContext.themeDataStore.data.map { prefs ->
        prefs[KEY_DARK_THEME]
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        appContext.themeDataStore.edit { prefs ->
            prefs[KEY_DARK_THEME] = enabled
        }
    }

    companion object {
        private val KEY_DARK_THEME = booleanPreferencesKey("dark_theme_enabled")
    }
}