package site.sunmeat.weathernow.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import site.sunmeat.weathernow.network.ApiClient
import site.sunmeat.weathernow.util.NotificationHelper
import site.sunmeat.weathernow.util.WeatherCodeMapper
import kotlin.math.roundToInt

class OdesaWeatherWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            // ✅ Координаты Одессы
            val lat = 46.4825
            val lon = 30.7233
            val city = "Odesa"

            val response = ApiClient.weatherApi.getForecast(lat, lon)

            val tempC = response.current?.temperature
            val code = response.daily?.weatherCode?.firstOrNull()

            val tempText = tempC?.let { formatTemp(it) } ?: "--°"
            val descText = WeatherCodeMapper.toText(code)

            NotificationHelper.showWeatherUpdated(
                context = applicationContext,
                city = city,
                lat = lat,
                lon = lon,
                tempText = tempText,
                descText = descText
            )

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun isFahrenheit(): Boolean {
        val prefs = applicationContext.getSharedPreferences("prefs_units", Context.MODE_PRIVATE)
        return prefs.getBoolean("use_fahrenheit", false)
    }

    private fun formatTemp(tempC: Double): String {
        return if (isFahrenheit()) {
            val f = (tempC * 9.0 / 5.0 + 32.0).roundToInt()
            "$f°"
        } else {
            "${tempC.roundToInt()}°"
        }
    }
}
