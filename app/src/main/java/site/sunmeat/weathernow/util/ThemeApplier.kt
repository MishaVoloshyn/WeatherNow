package site.sunmeat.weathernow.util

import androidx.appcompat.app.AppCompatDelegate

object ThemeApplier {

    // 0 = System, 1 = Light, 2 = Dark
    fun apply(mode: Int) {
        val nightMode = when (mode) {
            1 -> AppCompatDelegate.MODE_NIGHT_NO
            2 -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
