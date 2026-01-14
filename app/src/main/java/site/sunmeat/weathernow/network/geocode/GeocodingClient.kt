package site.sunmeat.weathernow.network.geocode

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GeocodingClient {
    val api: GeocodingApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeocodingApi::class.java)
    }
}
