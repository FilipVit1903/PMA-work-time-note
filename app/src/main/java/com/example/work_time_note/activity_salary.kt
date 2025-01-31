package com.example.work_time_note

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.work_time_note.customviews.CustomBarChart
import com.example.work_time_note.databinding.ActivitySalaryBinding

class ActivitySalary : AppCompatActivity() {

    private lateinit var binding: ActivitySalaryBinding
    private var hourlyRate: Int = 200 // Výchozí hodinová sazba

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Data pro grafy
        val hoursPerDay = listOf(
            "Pondělí" to 8,
            "Úterý" to 6,
            "Středa" to 7,
            "Čtvrtek" to 5,
            "Pátek" to 4
        )

        // Výchozí mzda na den
        val salaryPerDay = calculateSalaryPerDay(hoursPerDay)

        // Nastavení výchozího grafu (Hodiny)
        binding.barChart.setChartData(hoursPerDay)

        // Nastavení celkové mzdy
        updateTotalSalary(salaryPerDay)

        // Uložit novou hodinovou sazbu
        binding.btnSaveRate.setOnClickListener {
            val newRate = binding.etHourlyRate.text.toString().toIntOrNull()
            if (newRate != null && newRate > 0) {
                hourlyRate = newRate
                Toast.makeText(this, "Hodinová sazba nastavena na $hourlyRate Kč", Toast.LENGTH_SHORT).show()

                // Aktualizace mzdy a grafu
                val updatedSalary = calculateSalaryPerDay(hoursPerDay)
                binding.barChart.setChartData(updatedSalary)
                updateTotalSalary(updatedSalary)
            } else {
                Toast.makeText(this, "Zadejte platnou hodinovou sazbu!", Toast.LENGTH_SHORT).show()
            }
        }

        // Přepínání mezi grafy
        binding.btnHours.setOnClickListener {
            binding.barChart.setChartData(hoursPerDay)
        }

        binding.btnSalary.setOnClickListener {
            val updatedSalary = calculateSalaryPerDay(hoursPerDay)
            binding.barChart.setChartData(updatedSalary)
        }

        binding.btnHomepage.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    // Vypočítá mzdu na základě hodin
    private fun calculateSalaryPerDay(hoursPerDay: List<Pair<String, Int>>): List<Pair<String, Int>> {
        return hoursPerDay.map { Pair(it.first, it.second * hourlyRate) }
    }

    // Aktualizuje celkovou mzdu
    private fun updateTotalSalary(salaryPerDay: List<Pair<String, Int>>) {
        val totalSalary = salaryPerDay.sumOf { it.second }
        binding.tvTotalSalary.text = "Celková mzda: $totalSalary Kč"
    }
}
