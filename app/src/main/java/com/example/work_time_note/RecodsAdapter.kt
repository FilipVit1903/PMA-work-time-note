package com.example.work_time_note

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.work_time_note.databinding.ItemRecordBinding

class RecordsAdapter(
    private val records: List<WorkRecord>, // Seznam výkazů
    private val onEditClick: (WorkRecord) -> Unit, // Callback pro editaci
) : RecyclerView.Adapter<RecordsAdapter.RecordViewHolder>() {

    // ViewHolder pro položku seznamu
    class RecordViewHolder(val binding: ItemRecordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        // Inflace layoutu položky seznamu
        val binding = ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position] // Aktuální položka

        with(holder.binding) {
            // Nastavení údajů do položky seznamu
            tvActivityName.text = record.activityName
            tvDate.text = record.date
            tvDuration.text = record.duration
            tvTimeRange.text = "${record.startTime} - ${record.endTime}"

            // Kliknutí na tlačítko "Edit"
            btnEdit.setOnClickListener {
                onEditClick(record) // Zavolání callbacku pro editaci
            }

        }
    }

    override fun getItemCount(): Int = records.size // Počet položek v seznamu
}
