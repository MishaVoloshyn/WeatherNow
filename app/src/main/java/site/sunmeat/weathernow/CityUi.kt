package site.sunmeat.weathernow

data class CityUi(
    val name: String,
    val condition: String,
    val temp: String,
    val minMax: String,
    val latitude: Double?,
    val longitude: Double?,
    val isFavorite: Boolean = false
)
