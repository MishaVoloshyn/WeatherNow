package site.sunmeat.weathernow

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.sunmeat.weathernow.databinding.ActivityMainBinding
import site.sunmeat.weathernow.network.ApiClient
import site.sunmeat.weathernow.network.geocode.GeocodingClient
import site.sunmeat.weathernow.storage.CitiesStorage
import site.sunmeat.weathernow.storage.StoredCity
import site.sunmeat.weathernow.util.WeatherCodeMapper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var storage: CitiesStorage

    private val cities = mutableListOf<CityUi>()

    private val adapter = CityAdapter(
        onCardClick = { city -> openForecast(city) },
        onDetailsClick = { city -> openForecast(city) },
        onLongClick = { city -> confirmDelete(city) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = CitiesStorage(this)

        storage.ensureDefaultsIfEmpty(
            listOf(
                StoredCity("Kyiv", 50.4501, 30.5234),
                StoredCity("Lviv", 49.8397, 24.0297),
                StoredCity("Odesa", 46.4825, 30.7233)
            )
        )
        binding.fabAdd.apply {
            scaleX = 0f
            scaleY = 0f
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .start()
        }

        binding.rvCities.layoutManager = LinearLayoutManager(this)
        binding.rvCities.adapter = adapter

        reloadCitiesFromStorageAndUpdateWeather()

        binding.fabAdd.setOnClickListener {
            showAddCityDialog()
        }
    }

    private fun openForecast(city: CityUi) {
        val intent = Intent(this, ForecastActivity::class.java).apply {
            putExtra(ForecastActivity.EXTRA_CITY_NAME, city.name)
            putExtra(ForecastActivity.EXTRA_LAT, city.lat)
            putExtra(ForecastActivity.EXTRA_LON, city.lon)
        }
        startActivity(intent)
    }

    private fun confirmDelete(city: CityUi) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete city?")
            .setMessage("Remove \"${city.name}\" from list?")
            .setPositiveButton("Delete") { d, _ ->
                d.dismiss()
                storage.removeCityByName(city.name)
                reloadCitiesFromStorageAndUpdateWeather()
                Toast.makeText(this, "Deleted: ${city.name}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .show()
    }

    private fun reloadCitiesFromStorageAndUpdateWeather() {
        cities.clear()

        val stored = storage.getCities()
        cities.addAll(stored.map { sc ->
            CityUi(
                name = sc.name,
                lat = sc.lat,
                lon = sc.lon,
                temp = "--°",
                condition = "Loading...",
                minMax = "H:--°  L:--°"
            )
        })

        adapter.submit(cities.toList())

        for (i in cities.indices) {
            loadCityWeatherIntoCard(i, cities[i])
        }
    }

    private fun loadCityWeatherIntoCard(index: Int, city: CityUi) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val forecast = ApiClient.weatherApi.getForecast(city.lat, city.lon)

                val tempC = forecast.current?.temperature?.toInt()
                val tempText = tempC?.let { "${it}°" } ?: "--°"

                val codeToday = forecast.daily?.weatherCode?.firstOrNull()
                val conditionText = WeatherCodeMapper.toText(codeToday)

                val max = forecast.daily?.max?.firstOrNull()?.toInt()
                val min = forecast.daily?.min?.firstOrNull()?.toInt()
                val minMaxText =
                    if (max != null && min != null) "H:${max}°  L:${min}°" else "H:--°  L:--°"

                withContext(Dispatchers.Main) {
                    if (index in cities.indices) {
                        cities[index] = cities[index].copy(
                            temp = tempText,
                            condition = conditionText,
                            minMax = minMaxText
                        )
                        adapter.submit(cities.toList())
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    if (index in cities.indices) {
                        cities[index] = cities[index].copy(
                            condition = "—",
                            temp = "--°",
                            minMax = "H:--°  L:--°"
                        )
                        adapter.submit(cities.toList())
                    }
                }
            }
        }
    }

    private fun showAddCityDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_city, null)
        val et = view.findViewById<EditText>(R.id.etCityName)

        MaterialAlertDialogBuilder(this)
            .setTitle("Add city")
            .setView(view)
            .setPositiveButton("Add") { dialog, _ ->
                val name = et.text?.toString()?.trim().orEmpty()
                if (name.isEmpty()) {
                    Toast.makeText(this, "Enter city name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                dialog.dismiss()
                resolveCityAndSave(name)
            }
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .show()
    }

    private fun resolveCityAndSave(cityName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geo = GeocodingClient.api.search(cityName)
                val first = geo.results?.firstOrNull()

                if (first == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "City not found", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val lat = first.latitude
                val lon = first.longitude
                val name = first.name ?: cityName

                storage.addCity(StoredCity(name, lat, lon))

                withContext(Dispatchers.Main) {
                    reloadCitiesFromStorageAndUpdateWeather()
                    Toast.makeText(this@MainActivity, "Saved: $name", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${e.message ?: "unknown"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
