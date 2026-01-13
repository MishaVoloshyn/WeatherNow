package site.sunmeat.weathernow.network

import retrofit2.http.GET
import retrofit2.http.Query
import site.sunmeat.weathernow.network.dto.GeoResponse

/**
 * Geocoding API (Open-Meteo)
 * City name -> coordinates
 */
interface GeoApi {

    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 5,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeoResponse
}
