package site.sunmeat.weathernow

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import site.sunmeat.weathernow.databinding.ItemCityBinding

class CityAdapter(
    private val onCardClick: (CityUi) -> Unit,
    private val onDetailsClick: (CityUi) -> Unit,
    private val onLongClick: (CityUi) -> Unit
) : RecyclerView.Adapter<CityAdapter.CityVH>() {

    private val items = mutableListOf<CityUi>()

    fun submit(list: List<CityUi>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityVH {
        val binding = ItemCityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CityVH(binding, onCardClick, onDetailsClick, onLongClick)
    }

    override fun onBindViewHolder(holder: CityVH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class CityVH(
        private val binding: ItemCityBinding,
        private val onCardClick: (CityUi) -> Unit,
        private val onDetailsClick: (CityUi) -> Unit,
        private val onLongClick: (CityUi) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(city: CityUi) {
            binding.tvCity.text = city.name
            binding.tvCondition.text = city.condition
            binding.tvTemp.text = city.temp
            binding.tvMinMax.text = city.minMax

            binding.root.setOnClickListener { onCardClick(city) }
            binding.btnDetails.setOnClickListener { onDetailsClick(city) }

            // ✅ ДОЛГОЕ НАЖАТИЕ = удаление
            binding.root.setOnLongClickListener {
                onLongClick(city)
                true
            }
        }
    }
}
