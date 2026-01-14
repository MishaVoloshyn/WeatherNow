package site.sunmeat.weathernow.storage

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "theme_settings")

object ThemeStore {

    private val KEY_THEME_MODE = intPreferencesKey("theme_mode")

    /**
     * 0 = System
     * 1 = Light
     * 2 = Dark
     */
    fun themeFlow(context: Context): Flow<Int> {
        return context.themeDataStore.data.map { prefs ->
            prefs[KEY_THEME_MODE] ?: 0
        }
    }

    suspend fun setTheme(context: Context, mode: Int) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode
        }
    }

    fun applyTheme(mode: Int) {
        val nightMode = when (mode) {
            1 -> AppCompatDelegate.MODE_NIGHT_NO
            2 -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
