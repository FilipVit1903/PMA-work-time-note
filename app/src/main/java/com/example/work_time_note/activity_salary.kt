package com.example.work_time_note

import android.app.AlertDialog
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

        // Nastavení aktuální hodinové mzdy v UI
        binding.tvHourlyRate.text = "Aktuální hodinová mzda: $hourlyRate Kč"

        // Uložení nové hodinové sazby
        binding.btnSaveRate.setOnClickListener {
            val newRate = binding.etHourlyRate.text.toString().toDoubleOrNull()
            if (newRate != null && newRate > 0) {
                hourlyRate = newRate
                saveHourlyRate(hourlyRate)
                binding.tvHourlyRate.text = "Aktuální hodinová mzda: $hourlyRate Kč"
                Toast.makeText(this, "Hodinová sazba nastavena na $hourlyRate Kč", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Zadejte platnou hodinovou sazbu!", Toast.LENGTH_SHORT).show()
            }
        }

        // Výběr měsíce pro zobrazení dat
        binding.btnSelectMonth.setOnClickListener {
            showMonthPickerDialog()
        }

        // Zpět na hlavní obrazovku
        binding.btnHomepage.setOnClickListener {
            finish()
        }
    }

    // Uložení hodinové sazby do SharedPreferences
    private fun saveHourlyRate(rate: Double) {
        val sharedPref = getSharedPreferences("WorkTimePrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putFloat("hourlyRate", rate.toFloat())
            apply()
        }
    }

    // Načtení hodinové sazby z SharedPreferences
    private fun loadHourlyRate() {
        val sharedPref = getSharedPreferences("WorkTimePrefs", MODE_PRIVATE)
        hourlyRate = sharedPref.getFloat("hourlyRate", 200.0f).toDouble() // Výchozí hodnota: 200 Kč/h
    }

    // Výběr měsíce přes AlertDialog
    private fun showMonthPickerDialog() {
        val months = arrayOf(
            "Leden", "Únor", "Březen", "Duben", "Květen", "Červen",
            "Červenec", "Srpen", "Září", "Říjen", "Listopad", "Prosinec"
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Vyberte měsíc")
        builder.setItems(months) { _, which ->
            val selectedMonth = which + 1 // Měsíce jsou indexovány od 1
            val selectedYear = Calendar.getInstance().get(Calendar.YEAR)
            calculateTotalHoursAndSalaryForMonth(selectedMonth, selectedYear)
        }
        builder.show()
    }

    // Výpočet celkových hodin a mzdy za vybraný měsíc
    private fun calculateTotalHoursAndSalaryForMonth(selectedMonth: Int, selectedYear: Int) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        db.collection("workRecords")
            .get()
            .addOnSuccessListener { result ->
                var totalHours = 0.0

                for (document in result) {
                    val recordDate = document.getString("date") ?: ""
                    val startTime = document.getString("startTime") ?: ""
                    val endTime = document.getString("endTime") ?: ""

                    // **Oprava filtrování** - Kontrola správného formátu měsíce
                    val dateParts = recordDate.split(".")
                    if (dateParts.size == 3) {
                        val recordMonth = dateParts[1].toInt()
                        val recordYear = dateParts[2].toInt()

                        if (recordMonth == selectedMonth && recordYear == selectedYear) {
                            val hoursWorked = calculateHours(startTime, endTime)
                            totalHours += hoursWorked
                        }
                    }
                }

                // Výpočet celkové mzdy
                val totalSalary = totalHours * hourlyRate

                // Aktualizace UI
                binding.tvTotalHours.text = String.format("Celkem hodin: %.1f h", totalHours)
                binding.tvTotalSalary.text = String.format("Celková mzda: %.2f Kč", totalSalary)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Chyba při načítání dat", Toast.LENGTH_SHORT).show()
            }
    }

    // Převod času na počet hodin
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
