package com.example.work_time_note

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.work_time_note.databinding.ActivityAddRecordBinding
import com.example.work_time_note.databinding.ActivityMainBinding
import com.example.work_time_note.databinding.ActivityRecordsBinding
import com.example.work_time_note.databinding.ActivitySalaryBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val db = FirebaseFirestore.getInstance()
    private var hourlyRate: Double = 200.0 // Výchozí hodinová sazba

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigace na stránku "Přidat výkaz"
        binding.btnAddRecord.setOnClickListener {
            val intent = Intent(this, ActivityAddRecord::class.java)
            startActivity(intent)
        }

        // Navigace na stránku "Výkazy"
        binding.btnViewRecords.setOnClickListener {
            val intent = Intent(this, ActivityRecords::class.java)
            startActivity(intent)
        }

        // Navigace na stránku "Mzda"
        binding.btnViewSalary.setOnClickListener {
            val intent = Intent(this, ActivitySalary::class.java)
            startActivity(intent)
        }

        // Načtení hodinové sazby
        loadHourlyRate()

        // Výpočet celkové mzdy a hodin
        calculateTotalHoursAndSalary()
    }

    //Načtení hodinové sazby z SharedPreferences
    private fun loadHourlyRate() {
        val sharedPref = getSharedPreferences("WorkTimePrefs", Context.MODE_PRIVATE)
        hourlyRate = sharedPref.getFloat("hourlyRate", 200.0f).toDouble() // Výchozí 200 Kč/h
    }

    //Výpočet celkových hodin a mzdy za aktuální měsíc
    private fun calculateTotalHoursAndSalary() {
        val calendar = Calendar.getInstance()

        // První den aktuálního měsíce
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val monthStart = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendar.time)

        // Poslední den aktuálního měsíce
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val monthEnd = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendar.time)

        db.collection("workRecords")
            .get()
            .addOnSuccessListener { result ->
                var totalHours = 0.0

                for (document in result) {
                    val recordDate = document.getString("date") ?: ""
                    val startTime = document.getString("startTime") ?: ""
                    val endTime = document.getString("endTime") ?: ""

                    // Ověření, zda je datum v aktuálním měsíci
                    if (isDateInRange(recordDate, monthStart, monthEnd)) {
                        val hoursWorked = calculateHours(startTime, endTime)
                        totalHours += hoursWorked
                    }
                }

                val totalSalary = totalHours * hourlyRate

                // Aktualizace UI
                binding.twSumHours.text = String.format("%.1f hodin", totalHours)
                binding.tvTotalSalary.text = String.format("%.2f Kč", totalSalary)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Chyba při načítání dat", Toast.LENGTH_SHORT).show()
            }
    }

    //Ověření, zda je datum v daném měsíci
    private fun isDateInRange(dateStr: String, start: String, end: String): Boolean {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return try {
            val date = dateFormat.parse(dateStr)
            val startDate = dateFormat.parse(start)
            val endDate = dateFormat.parse(end)

            date != null && startDate != null && endDate != null &&
                    (date == startDate || date == endDate || (date.after(startDate) && date.before(endDate)))
        } catch (e: Exception) {
            false
        }
    }

    //Převod času na počet hodin
    private fun calculateHours(startTime: String, endTime: String): Double {
        return try {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            val start = format.parse(startTime)
            val end = format.parse(endTime)

            if (start != null && end != null) {
                val diff = end.time - start.time
                diff / (1000.0 * 60.0 * 60.0) // Převod na hodiny
            } else {
                0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }
}
