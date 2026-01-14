package site.sunmeat.weathernow

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import site.sunmeat.weathernow.databinding.ItemDayForecastBinding

class DayForecastAdapter : RecyclerView.Adapter<DayForecastAdapter.VH>() {

    private val items = mutableListOf<DayForecastUi>()

    fun submit(data: List<DayForecastUi>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemDayForecastBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class VH(private val binding: ItemDayForecastBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DayForecastUi) {
            binding.tvDay.text = item.day
            binding.tvDayDesc.text = item.desc
            binding.tvDayTemp.text = item.temp
            binding.ivDayIcon.setImageResource(item.iconRes)
        }
    }
}
