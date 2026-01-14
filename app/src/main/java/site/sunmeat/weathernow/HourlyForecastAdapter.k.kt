package site.sunmeat.weathernow

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import site.sunmeat.weathernow.databinding.ItemHourlyForecastBinding

class HourlyForecastAdapter : RecyclerView.Adapter<HourlyForecastAdapter.VH>() {

    private val items = mutableListOf<HourlyForecastUi>()

    fun submit(list: List<HourlyForecastUi>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemHourlyForecastBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    class VH(private val binding: ItemHourlyForecastBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HourlyForecastUi) {
            binding.tvHour.text = item.timeLabel
            binding.ivHourIcon.setImageResource(item.iconRes)
            binding.tvHourTemp.text = item.temp
        }
    }
}
