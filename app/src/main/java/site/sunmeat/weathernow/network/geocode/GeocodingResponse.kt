package site.sunmeat.weathernow.network.geocode

import com.google.gson.annotations.SerializedName

data class GeocodingResponse(
    @SerializedName("results")
    val results: List<GeocodingResult>?
)

data class GeocodingResult(
    @SerializedName("name")
    val name: String?,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double
)
