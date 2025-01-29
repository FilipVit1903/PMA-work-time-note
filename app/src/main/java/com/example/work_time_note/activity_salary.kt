package com.example.work_time_note

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.work_time_note.databinding.ActivitySalaryBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ActivitySalary : AppCompatActivity() {

    private lateinit var binding: ActivitySalaryBinding
    private val db = FirebaseFirestore.getInstance()
    private var hourlyRate: Double = 200.0 // Výchozí hodinová sazba

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Načtení uložené hodinové sazby
        loadHourlyRate()

        // Výpočet celkové mzdy
        calculateTotalSalary()

        // Uložení nové hodinové sazby
        binding.btnSaveRate.setOnClickListener {
            val newRate = binding.etHourlyRate.text.toString()
            if (newRate.isNotEmpty()) {
                hourlyRate = newRate.toDouble()
                saveHourlyRate(hourlyRate)
                calculateTotalSalary() // Přepočítáme mzdu
            } else {
                Toast.makeText(this, "Zadejte platnou hodinovou sazbu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ Načtení hodinové sazby z SharedPreferences
    private fun loadHourlyRate() {
        val sharedPref = getSharedPreferences("WorkTimePrefs", Context.MODE_PRIVATE)
        hourlyRate = sharedPref.getFloat("hourlyRate", 200.0f).toDouble() // Výchozí 200 Kč/h
        binding.etHourlyRate.setText(hourlyRate.toString())
    }

    // ✅ Uložení hodinové sazby do SharedPreferences
    private fun saveHourlyRate(rate: Double) {
        val sharedPref = getSharedPreferences("WorkTimePrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putFloat("hourlyRate", rate.toFloat())
            apply()
        }
        Toast.makeText(this, "Hodinová sazba uložena", Toast.LENGTH_SHORT).show()
    }

    // ✅ Výpočet celkové mzdy za aktuální měsíc
    private fun calculateTotalSalary() {
        val calendar = Calendar.getInstance()
        val monthStart = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(
            calendar.apply { set(Calendar.DAY_OF_MONTH, 1) }.time
        )

        db.collection("workRecords")
            .whereGreaterThanOrEqualTo("date", monthStart)
            .get()
            .addOnSuccessListener { result ->
                var totalHours = 0.0
                for (document in result) {
                    val startTime = document.getString("startTime") ?: ""
                    val endTime = document.getString("endTime") ?: ""

                    val hoursWorked = calculateHours(startTime, endTime)
                    totalHours += hoursWorked
                }

                val totalSalary = totalHours * hourlyRate
                binding.tvTotalSalary.text = "Celková mzda: ${String.format("%.2f", totalSalary)} Kč"
            }
            .addOnFailureListener {
                Toast.makeText(this, "Chyba při načítání dat", Toast.LENGTH_SHORT).show()
            }
    }

    // ✅ Převod času na počet hodin
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
