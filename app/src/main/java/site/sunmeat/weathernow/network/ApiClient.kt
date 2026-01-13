package site.sunmeat.weathernow.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit клиент.
 * Как объяснить преподу:
 * - Отдельный класс для создания Retrofit, чтобы не дублировать код.
 */
object ApiClient {

    private val retrofitGeo: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val retrofitWeather: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val geoApi: GeoApi by lazy { retrofitGeo.create(GeoApi::class.java) }
    val weatherApi: WeatherApi by lazy { retrofitWeather.create(WeatherApi::class.java) }
}
