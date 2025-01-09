package com.example.work_time_note

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.work_time_note.databinding.ActivityAddRecordBinding
import com.example.work_time_note.databinding.ActivityMainBinding
import com.example.work_time_note.databinding.ActivityRecordsBinding
import com.example.work_time_note.databinding.ActivitySalaryBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
            val intent = Intent(this, activity_salary::class.java)
            startActivity(intent)
        }
    }
}
