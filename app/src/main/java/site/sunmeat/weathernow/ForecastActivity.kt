package site.sunmeat.weathernow

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.sunmeat.weathernow.databinding.ActivityForecastBinding
import site.sunmeat.weathernow.network.ApiClient
import site.sunmeat.weathernow.network.dto.ForecastResponse
import site.sunmeat.weathernow.util.WeatherCodeMapper
import java.text.SimpleDateFormat
import java.util.Locale

class ForecastActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForecastBinding
    private val adapter = DayForecastAdapter()

    private var lastLat: Double = Double.NaN
    private var lastLon: Double = Double.NaN
    private var lastTempText: String = "--°"
    private var lastDescText: String = "—"
    private var lastMinMaxText: String = "H:--°  L:--°"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForecastBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarForecast)

        // ✅ Своя иконка back (без приватных ресурсов)
        binding.toolbarForecast.setNavigationIcon(R.drawable.ic_back)
        binding.toolbarForecast.setNavigationOnClickListener { finishWithResult() }

        // ✅ Современная обработка back-gesture (без deprecated onBackPressed)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishWithResult()
            }
        })

        binding.rvDays.adapter = adapter

        val cityName = intent.getStringExtra(EXTRA_CITY_NAME) ?: "City"
        lastLat = intent.getDoubleExtra(EXTRA_LAT, Double.NaN)
        lastLon = intent.getDoubleExtra(EXTRA_LON, Double.NaN)

        binding.tvCityTitle.text = cityName

        if (lastLat.isNaN() || lastLon.isNaN()) {
            Toast.makeText(this, getString(R.string.no_coordinates), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadForecast(lastLat, lastLon)
    }

    private fun loadForecast(lat: Double, lon: Double) {
        binding.tvConditionBig.text = getString(R.string.loading)
        binding.tvExtra.text = ""

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.weatherApi.getForecast(lat, lon)
                withContext(Dispatchers.Main) { applyForecast(response) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.tvConditionBig.text = getString(R.string.error)
                    Toast.makeText(
                        this@ForecastActivity,
                        getString(R.string.network_error, e.message ?: "unknown"),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun applyForecast(data: ForecastResponse) {
        val currentTemp = data.current?.temperature
        lastTempText = if (currentTemp != null) "${currentTemp.toInt()}°" else "--°"
        binding.tvBigTemp.text = lastTempText

        val daily = data.daily
        val codeToday = daily?.weatherCode?.firstOrNull()

        lastDescText = WeatherCodeMapper.toText(codeToday)
        binding.tvConditionBig.text = lastDescText

        // Иконка hero
        binding.ivHeroIcon.setImageResource(WeatherCodeMapper.toIcon(codeToday))

        // min/max сегодня
        val todayMax = daily?.max?.firstOrNull()?.toInt()
        val todayMin = daily?.min?.firstOrNull()?.toInt()
        lastMinMaxText = if (todayMax != null && todayMin != null) {
            "H:${todayMax}°  L:${todayMin}°"
        } else {
            "H:--°  L:--°"
        }

        val list = buildDaysList(daily?.time, daily?.max, daily?.min, daily?.weatherCode)
        adapter.submit(list)

        binding.tvExtra.text = getString(R.string.data_source)
    }

    private fun buildDaysList(
        time: List<String>?,
        max: List<Double>?,
        min: List<Double>?,
        codes: List<Int>?
    ): List<DayForecastUi> {
        if (time == null || max == null || min == null) return emptyList()

        val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val sdfOut = SimpleDateFormat("EEE", Locale.US)

        val count = minOf(time.size, max.size, min.size, codes?.size ?: time.size)
        val result = ArrayList<DayForecastUi>(count)

        for (i in 0 until count) {
            val dayLabel = try {
                val d = sdfIn.parse(time[i])
                if (d != null) sdfOut.format(d) else time[i]
            } catch (_: Exception) {
                time[i]
            }

            val code = codes?.getOrNull(i)
            val desc = WeatherCodeMapper.toText(code)
            val icon = WeatherCodeMapper.toIcon(code)
            val temp = "${max[i].toInt()}° / ${min[i].toInt()}°"

            result.add(DayForecastUi(dayLabel, desc, temp, icon))
        }

        return result
    }

    private fun finishWithResult() {
        val result = Intent().apply {
            putExtra(EXTRA_LAT, lastLat)
            putExtra(EXTRA_LON, lastLon)
            putExtra(EXTRA_RESULT_TEMP, lastTempText)
            putExtra(EXTRA_RESULT_DESC, lastDescText)
            putExtra(EXTRA_RESULT_MINMAX, lastMinMaxText)
        }
        setResult(RESULT_OK, result)
        finish()
    }

    companion object {
        const val EXTRA_CITY_NAME = "extra_city_name"
        const val EXTRA_LAT = "extra_lat"
        const val EXTRA_LON = "extra_lon"

        const val EXTRA_RESULT_TEMP = "extra_result_temp"
        const val EXTRA_RESULT_DESC = "extra_result_desc"
        const val EXTRA_RESULT_MINMAX = "extra_result_minmax"
    }
}
