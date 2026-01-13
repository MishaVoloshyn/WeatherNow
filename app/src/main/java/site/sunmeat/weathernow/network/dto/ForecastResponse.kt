package site.sunmeat.weathernow.network.dto

import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    @SerializedName("current") val current: CurrentDto?,
    @SerializedName("daily") val daily: DailyDto?
)

data class CurrentDto(
    @SerializedName("temperature_2m") val temperature: Double?
)

data class DailyDto(
    @SerializedName("time") val time: List<String>?,
    @SerializedName("temperature_2m_max") val max: List<Double>?,
    @SerializedName("temperature_2m_min") val min: List<Double>?,
    @SerializedName("weather_code") val weatherCode: List<Int>?
)
