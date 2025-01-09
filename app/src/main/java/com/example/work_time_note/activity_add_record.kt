package com.example.work_time_note

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.work_time_note.databinding.ActivityAddRecordBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ActivityAddRecord : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecordBinding
    private val db = FirebaseFirestore.getInstance() // Inicializace Firestore

    private var selectedDate = ""
    private var startTime = ""
    private var endTime = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializace bindingu
        binding = ActivityAddRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Výběr data
        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate = "$day.${month + 1}.$year"
                binding.etDate.setText(selectedDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Výběr času začátku
        binding.etStartTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                startTime = String.format("%02d:%02d", hour, minute)
                binding.etStartTime.setText(startTime)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        // Výběr času konce
        binding.etEndTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                endTime = String.format("%02d:%02d", hour, minute)
                binding.etEndTime.setText(endTime)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        // Uložení záznamu
        binding.btnAddRecord.setOnClickListener {
            val activityName = binding.etActivityName.text.toString()
            val note = binding.etNote.text.toString()

            // Kontrola povinných polí
            if (activityName.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || selectedDate.isEmpty()) {
                Toast.makeText(this, "Vyplňte všechna povinná pole!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Vytvoření záznamu
            val record = hashMapOf(
                "activityName" to activityName,
                "startTime" to startTime,
                "endTime" to endTime,
                "date" to selectedDate,
                "note" to note
            )

            // Uložení do Firestore
            db.collection("workRecords")
                .add(record)
                .addOnSuccessListener {
                    Toast.makeText(this, "Výkaz byl úspěšně uložen!", Toast.LENGTH_LONG).show()
                    finish() // Ukončení aktivity po úspěšném uložení
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Chyba při ukládání: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
