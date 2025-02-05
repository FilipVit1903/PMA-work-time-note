package com.example.work_time_note

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.work_time_note.databinding.ActivityEditRecordBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ActivityEditRecord : AppCompatActivity() {

    private lateinit var binding: ActivityEditRecordBinding
    private val db = FirebaseFirestore.getInstance()
    private var recordId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Načtení dat z Intentu
        recordId = intent.getStringExtra("recordId")
        val activityName = intent.getStringExtra("activityName") ?: ""
        val startTime = intent.getStringExtra("startTime") ?: ""
        val endTime = intent.getStringExtra("endTime") ?: ""
        val date = intent.getStringExtra("date") ?: ""
        val note = intent.getStringExtra("note") ?: ""

        // Předvyplnění formuláře
        binding.etActivityName.setText(activityName)
        binding.etStartTime.setText(startTime)
        binding.etEndTime.setText(endTime)
        binding.etDate.setText(date)
        binding.etNote.setText(note)

        // Přidání listenerů na výběr času a data
        binding.etStartTime.setOnClickListener { showTimePickerDialog(binding.etStartTime) }
        binding.etEndTime.setOnClickListener { showTimePickerDialog(binding.etEndTime) }
        binding.etDate.setOnClickListener { showDatePickerDialog() }

        // Kontrola, zda je výkaz starší než 3 dny
        if (isOlderThanThreeDays(date)) {
            binding.btnSaveChanges.isEnabled = false
            binding.btnDeleteRecord.isEnabled = false
            Toast.makeText(this, "Tento záznam již nelze upravit ani smazat.", Toast.LENGTH_LONG).show()
        }

        // Editace dat
        binding.btnSaveChanges.setOnClickListener {
            updateRecord()
        }

        // Mazání záznamu
        binding.btnDeleteRecord.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // Zpět na homepage
        binding.btnHomepage.setOnClickListener {
            val intent = Intent(this, ActivityRecords::class.java)
            startActivity(intent)
        }
    }

    // Aktualizace záznamu
    private fun updateRecord() {
        val updatedActivityName = binding.etActivityName.text.toString()
        val updatedStartTime = binding.etStartTime.text.toString()
        val updatedEndTime = binding.etEndTime.text.toString()
        val updatedDate = binding.etDate.text.toString()
        val updatedNote = binding.etNote.text.toString()

        if (updatedActivityName.isEmpty() || updatedStartTime.isEmpty() || updatedEndTime.isEmpty() || updatedDate.isEmpty()) {
            Toast.makeText(this, "Vyplňte všechna povinná pole!", Toast.LENGTH_SHORT).show()
            return
        }

        recordId?.let { id ->
            val updatedRecord = mapOf(
                "activityName" to updatedActivityName,
                "startTime" to updatedStartTime,
                "endTime" to updatedEndTime,
                "date" to updatedDate,
                "note" to updatedNote
            )

            db.collection("workRecords").document(id)
                .update(updatedRecord)
                .addOnSuccessListener {
                    Toast.makeText(this, "Záznam byl úspěšně upraven!", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Chyba při úpravě: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    // Zobrazení dialogu pro potvrzení mazání
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Smazat záznam")
            .setMessage("Opravdu chcete tento záznam smazat?")
            .setPositiveButton("Ano") { _, _ ->
                deleteRecord()
            }
            .setNegativeButton("Ne", null)
            .show()
    }

    // Mazání záznamu
    private fun deleteRecord() {
        recordId?.let { id ->
            db.collection("workRecords").document(id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Záznam byl úspěšně smazán!", Toast.LENGTH_LONG).show()
                    finish() // Ukončení aktivity po smazání
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Chyba při mazání: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    // Metoda pro výběr času
    private fun showTimePickerDialog(editText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(this, { _, hour, minute ->
            val formattedTime = String.format("%02d:%02d", hour, minute)
            editText.setText(formattedTime)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
        timePicker.show()
    }

    // Metoda pro výběr data
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(this, { _, year, month, day ->
            val formattedDate = String.format("%02d.%02d.%d", day, month + 1, year)
            binding.etDate.setText(formattedDate)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }

    // Funkce pro kontrolu stáří záznamu
    private fun isOlderThanThreeDays(recordDate: String): Boolean {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val recordDateObj = sdf.parse(recordDate) ?: return true

        val threeDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -3)
        }.time

        return recordDateObj.before(threeDaysAgo)
    }
}
