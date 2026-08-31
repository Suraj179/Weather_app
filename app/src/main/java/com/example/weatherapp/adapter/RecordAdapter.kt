package com.example.weatherapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ItemWeatherRecordBinding
import com.example.weatherapp.model.WeatherRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * RecyclerView adapter displaying saved weather records with edit/delete actions.
 */
class RecordAdapter(
    private val onEdit: (WeatherRecord) -> Unit,
    private val onDelete: (WeatherRecord) -> Unit
) : ListAdapter<WeatherRecord, RecordAdapter.RecordViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())

    inner class RecordViewHolder(val binding: ItemWeatherRecordBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val binding = ItemWeatherRecordBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = getItem(position)
        with(holder.binding) {
            tvLocation.text = record.locationName
            tvTemperature.text = "${record.temperature.roundToInt()}\u00B0"
            tvCondition.text = "${record.condition} \u00B7 Humidity ${record.humidity}% \u00B7 Wind ${record.windSpeed} km/h"
            tvSavedAt.text = "Saved ${dateFormat.format(Date(record.savedAt))}"

            btnEdit.setOnClickListener { onEdit(record) }
            btnDelete.setOnClickListener { onDelete(record) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<WeatherRecord>() {
        override fun areItemsTheSame(oldItem: WeatherRecord, newItem: WeatherRecord) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: WeatherRecord, newItem: WeatherRecord) =
            oldItem == newItem
    }
}
