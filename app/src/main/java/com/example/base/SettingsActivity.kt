package com.example.base

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.base.data.AppDatabase
import com.example.base.data.model.User
import com.example.base.util.NotificationHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var notificationHelper: NotificationHelper

    // Views
    private lateinit var etName: TextInputEditText
    private lateinit var etWeight: TextInputEditText
    private lateinit var etBirthDate: TextInputEditText
    private lateinit var tvCalculatedGoal: TextView
    private lateinit var tvGoalExplanation: TextView
    private lateinit var btnStartTime: MaterialButton
    private lateinit var btnEndTime: MaterialButton
    private lateinit var btnSave: MaterialButton

    // State
    private var selectedBirthDate: Long = 0L
    private var selectedStartTime: String = "08:00"
    private var selectedEndTime: String = "22:00"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Init DB and Helper
        db = AppDatabase.getDatabase(this)
        notificationHelper = NotificationHelper(this)

        setupViews()
        loadUserData()
    }

    private fun setupViews() {
        // Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // Bind Views
        etName = findViewById(R.id.et_user_name)
        etWeight = findViewById(R.id.et_weight)
        etBirthDate = findViewById(R.id.et_birth_date)
        tvCalculatedGoal = findViewById(R.id.tv_calculated_goal)
        tvGoalExplanation = findViewById(R.id.tv_goal_explanation)
        btnStartTime = findViewById(R.id.btn_start_time)
        btnEndTime = findViewById(R.id.btn_end_time)
        btnSave = findViewById(R.id.btn_save)

        // Listeners
        etBirthDate.setOnClickListener { showDatePicker() }
        btnStartTime.setOnClickListener { showTimePicker(true) }
        btnEndTime.setOnClickListener { showTimePicker(false) }
        btnSave.setOnClickListener { saveSettings() }

        // Watch for weight changes to update goal preview
        etWeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateGoalDisplay()
            }
        })
    }

    private fun loadUserData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val user = db.userDao().getUser()
            withContext(Dispatchers.Main) {
                if (user != null) {
                    etName.setText(user.name)
                    etWeight.setText(user.weight.toString())
                    
                    // Set Birth Date
                    selectedBirthDate = user.birthDate
                    if (selectedBirthDate != 0L) {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        etBirthDate.setText(sdf.format(selectedBirthDate))
                    }

                    // Set Times
                    selectedStartTime = user.wakeUpTime
                    selectedEndTime = user.sleepTime
                    btnStartTime.text = selectedStartTime
                    btnEndTime.text = selectedEndTime

                    // Update Goal Display
                    updateGoalDisplay()
                }
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        if (selectedBirthDate != 0L) {
            calendar.timeInMillis = selectedBirthDate
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance()
                selectedCal.set(year, month, dayOfMonth)
                selectedBirthDate = selectedCal.timeInMillis
                
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                etBirthDate.setText(sdf.format(selectedCal.time))
                
                updateGoalDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(isStart: Boolean) {
        val currentTime = if (isStart) selectedStartTime else selectedEndTime
        val parts = currentTime.split(":")
        val hour = parts[0].toIntOrNull() ?: 8
        val minute = parts[1].toIntOrNull() ?: 0

        TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val timeString = String.format("%02d:%02d", selectedHour, selectedMinute)
                if (isStart) {
                    selectedStartTime = timeString
                    btnStartTime.text = timeString
                } else {
                    selectedEndTime = timeString
                    btnEndTime.text = timeString
                }
            },
            hour,
            minute,
            true // 24h format
        ).show()
    }

    private fun updateGoalDisplay() {
        val weightStr = etWeight.text.toString()
        val weight = weightStr.toFloatOrNull() ?: 0f
        
        if (weight > 0 && selectedBirthDate != 0L) {
            val age = calculateAge(selectedBirthDate)
            val multiplier = getMultiplier(age)
            val goal = (weight * multiplier).toInt()
            
            tvCalculatedGoal.text = "${goal}ml"
            tvGoalExplanation.text = "Baseado em ${multiplier}ml por kg de peso corporal"
        } else {
            tvCalculatedGoal.text = "---"
            tvGoalExplanation.text = "Preencha peso e data de nascimento"
        }
    }

    private fun calculateAge(birthDate: Long): Int {
        val dob = Calendar.getInstance()
        dob.timeInMillis = birthDate
        val today = Calendar.getInstance()
        
        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    private fun getMultiplier(age: Int): Int {
        return when {
            age < 17 -> 40
            age in 18..55 -> 35
            age in 56..65 -> 30
            else -> 25
        }
    }

    private fun calculateGoal(weight: Float, age: Int): Int {
        return (weight * getMultiplier(age)).toInt()
    }

    private fun saveSettings() {
        val name = etName.text.toString()
        val weightStr = etWeight.text.toString()
        
        if (name.isBlank() || weightStr.isBlank() || selectedBirthDate == 0L) {
            Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        val weight = weightStr.toFloatOrNull()
        if (weight == null || weight <= 0) {
            Toast.makeText(this, "Peso inválido", Toast.LENGTH_SHORT).show()
            return
        }

        val age = calculateAge(selectedBirthDate)
        val goal = calculateGoal(weight, age)

        lifecycleScope.launch(Dispatchers.IO) {
            val newUser = User(
                name = name,
                weight = weight,
                dailyGoal = goal,
                birthDate = selectedBirthDate,
                wakeUpTime = selectedStartTime,
                sleepTime = selectedEndTime,
                onboardingCompleted = true
            )
            
            db.userDao().insertUser(newUser)
            
            // Reschedule notifications (simple implementation: cancel all and schedule next)
            // Ideally we would pass the new times to the helper
            // For now, let's just ensure the channel exists
            notificationHelper.createNotificationChannel()
            
            withContext(Dispatchers.Main) {
                Toast.makeText(this@SettingsActivity, "Configurações salvas!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
