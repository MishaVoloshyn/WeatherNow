package site.sunmeat.weathernow

/**
 * UI-модель города.
 * Как объяснить преподу:
 * - Мы храним координаты (lat/lon), чтобы запросы к погодному API были однозначными.
 * - Названия городов могут повторяться в разных странах, координаты решают проблему.
 */
data class CityUi(
    val name: String,
    val condition: String,
    val temp: String,
    val minMax: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)
