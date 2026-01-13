package site.sunmeat.weathernow.network

import retrofit2.http.GET
import retrofit2.http.Query
import site.sunmeat.weathernow.network.dto.ForecastResponse

/**
 * Прогноз погоды по координатам (7 дней).
 * Мы запрашиваем daily min/max + weather_code и current температуру.
 */
interface WeatherApi {

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,weather_code",
        @Query("timezone") timezone: String = "auto"
    ): ForecastResponse
}
