package site.sunmeat.weathernow.storage

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

object SettingsStorage {

    private val KEY_THEME_MODE = intPreferencesKey("theme_mode")

    // 0 = System, 1 = Light, 2 = Dark
    fun observeThemeMode(context: Context): Flow<Int> {
        return context.dataStore.data.map { prefs ->
            prefs[KEY_THEME_MODE] ?: 0
        }
    }

    suspend fun saveThemeMode(context: Context, mode: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode
        }
    }

    suspend fun readThemeModeOnce(context: Context): Int {
        var result = 0
        context.dataStore.data.map { prefs -> prefs[KEY_THEME_MODE] ?: 0 }
            .collect { value ->
                result = value
                return@collect
            }
        return result
    }
}
