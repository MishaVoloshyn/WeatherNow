package site.sunmeat.weathernow.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Stores theme mode in DataStore:
 * 0 = system, 1 = light, 2 = dark
 */
object ThemeStorage {

    private val Context.dataStore by preferencesDataStore(name = "settings_store")

    private val KEY_THEME_MODE = intPreferencesKey("theme_mode")

    const val MODE_SYSTEM = 0
    const val MODE_LIGHT = 1
    const val MODE_DARK = 2

    fun observeThemeMode(context: Context): Flow<Int> {
        return context.dataStore.data.map { prefs ->
            prefs[KEY_THEME_MODE] ?: MODE_SYSTEM
        }
    }

    suspend fun saveThemeMode(context: Context, mode: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode
        }
    }
}
