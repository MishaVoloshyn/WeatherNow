package site.sunmeat.weathernow.network

import retrofit2.http.GET
import retrofit2.http.Query
import site.sunmeat.weathernow.network.dto.ForecastResponse

interface WeatherApi {

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,

        // current
        @Query("current") current: String = "temperature_2m",

        // daily
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,weather_code",

        // hourly (НОВОЕ)
        @Query("hourly") hourly: String = "temperature_2m,weather_code",

        @Query("timezone") timezone: String = "auto"
    ): ForecastResponse
}
