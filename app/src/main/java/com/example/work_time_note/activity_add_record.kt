package com.example.work_time_note

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.work_time_note.databinding.ActivityAddRecordBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
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

        // Výběr data (správně připojeno k showDatePickerDialog)
        binding.etDate.setOnClickListener { showDatePickerDialog() }

        // Výběr času začátku
        binding.etStartTime.setOnClickListener {
            showTimePickerDialog(binding.etStartTime)
        }

        // Výběr času konce
        binding.etEndTime.setOnClickListener {
            showTimePickerDialog(binding.etEndTime)
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

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()

        // Nastavíme dnešní datum na půlnoc, aby porovnání bylo správné
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Nastavíme datum 3 dny zpět na půlnoc, jinak bychom si nemohli vykázat aktuální den
        val threeDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -3)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val datePicker = DatePickerDialog(this, { _, year, month, day ->
            val selectedDateCalendar = Calendar.getInstance().apply {
                set(year, month, day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            when {
                selectedDateCalendar.before(threeDaysAgo) -> {
                    Toast.makeText(this, "Nelze zadat datum starší než 3 dny.", Toast.LENGTH_LONG).show()
                }
                selectedDateCalendar.after(today) -> {
                    Toast.makeText(this, "Nelze zadat budoucí datum.", Toast.LENGTH_LONG).show()
                }
                else -> {
                    // Formátujeme datum správně
                    selectedDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(selectedDateCalendar.time)
                    binding.etDate.setText(selectedDate)
                }
            }
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))

        // ✅ Opravené omezení: Pouze poslední 3 dny a dnešek
        datePicker.datePicker.minDate = threeDaysAgo.timeInMillis
        datePicker.datePicker.maxDate = today.timeInMillis // Dnešní den je povolen
        datePicker.show()
    }



    //Funkce pro zobrazení TimePicker
    private fun showTimePickerDialog(editText: com.google.android.material.textfield.TextInputEditText) {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(this, { _, hour, minute ->
            val formattedTime = String.format("%02d:%02d", hour, minute)
            editText.setText(formattedTime)

            // Uložíme vybraný čas
            if (editText.id == binding.etStartTime.id) {
                startTime = formattedTime
            } else {
                endTime = formattedTime
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

        timePicker.show()
    }
}
