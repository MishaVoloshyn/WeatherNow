package site.sunmeat.weathernow.util

import site.sunmeat.weathernow.R

/**
 * Маппинг кодов Open-Meteo -> текст + иконка.
 * Как объяснить преподу:
 * - API отдаёт числовые weather_code
 * - Мы делаем слой преобразования для UI (человекочитаемо и красиво)
 */
object WeatherCodeMapper {

    fun toText(code: Int?): String = when (code) {
        0 -> "Clear"
        1, 2 -> "Partly cloudy"
        3 -> "Cloudy"
        45, 48 -> "Fog"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        71, 73, 75 -> "Snow"
        80, 81, 82 -> "Rain showers"
        95 -> "Thunderstorm"
        else -> "—"
    }

    fun toIcon(code: Int?): Int = when (code) {
        0 -> R.drawable.ic_weather_sun
        1, 2, 3 -> R.drawable.ic_weather_cloud
        45, 48 -> R.drawable.ic_weather_fog
        51, 53, 55, 61, 63, 65, 80, 81, 82, 95 -> R.drawable.ic_weather_rain
        71, 73, 75 -> R.drawable.ic_weather_snow
        else -> R.drawable.ic_weather_cloud
    }
}
