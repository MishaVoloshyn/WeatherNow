package site.sunmeat.weathernow.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val WEATHER_BASE_URL = "https://api.open-meteo.com/"
    private const val GEO_BASE_URL = "https://geocoding-api.open-meteo.com/"

    private val weatherRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(WEATHER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val geoRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(GEO_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val weatherApi: WeatherApi by lazy {
        weatherRetrofit.create(WeatherApi::class.java)
    }

    val geoApi: GeoApi by lazy {
        geoRetrofit.create(GeoApi::class.java)
    }
}
