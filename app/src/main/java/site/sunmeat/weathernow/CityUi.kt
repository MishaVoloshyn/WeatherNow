package site.sunmeat.weathernow

data class CityUi(
    val name: String,
    val lat: Double,
    val lon: Double,
    val temp: String = "--°",
    val condition: String = "—",
    val minMax: String = "H:--°  L:--°"
)
