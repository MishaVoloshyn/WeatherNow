package site.sunmeat.weathernow

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import site.sunmeat.weathernow.databinding.ItemHourForecastBinding

class HourlyAdapter : RecyclerView.Adapter<HourlyAdapter.VH>() {

    private val items = mutableListOf<HourlyUi>()

    fun submit(data: List<HourlyUi>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemHourForecastBinding.inflate(
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

    class VH(private val binding: ItemHourForecastBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HourlyUi) {
            binding.tvHour.text = item.hour
            binding.tvHourTemp.text = item.temp
            binding.ivHourIcon.setImageResource(item.iconRes)
        }
    }
}
