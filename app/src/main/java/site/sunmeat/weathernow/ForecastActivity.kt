package site.sunmeat.weathernow

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.sunmeat.weathernow.databinding.ActivityForecastBinding
import site.sunmeat.weathernow.network.ApiClient
import site.sunmeat.weathernow.network.dto.ForecastResponse
import site.sunmeat.weathernow.util.NotificationHelper
import site.sunmeat.weathernow.util.WeatherCodeMapper
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

class ForecastActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForecastBinding

    private val hourlyAdapter = HourlyAdapter()
    private val daysAdapter = DayForecastAdapter()

    private var lastLat = Double.NaN
    private var lastLon = Double.NaN
    private var lastCityName = "City"

    private var lastTempText = "--°"
    private var lastDescText = "—"
    private var lastMinMaxText = "H:--°  L:--°"

    private var lastData: ForecastResponse? = null

    private val prefs by lazy { getSharedPreferences(PREFS_UNITS, MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForecastBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarForecast)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.toolbarForecast.setNavigationIcon(R.drawable.ic_back)
        binding.toolbarForecast.navigationIcon?.let {
            val wrapped = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrapped, getColor(R.color.text_primary))
            binding.toolbarForecast.navigationIcon = wrapped
        }
        binding.toolbarForecast.setNavigationOnClickListener { finishWithResult() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = finishWithResult()
        })

        // Hourly
        binding.rvHourly.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvHourly.adapter = hourlyAdapter

        // Days (vertical)
        binding.rvDays.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvDays.adapter = daysAdapter

        lastCityName = intent.getStringExtra(EXTRA_CITY_NAME) ?: "City"
        lastLat = intent.getDoubleExtra(EXTRA_LAT, Double.NaN)
        lastLon = intent.getDoubleExtra(EXTRA_LON, Double.NaN)

        binding.tvToolbarCity.text = lastCityName

        // Switch °C/°F
        binding.swUnits.isChecked = isFahrenheit()
        binding.swUnits.setOnCheckedChangeListener { _, isChecked ->
            setFahrenheit(isChecked)
            lastData?.let { applyForecast(it, showNotification = false) }
        }

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
                withContext(Dispatchers.Main) {
                    lastData = response
                    applyForecast(response, showNotification = true) // ✅ уведомление после загрузки
                }
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

    private fun applyForecast(data: ForecastResponse, showNotification: Boolean) {
        lastData = data

        // Current temp
        val currentTempC = data.current?.temperature
        lastTempText = currentTempC?.let { formatTemp(it) } ?: "--°"
        binding.tvBigTemp.text = lastTempText

        // Today
        val daily = data.daily
        val codeToday = daily?.weatherCode?.firstOrNull()
        lastDescText = WeatherCodeMapper.toText(codeToday)
        binding.tvConditionBig.text = lastDescText
        binding.ivHeroIcon.setImageResource(WeatherCodeMapper.toIcon(codeToday))

        val maxTodayC = daily?.max?.firstOrNull()
        val minTodayC = daily?.min?.firstOrNull()
        lastMinMaxText =
            if (maxTodayC != null && minTodayC != null) {
                "H:${formatTemp(maxTodayC)}  L:${formatTemp(minTodayC)}"
            } else {
                "H:--°  L:--°"
            }

        // Hourly
        hourlyAdapter.submit(
            buildHourlyList(
                time = data.hourly?.time,
                temps = data.hourly?.temp,
                codes = data.hourly?.weatherCode
            )
        )

        // Days
        daysAdapter.submit(
            buildDaysList(
                time = daily?.time,
                max = daily?.max,
                min = daily?.min,
                codes = daily?.weatherCode
            )
        )

        binding.tvExtra.text = getString(R.string.data_source)

        // ✅ Notification (PendingIntent opens this screen)
        if (showNotification) {
            NotificationHelper.showWeatherUpdated(
                context = this,
                city = lastCityName,
                lat = lastLat,
                lon = lastLon,
                tempText = lastTempText,
                descText = lastDescText
            )
        }
    }

    private fun buildHourlyList(
        time: List<String>?,
        temps: List<Double>?,
        codes: List<Int>?
    ): List<HourlyUi> {
        if (time == null || temps == null) return emptyList()

        val sdfIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
        val sdfOut = SimpleDateFormat("HH", Locale.US)

        val count = minOf(time.size, temps.size, codes?.size ?: time.size, 12)
        val result = ArrayList<HourlyUi>(count)

        for (i in 0 until count) {
            val hour = try {
                sdfOut.format(sdfIn.parse(time[i])!!)
            } catch (_: Exception) {
                time[i].takeLast(2)
            }

            val icon = WeatherCodeMapper.toIcon(codes?.getOrNull(i))
            val tempText = formatTemp(temps[i])

            result.add(HourlyUi(hour, tempText, icon))
        }
        return result
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

        val count = minOf(time.size, max.size, min.size)
        val result = ArrayList<DayForecastUi>(count)

        for (i in 0 until count) {
            val day = try {
                sdfOut.format(sdfIn.parse(time[i])!!)
            } catch (_: Exception) {
                time[i]
            }

            val icon = WeatherCodeMapper.toIcon(codes?.getOrNull(i))
            val temp = "${formatTemp(max[i])} / ${formatTemp(min[i])}"

            result.add(DayForecastUi(day, "", temp, icon))
        }
        return result
    }

    private fun isFahrenheit(): Boolean =
        prefs.getBoolean(KEY_USE_F, false)

    private fun setFahrenheit(value: Boolean) {
        prefs.edit().putBoolean(KEY_USE_F, value).apply()
    }

    private fun formatTemp(tempC: Double): String {
        return if (isFahrenheit()) {
            val f = (tempC * 9.0 / 5.0 + 32.0).roundToInt()
            "$f°"
        } else {
            "${tempC.roundToInt()}°"
        }
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

        private const val PREFS_UNITS = "prefs_units"
        private const val KEY_USE_F = "use_fahrenheit"
    }
}
