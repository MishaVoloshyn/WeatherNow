package site.sunmeat.weathernow

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.sunmeat.weathernow.databinding.ActivityMainBinding
import site.sunmeat.weathernow.databinding.DialogAddCityBinding
import site.sunmeat.weathernow.network.ApiClient
import site.sunmeat.weathernow.network.dto.GeoResult
import site.sunmeat.weathernow.util.WeatherCodeMapper

/**
 * MainActivity — список городов.
 *
 * Что важно для защиты:
 * - При запуске экрана автоматически подтягиваем погоду для всех городов (Retrofit + корутины).
 * - Запросы идут асинхронно, UI не блокируется.
 * - Карточки обновляются по мере прихода ответов.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: CityAdapter

    private val cities = mutableListOf(
        CityUi("Kyiv", "—", "--°", "H:--°  L:--°", 50.45, 30.52),
        CityUi("Lviv", "—", "--°", "H:--°  L:--°", 49.84, 24.03),
        CityUi("Odesa", "—", "--°", "H:--°  L:--°", 46.48, 30.73)
    )

    private val forecastLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val data = result.data ?: return@registerForActivityResult

        val lat = data.getDoubleExtra(ForecastActivity.EXTRA_LAT, Double.NaN)
        val lon = data.getDoubleExtra(ForecastActivity.EXTRA_LON, Double.NaN)
        val temp = data.getStringExtra(ForecastActivity.EXTRA_RESULT_TEMP) ?: return@registerForActivityResult
        val desc = data.getStringExtra(ForecastActivity.EXTRA_RESULT_DESC) ?: "—"
        val minMax = data.getStringExtra(ForecastActivity.EXTRA_RESULT_MINMAX) ?: "H:--°  L:--°"

        if (lat.isNaN() || lon.isNaN()) return@registerForActivityResult

        val index = cities.indexOfFirst { it.latitude == lat && it.longitude == lon }
        if (index != -1) {
            val old = cities[index]
            cities[index] = old.copy(temp = temp, condition = desc, minMax = minMax)
            refreshList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        adapter = CityAdapter(
            onCardClick = { openForecast(it) },
            onDetailsClick = { openForecast(it) }
        )

        binding.rvCities.adapter = adapter
        refreshList()

        binding.fabAdd.setOnClickListener { showAddCityDialog() }

        // ✅ Главный фикс: сразу подтягиваем погоду для всех городов
        refreshAllCitiesWeather()
    }

    private fun refreshList() {
        adapter.submit(cities)
        binding.tvEmpty.visibility = if (cities.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openForecast(city: CityUi) {
        val lat = city.latitude
        val lon = city.longitude
        if (lat == null || lon == null) {
            Toast.makeText(this, "No coordinates for ${city.name}", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ForecastActivity::class.java).apply {
            putExtra(ForecastActivity.EXTRA_CITY_NAME, city.name)
            putExtra(ForecastActivity.EXTRA_LAT, lat)
            putExtra(ForecastActivity.EXTRA_LON, lon)
        }
        forecastLauncher.launch(intent)
    }

    /**
     * ✅ Обновление погоды на главном экране (без захода в Details).
     * Запросы делаем в фоне, UI не блокируем.
     */
    private fun refreshAllCitiesWeather() {
        CoroutineScope(Dispatchers.IO).launch {
            // Ограничим параллельность простым батчем: по 2 запроса за раз
            val batchSize = 2
            val indices = cities.indices.toList()

            indices.chunked(batchSize).forEach { chunk ->
                val jobs = chunk.map { index ->
                    async {
                        val city = cities[index]
                        val lat = city.latitude
                        val lon = city.longitude
                        if (lat == null || lon == null) return@async null

                        try {
                            val response = ApiClient.weatherApi.getForecast(lat, lon)

                            val currentTemp = response.current?.temperature
                            val daily = response.daily
                            val maxToday = daily?.max?.firstOrNull()?.toInt()
                            val minToday = daily?.min?.firstOrNull()?.toInt()
                            val codeToday = daily?.weatherCode?.firstOrNull()

                            val tempText = if (currentTemp != null) "${currentTemp.toInt()}°" else "--°"
                            val descText = WeatherCodeMapper.toText(codeToday)
                            val minMaxText = if (maxToday != null && minToday != null) {
                                "H:${maxToday}°  L:${minToday}°"
                            } else {
                                "H:--°  L:--°"
                            }

                            Triple(index, tempText, Pair(descText, minMaxText))
                        } catch (_: Exception) {
                            null
                        }
                    }
                }

                val results = jobs.awaitAll().filterNotNull()

                withContext(Dispatchers.Main) {
                    results.forEach { triple ->
                        val index = triple.first
                        val tempText = triple.second
                        val desc = triple.third.first
                        val minMax = triple.third.second

                        val old = cities[index]
                        cities[index] = old.copy(temp = tempText, condition = desc, minMax = minMax)
                    }
                    refreshList()
                }
            }
        }
    }

    private fun showAddCityDialog() {
        val dialogBinding = DialogAddCityBinding.inflate(LayoutInflater.from(this))

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.add_city_title))
            .setView(dialogBinding.root)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.add), null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val cityName = dialogBinding.etCity.text?.toString()?.trim().orEmpty()

                    if (cityName.isEmpty()) {
                        dialogBinding.tilCity.error = getString(R.string.error_city_empty)
                        return@setOnClickListener
                    } else {
                        dialogBinding.tilCity.error = null
                    }

                    addCityByApi(cityName) { result ->
                        if (result == null) {
                            Toast.makeText(this, "City not found", Toast.LENGTH_SHORT).show()
                        } else {
                            val displayName = if (!result.country.isNullOrBlank()) {
                                "${result.name}, ${result.country}"
                            } else result.name

                            val city = CityUi(
                                name = displayName,
                                condition = "—",
                                temp = "--°",
                                minMax = "H:--°  L:--°",
                                latitude = result.latitude,
                                longitude = result.longitude
                            )

                            cities.add(city)
                            refreshList()

                            // ✅ сразу подтягиваем погоду для нового города
                            refreshAllCitiesWeather()

                            // и открываем Forecast (по желанию оставляем, это удобно)
                            openForecast(city)
                        }
                    }

                    dialog.dismiss()
                }
        }

        dialog.show()
    }

    private fun addCityByApi(cityName: String, onDone: (GeoResult?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.geoApi.searchCity(cityName)
                val first = response.results?.firstOrNull()
                withContext(Dispatchers.Main) { onDone(first) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                    onDone(null)
                }
            }
        }
    }
}
