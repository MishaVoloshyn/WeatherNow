package site.sunmeat.weathernow

data class HourlyForecastUi(
    val timeLabel: String,   // "Now", "20", "21"
    val temp: String,        // "1°"
    val iconRes: Int         // drawable id
)
