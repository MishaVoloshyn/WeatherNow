package site.sunmeat.weathernow.storage

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class CitiesStorage(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getCities(): MutableList<StoredCity> {
        val json = prefs.getString(KEY_CITIES, null) ?: return mutableListOf()
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<StoredCity>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    StoredCity(
                        name = o.getString("name"),
                        lat = o.getDouble("lat"),
                        lon = o.getDouble("lon")
                    )
                )
            }
            list
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    fun saveCities(cities: List<StoredCity>) {
        val arr = JSONArray()
        for (c in cities) {
            val o = JSONObject()
            o.put("name", c.name)
            o.put("lat", c.lat)
            o.put("lon", c.lon)
            arr.put(o)
        }
        prefs.edit().putString(KEY_CITIES, arr.toString()).apply()
    }

    fun addCity(city: StoredCity) {
        val cities = getCities()
        val exists = cities.any { it.name.equals(city.name, ignoreCase = true) }
        if (!exists) {
            cities.add(city)
            saveCities(cities)
        }
    }

    /** ✅ Удаление по имени (без учета регистра) */
    fun removeCityByName(name: String) {
        val cities = getCities()
        val newList = cities.filterNot { it.name.equals(name, ignoreCase = true) }
        saveCities(newList)
    }

    fun ensureDefaultsIfEmpty(defaults: List<StoredCity>) {
        val current = getCities()
        if (current.isEmpty()) saveCities(defaults)
    }

    companion object {
        private const val PREFS = "prefs_cities"
        private const val KEY_CITIES = "cities_json"
    }
}
