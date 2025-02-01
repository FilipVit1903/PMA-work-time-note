package com.example.work_time_note

import android.app.DatePickerDialog
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Tab
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.work_time_note.databinding.ActivityRecordsBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import com.google.android.material.tabs.TabLayout
import java.util.*

class ActivityRecords : AppCompatActivity() {

    private lateinit var binding: ActivityRecordsBinding
    private val db = FirebaseFirestore.getInstance()
    private val records = mutableListOf<WorkRecord>() // ✅ Opravená inicializace records
    private lateinit var adapter: RecordsAdapter
    private var activeButton: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Binding
        binding = ActivityRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Adapter pro RecyclerView
        adapter = RecordsAdapter(
            records,
            onEditClick = { record -> openEditRecordActivity(record) },
        )
        binding.recyclerViewRecords.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewRecords.adapter = adapter

        // Nastavení tlačítek pro filtrování
        setFilterButton(binding.btnToday) { loadRecordsForToday() }
        setFilterButton(binding.btnThisWeek) { loadRecordsForThisWeek() }
        setFilterButton(binding.btnThisMonth) { loadRecordsForThisMonth() }
        setFilterButton(binding.btnSelectDate) { showDatePickerDialog() }
        setFilterButton(binding.btnSelectMonth) { showMonthPickerDialog() }

        // Výchozí zobrazení: Dnes
        binding.btnToday.performClick()

        binding.btnHomepage.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }

    private fun setupFilterButtons() {
        binding.btnToday.setOnClickListener {
            loadRecordsForToday()
        }

        binding.btnThisWeek.setOnClickListener {
            loadRecordsForThisWeek()
        }

        binding.btnThisMonth.setOnClickListener {
            loadRecordsForThisMonth()
        }

        binding.btnSelectDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.btnSelectMonth.setOnClickListener {
            showMonthPickerDialog()
        }
    }


    //Metoda pro nastavení tlačítek pro filtrování
    private fun setFilterButton(button: TextView, action: () -> Unit) {
        button.setOnClickListener {
            // Vrácení barvy textu u předchozího aktivního tlačítka
            activeButton?.let {
                it.setBackgroundResource(R.drawable.tab_default_background)
                it.setTextColor(resources.getColor(R.color.black, theme)) // Neaktivní text na černou
            }

            // Nastavení stylu a barvy textu pro aktuální tlačítko
            button.setBackgroundResource(R.drawable.tab_active_background)
            button.setTextColor(resources.getColor(R.color.white, theme)) // Aktivní text na bílou
            activeButton = button

            // Spuštění akce
            action()
        }
    }

    //Metoda pro otevření aktivity pro úpravu výkazu
    private fun openEditRecordActivity(record: WorkRecord) {
        val intent = android.content.Intent(this, ActivityEditRecord::class.java).apply {
            putExtra("recordId", record.id)
            putExtra("activityName", record.activityName)
            putExtra("startTime", record.startTime)
            putExtra("endTime", record.endTime)
            putExtra("date", record.date)
            putExtra("note", record.note)
        }
        startActivity(intent)
    }

    //Metoda pro zobrazení dialogu pro smazání záznamu
    private fun showDeleteConfirmationDialog(record: WorkRecord) {
        AlertDialog.Builder(this)
            .setTitle("Smazat záznam")
            .setMessage("Opravdu chcete tento záznam smazat?")
            .setPositiveButton("Ano") { _, _ ->
                db.collection("workRecords").document(record.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Záznam smazán.", Toast.LENGTH_SHORT).show()
                        loadRecordsForToday() // Znovu načte dnešní data
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Chyba při mazání: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Ne", null)
            .show()
    }

    //Filtrování dnešních záznamů
    private fun loadRecordsForToday() {
        val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        filterRecords { it.date == today }
    }

    //Filtrování výkazů za aktuální týden
    private fun loadRecordsForThisWeek() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val weekStart = calendar.time

        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val weekEnd = calendar.time

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("Europe/Prague") // Nastavení časového pásma

        filterRecords { record ->
            try {
                val recordDate = dateFormat.parse(record.date)
                recordDate != null && recordDate in weekStart..weekEnd
            } catch (e: Exception) {
                Log.e("LoadRecordsForThisWeek", "Chyba při parsování datumu: ${e.message}", e)
                false
            }
        }
    }


    // ✅ Filtrování výkazů za aktuální měsíc
    private fun loadRecordsForThisMonth() {
        val calendar = Calendar.getInstance()

        // První den aktuálního měsíce
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val monthStart = SimpleDateFormat("MM.yyyy", Locale.getDefault()).format(calendar.time)

        // Debugging
        Log.d("LoadRecordsForThisMonth", "Filtrovaný měsíc: $monthStart")

        filterRecords { record ->
            try {
                val recordMonth = record.date.substring(3) // Získáme "MM.yyyy"
                Log.d("LoadRecordsForThisMonth", "Záznam datum: ${record.date}, extrahovaný měsíc: $recordMonth")
                recordMonth == monthStart
            } catch (e: Exception) {
                Log.e("LoadRecordsForThisMonth", "Chyba při filtrování: ${e.message}", e)
                false
            }
        }
    }





    //Výběr konkrétního data
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(this, { _, year, month, day ->
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("Europe/Prague") // Nastavení časového pásma

            val selectedDate = dateFormat.format(Calendar.getInstance().apply {
                set(year, month, day)
            }.time)

            filterRecords { it.date == selectedDate }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        datePicker.show()
    }


    //Výběr měsíce pro filtrování
    private fun showMonthPickerDialog() {
        val months = arrayOf(
            "Leden", "Únor", "Březen", "Duben", "Květen", "Červen",
            "Červenec", "Srpen", "Září", "Říjen", "Listopad", "Prosinec"
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Vyberte měsíc")
        builder.setItems(months) { _, which ->
            val selectedMonth = String.format("%02d", which + 1) // Převede na "01", "02", atd.
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val selectedMonthYear = "$selectedMonth.$currentYear"

            // Debugging
            Log.d("ShowMonthPickerDialog", "Vybraný měsíc: $selectedMonthYear")

            filterRecords { record ->
                try {
                    val recordMonth = record.date.substring(3) // Extrahuje "MM.yyyy"
                    Log.d("ShowMonthPickerDialog", "Záznam datum: ${record.date}, extrahovaný měsíc: $recordMonth")
                    recordMonth == selectedMonthYear
                } catch (e: Exception) {
                    Log.e("ShowMonthPickerDialog", "Chyba při filtrování: ${e.message}", e)
                    false
                }
            }
        }
        builder.show()
    }




    //Univerzální filtrovací metoda
    private fun filterRecords(predicate: (WorkRecord) -> Boolean) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("Europe/Prague") // Nastavení časového pásma

        db.collection("workRecords")
            .get()
            .addOnSuccessListener { result ->
                records.clear()
                for (document in result) {
                    try {
                        val dateString = document.getString("date") // Načteme datum jako String
                        if (dateString == null) {
                            Log.e("FilterRecords", "Chyba: datum nenalezeno u záznamu ${document.id}")
                            continue
                        }

                        val record = WorkRecord(
                            id = document.id,
                            activityName = document.getString("activityName") ?: "",
                            startTime = document.getString("startTime") ?: "",
                            endTime = document.getString("endTime") ?: "",
                            duration = calculateDuration(
                                document.getString("startTime") ?: "",
                                document.getString("endTime") ?: ""
                            ),
                            date = dateString, // Používáme naformátované datum
                            note = document.getString("note") ?: ""
                        )

                        if (predicate(record)) {
                            records.add(record)
                        }
                    } catch (e: Exception) {
                        Log.e("FilterRecords", "Chyba při zpracování záznamu: ${e.message}", e)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("FilterRecords", "Chyba při načítání dat: ${e.message}", e)
                Toast.makeText(this, "Chyba při načítání dat.", Toast.LENGTH_LONG).show()
            }
    }

    //Výpočet délky trvání práce
    private fun calculateDuration(startTime: String, endTime: String): String {
        return try {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            val start = format.parse(startTime)
            val end = format.parse(endTime)
            if (start != null && end != null) {
                val diff = end.time - start.time
                val hours = diff / (1000 * 60 * 60)
                val minutes = (diff % (1000 * 60 * 60)) / (1000 * 60)
                String.format("%02d:%02d h", hours, minutes)
            } else {
                "N/A"
            }
        } catch (e: Exception) {
            "N/A"
        }
    }
}
