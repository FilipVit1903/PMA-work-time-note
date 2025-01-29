package com.example.work_time_note

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.work_time_note.databinding.ActivityRecordsBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ActivityRecords : AppCompatActivity() {

    private lateinit var binding: ActivityRecordsBinding
    private val db = FirebaseFirestore.getInstance()
    private val records = mutableListOf<WorkRecord>()
    private lateinit var adapter: RecordsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializace bindingu
        binding = ActivityRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializace adaptéru pouze jednou
        adapter = RecordsAdapter(
            records,
            onEditClick = { record -> openEditRecordActivity(record) },
            onLongClick = { record -> showDeleteConfirmationDialog(record) } // Logika mazání
        )

        // Nastavení RecyclerView
        binding.recyclerViewRecords.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewRecords.adapter = adapter

        // Filtrování podle tabů
        binding.tabFilter.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.text) {
                    "Dnes" -> loadRecordsForToday()
                    "Tento týden" -> loadRecordsForThisWeek()
                    "Tento měsíc" -> loadRecordsForThisMonth()
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })

        // Výchozí načtení záznamů (Dnes)
        loadRecordsForToday()
    }

    private fun loadRecordsForToday() {
        val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

        filterRecords { record ->
            record.date == today
        }
    }

    private fun loadRecordsForThisWeek() {
        val calendar = Calendar.getInstance()
        val weekStart = calendar.apply { set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) }.time
        val weekEnd = calendar.apply { set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY) }.time

        filterRecords {
            val recordDate = SimpleDateFormat("d.M.yyyy", Locale.getDefault()).parse(it.date)
            recordDate != null && recordDate >= weekStart && recordDate <= weekEnd
        }
    }

    private fun loadRecordsForThisMonth() {
        val calendar = Calendar.getInstance()
        val monthStart = calendar.apply { set(Calendar.DAY_OF_MONTH, 1) }.time
        val monthEnd = calendar.apply {
            set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        }.time

        filterRecords {
            val recordDate = SimpleDateFormat("d.M.yyyy", Locale.getDefault()).parse(it.date)
            recordDate != null && recordDate >= monthStart && recordDate <= monthEnd
        }
    }

    private fun filterRecords(predicate: (WorkRecord) -> Boolean) {
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
                        duration = calculateDuration(
                            document.getString("startTime") ?: "",
                            document.getString("endTime") ?: ""
                        ),
                        date = document.getString("date") ?: "", // Získáváme datum
                        note = document.getString("note") ?: ""
                    )

                    // DEBUG: Výpis dat z Firestore
                    println("Načtený záznam: ${record.date} (Dnešní datum: ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())})")

                    if (predicate(record)) {
                        records.add(record)
                    }
                }

                adapter.notifyDataSetChanged()

            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Chyba při načítání dat: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun calculateDuration(startTime: String, endTime: String): String {
        return try {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            val start = format.parse(startTime)
            val end = format.parse(endTime)
            if (start != null && end != null) {
                val diff = end.time - start.time
                val hours = diff / (1000 * 60 * 60)
                val minutes = (diff % (1000 * 60 * 60)) / (1000 * 60)
                String.format("%02d:%02d", hours, minutes)
            } else {
                "N/A"
            }
        } catch (e: Exception) {
            "N/A"
        }
    }

    private fun openEditRecordActivity(record: WorkRecord) {
        val intent = Intent(this, ActivityEditRecord::class.java).apply {
            putExtra("recordId", record.id)
            putExtra("activityName", record.activityName)
            putExtra("startTime", record.startTime)
            putExtra("endTime", record.endTime)
            putExtra("date", record.date)
            putExtra("note", record.note)
        }
        startActivity(intent)
    }

    private fun showDeleteConfirmationDialog(record: WorkRecord) {
        val recordDate = SimpleDateFormat("d.M.yyyy", Locale.getDefault()).parse(record.date)
        val threeDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -3) }.time

        if (recordDate != null && recordDate >= threeDaysAgo) {
            AlertDialog.Builder(this)
                .setTitle("Smazat záznam")
                .setMessage("Opravdu chcete tento záznam smazat?")
                .setPositiveButton("Ano") { _, _ ->
                    db.collection("workRecords").document(record.id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Záznam byl smazán.", Toast.LENGTH_LONG).show()
                            loadRecordsForToday() // Načtení aktuálních dat
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Chyba při mazání: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .setNegativeButton("Ne", null)
                .show()
        } else {
            Toast.makeText(this, "Tento záznam již nelze smazat.", Toast.LENGTH_LONG).show()
        }
    }
}
