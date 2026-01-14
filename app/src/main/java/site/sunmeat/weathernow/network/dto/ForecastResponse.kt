package site.sunmeat.weathernow.network.dto

import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,

    @SerializedName("current") val current: CurrentDto? = null,
    @SerializedName("daily") val daily: DailyDto? = null,
    @SerializedName("hourly") val hourly: HourlyDto? = null
) {
    data class CurrentDto(
        @SerializedName("time") val time: String? = null,
        @SerializedName("temperature_2m") val temperature: Double? = null
    )

    data class DailyDto(
        @SerializedName("time") val time: List<String>? = null,
        @SerializedName("temperature_2m_max") val max: List<Double>? = null,
        @SerializedName("temperature_2m_min") val min: List<Double>? = null,
        @SerializedName("weather_code") val weatherCode: List<Int>? = null
    )

    data class HourlyDto(
        @SerializedName("time") val time: List<String>? = null,
        @SerializedName("temperature_2m") val temp: List<Double>? = null,
        @SerializedName("weather_code") val weatherCode: List<Int>? = null
    )
}
