package com.example.work_time_note

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.work_time_note.databinding.ActivityRecordsBinding
import com.google.firebase.firestore.FirebaseFirestore

class ActivityRecords : AppCompatActivity() {

    private lateinit var binding: ActivityRecordsBinding
    private val db = FirebaseFirestore.getInstance()
    private val records = mutableListOf<WorkRecord>()
    private lateinit var adapter: RecordsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Binding
        binding = ActivityRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Nastavení RecyclerView
        adapter = RecordsAdapter(records) { record ->
            // Logika pro editaci
        }
        binding.recyclerViewRecords.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewRecords.adapter = adapter

        // Načtení záznamů z Firestore
        db.collection("workRecords")
            .get()
            .addOnSuccessListener { result ->
                records.clear()
                for (document in result) {
                    val record = WorkRecord(
                        id = document.id,
                        activityName = document.getString("activityName") ?: "",
                        startTime = document.getString("startTime") ?: "",
                        endTime = document.getString("endTime") ?: "",
                        duration = "1:33", // Délka bude počítána později
                        date = document.getString("date") ?: "",
                        note = document.getString("note") ?: ""
                    )
                    records.add(record)
                }
                adapter.notifyDataSetChanged()
            }
    }
}
