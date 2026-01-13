package site.sunmeat.weathernow

/**
 * UI-модель прогноза на день.
 * iconRes — drawable для иконки погоды.
 */
data class DayForecastUi(
    val day: String,
    val desc: String,
    val temp: String,
    val iconRes: Int
)
