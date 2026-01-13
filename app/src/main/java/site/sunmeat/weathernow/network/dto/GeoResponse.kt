package site.sunmeat.weathernow.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Ответ Geocoding API
 */
data class GeoResponse(
    @SerializedName("results")
    val results: List<GeoResult>?
)

data class GeoResult(
    @SerializedName("name")
    val name: String,

    @SerializedName("country")
    val country: String?,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double
)
