package site.sunmeat.weathernow

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import site.sunmeat.weathernow.databinding.ItemCityBinding

/**
 * Adapter для списка городов.
 * Как объяснить преподу:
 * - RecyclerView для списков.
 * - События: клик по карточке и по кнопке Details.
 */
class CityAdapter(
    private val onCardClick: (CityUi) -> Unit,
    private val onDetailsClick: (CityUi) -> Unit
) : RecyclerView.Adapter<CityAdapter.CityVH>() {

    private val items = mutableListOf<CityUi>()

    fun submit(data: List<CityUi>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityVH {
        val binding = ItemCityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CityVH(binding, onCardClick, onDetailsClick)
    }

    override fun onBindViewHolder(holder: CityVH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class CityVH(
        private val binding: ItemCityBinding,
        private val onCardClick: (CityUi) -> Unit,
        private val onDetailsClick: (CityUi) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(city: CityUi) {
            binding.tvCity.text = city.name
            binding.tvCondition.text = city.condition
            binding.tvTemp.text = city.temp
            binding.tvMinMax.text = city.minMax

            // ✅ Иконка по типу погоды (простая маппа)
            binding.ivIcon.setImageResource(iconFor(city.condition))

            binding.root.setOnClickListener { onCardClick(city) }
            binding.btnDetails.setOnClickListener { onDetailsClick(city) }
        }

        private fun iconFor(condition: String): Int {
            val c = condition.lowercase()
            return when {
                c.contains("clear") -> R.drawable.ic_weather_sun
                c.contains("rain") || c.contains("drizzle") || c.contains("shower") -> R.drawable.ic_weather_rain
                c.contains("cloud") || c.contains("overcast") -> R.drawable.ic_weather_cloud
                else -> R.drawable.ic_weather_cloud
            }
        }
    }
}
