package com.example.work_time_note

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.work_time_note.databinding.ItemRecordBinding

class RecordsAdapter(
    private val records: List<WorkRecord>,
    private val onEditClick: (WorkRecord) -> Unit,
) : RecyclerView.Adapter<RecordsAdapter.RecordViewHolder>() {

    // ViewHolder pro položku seznamu
    class RecordViewHolder(val binding: ItemRecordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {

        val binding = ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]

        with(holder.binding) {

            tvActivityName.text = record.activityName
            tvDate.text = record.date
            tvDuration.text = record.duration
            tvTimeRange.text = "${record.startTime} - ${record.endTime}"

            btnEdit.setOnClickListener {
                onEditClick(record)
            }

        }
    }

    override fun getItemCount(): Int = records.size // Počet položek v seznamu
}
