package site.sunmeat.weathernow.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import site.sunmeat.weathernow.CityUi

object CitiesStorage {

    private const val STORE_NAME = "cities_store"
    private val Context.dataStore by preferencesDataStore(name = STORE_NAME)

    private val KEY_CITIES = stringPreferencesKey("cities_json")

    fun observeCities(context: Context): Flow<List<CityUi>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[KEY_CITIES]
            if (json.isNullOrBlank()) emptyList() else decode(json)
        }
    }

    suspend fun saveCities(context: Context, cities: List<CityUi>) {
        val json = encode(cities)
        context.dataStore.edit { prefs ->
            prefs[KEY_CITIES] = json
        }
    }

    private fun encode(list: List<CityUi>): String {
        val arr = JSONArray()
        list.forEach { c ->
            val o = JSONObject()
            o.put("name", c.name)
            o.put("lat", c.latitude ?: JSONObject.NULL)
            o.put("lon", c.longitude ?: JSONObject.NULL)
            // на будущее оставим возможность хранить фаворит
            o.put("fav", false)
            arr.put(o)
        }
        return arr.toString()
    }

    private fun decode(json: String): List<CityUi> {
        val arr = JSONArray(json)
        val result = ArrayList<CityUi>(arr.length())

        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val name = o.optString("name", "City")

            val lat = if (o.isNull("lat")) null else o.optDouble("lat")
            val lon = if (o.isNull("lon")) null else o.optDouble("lon")

            // при старте — плейсхолдеры, погоду подтянем отдельным запросом
            result.add(
                CityUi(
                    name = name,
                    condition = "—",
                    temp = "--°",
                    minMax = "H:--°  L:--°",
                    latitude = lat,
                    longitude = lon
                )
            )
        }
        return result
    }
}
