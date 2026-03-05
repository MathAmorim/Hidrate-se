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
    private lateinit var btnBackup: MaterialButton
    private lateinit var btnRestore: MaterialButton
    private lateinit var btnBatteryOptimization: MaterialButton
    private lateinit var btnAutoStart: MaterialButton
    private lateinit var btnExactAlarms: MaterialButton
    private lateinit var switchPermanentNotification: com.google.android.material.materialswitch.MaterialSwitch
    private lateinit var backupManager: com.example.base.data.BackupManager

    // State
    private var selectedBirthDate: Long = 0L
    private var selectedStartTime: String = "08:00"
    private var selectedEndTime: String = "22:00"

    private val restoreLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            performRestore(uri)
        }
    }
    
    // Launcher para forçar a espera do ecrã de permissão de Alarmes Exatos
    private val alarmPermissionLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) {
        // Quando o utilizador volta da janela de Alarmes da Xiaomi/Samsung, avança na cascata
        checkAndRequestBattery()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Init DB and Helper
        db = AppDatabase.getDatabase(this)
        notificationHelper = NotificationHelper(this)
        backupManager = com.example.base.data.BackupManager(this)

        setupViews()
        loadUserData()
        checkIfIsFirstRun()
    }
    
    private var isFirstTimeOnboarding = false

    private fun checkIfIsFirstRun() {
        lifecycleScope.launch(Dispatchers.IO) {
            val user = db.userDao().getUser()
            if (user == null || !user.onboardingCompleted) {
                isFirstTimeOnboarding = true
                withContext(Dispatchers.Main) {
                    supportActionBar?.title = "Configuração Inicial"
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    btnSave.text = "Concluir e Começar"
                }
            }
        }
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
        btnBackup = findViewById(R.id.btn_backup)
        btnRestore = findViewById(R.id.btn_restore)
        btnBatteryOptimization = findViewById(R.id.btn_battery_optimization)
        btnAutoStart = findViewById(R.id.btn_auto_start)
        btnExactAlarms = findViewById(R.id.btn_exact_alarms)
        switchPermanentNotification = findViewById(R.id.switch_permanent_notification)
        val btnAddWidget = findViewById<android.view.View>(R.id.btn_add_widget)

        // Listeners
        etBirthDate.setOnClickListener { showDatePicker() }
        btnStartTime.setOnClickListener { showTimePicker(true) }
        btnEndTime.setOnClickListener { showTimePicker(false) }
        btnSave.setOnClickListener { saveSettings() }
        btnBackup.setOnClickListener { performBackup() }
        btnRestore.setOnClickListener { restoreLauncher.launch(arrayOf("*/*")) }
        btnAddWidget.setOnClickListener { requestPinWidget() }
        
        // Listeners das Permissões
        btnBatteryOptimization.setOnClickListener {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                startActivity(intent)
            } catch (e: Exception) {
                // Fallback Settings
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = android.net.Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
        
        btnAutoStart.setOnClickListener {
            try {
                // Alvo principal: Xiaomi MIUI que bloqueia brutalmente as notificações
                val intent = android.content.Intent()
                intent.setComponent(android.content.ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"))
                startActivity(intent)
            } catch (e: Exception) {
                try {
                    // Outras marcas
                    val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = android.net.Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
        
        btnExactAlarms.setOnClickListener {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                try {
                    val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    intent.data = android.net.Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this, "Esta versão do Android não bloqueia alarmes exatos por padrão.", Toast.LENGTH_LONG).show()
            }
        }

        // Watch for weight changes to update goal preview
        etWeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateGoalDisplay()
            }
        })
    }

    private fun requestPinWidget() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(this)
            val myProvider = android.content.ComponentName(this, com.example.base.widget.WaterWidgetProvider::class.java)
            
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                Toast.makeText(this, getString(R.string.toast_widget_requested), Toast.LENGTH_SHORT).show()
                appWidgetManager.requestPinAppWidget(myProvider, null, null)
            } else {
                Toast.makeText(this, getString(R.string.toast_widget_unsupported), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, getString(R.string.toast_widget_unsupported), Toast.LENGTH_SHORT).show()
        }
    }

    private fun performBackup() {
        lifecycleScope.launch {
            Toast.makeText(this@SettingsActivity, getString(R.string.toast_backup_start), Toast.LENGTH_SHORT).show()
            val result = backupManager.performBackup()
            result.onSuccess { message ->
                Toast.makeText(this@SettingsActivity, message, Toast.LENGTH_LONG).show()
            }.onFailure { e ->
                Toast.makeText(this@SettingsActivity, getString(R.string.dialog_error_backup, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun performRestore(uri: android.net.Uri) {
        lifecycleScope.launch {
            Toast.makeText(this@SettingsActivity, getString(R.string.toast_restore_start), Toast.LENGTH_SHORT).show()
            val result = backupManager.performRestore(uri)
            result.onSuccess { message ->
                Toast.makeText(this@SettingsActivity, message, Toast.LENGTH_LONG).show()
                // Reload data
                loadUserData()
            }.onFailure { e ->
                Toast.makeText(this@SettingsActivity, getString(R.string.dialog_error_restore, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
        }
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
                    
                    switchPermanentNotification.isChecked = user.isPermanentNotificationEnabled

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
            
            tvCalculatedGoal.text = getString(R.string.current_intake_format, goal)
            tvGoalExplanation.text = getString(R.string.goal_explanation_format, multiplier.toString())
        } else {
            tvCalculatedGoal.text = "---"
            tvGoalExplanation.text = getString(R.string.goal_explanation_empty)
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
            Toast.makeText(this, getString(R.string.toast_fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        val weight = weightStr.toFloatOrNull()
        if (weight == null || weight <= 0) {
            Toast.makeText(this, getString(R.string.toast_invalid_weight), Toast.LENGTH_SHORT).show()
            return
        }

        val age = calculateAge(selectedBirthDate)
        val goal = calculateGoal(weight, age)

        lifecycleScope.launch(Dispatchers.IO) {
            val newUser = User(
                // Use id 1 to assure it edits the user instead of creating multiples if logic implies so. 
                // We fetch current user first to preserve ID if necessary:
                id = db.userDao().getUser()?.id ?: 0,
                name = name,
                weight = weight,
                dailyGoal = goal,
                birthDate = selectedBirthDate,
                wakeUpTime = selectedStartTime,
                sleepTime = selectedEndTime,
                onboardingCompleted = true,
                isPermanentNotificationEnabled = switchPermanentNotification.isChecked
            )
            
            db.userDao().insertUser(newUser)
            
            notificationHelper.createNotificationChannel()
            
            if (newUser.isPermanentNotificationEnabled) {
                notificationHelper.showPermanentNotification()
            } else {
                notificationHelper.hidePermanentNotification()
            }
            
            withContext(Dispatchers.Main) {
                if (isFirstTimeOnboarding) {
                    requestPermissionsFlow()
                } else {
                    Toast.makeText(this@SettingsActivity, getString(R.string.toast_settings_saved), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun requestPermissionsFlow() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.dialog_permissions_title)
            .setMessage(R.string.dialog_permissions_msg)
            .setPositiveButton(R.string.dialog_permissions_btn) { _, _ ->
                checkAndRequestNotifications()
            }
            .setCancelable(false)
            .show()
    }

    private fun checkAndRequestNotifications() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
                return // Prossegue no callback
            }
        }
        checkAndRequestAlarms()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            checkAndRequestAlarms()
        }
    }

    private fun checkAndRequestAlarms() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(android.app.AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.data = android.net.Uri.parse("package:$packageName")
                try {
                    // Impede o ecrã de fugir, aguardando que o utiilizador leia e retorne!
                    alarmPermissionLauncher.launch(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    checkAndRequestBattery()
                }
                return
            }
        }
        checkAndRequestBattery()
    }

    private fun checkAndRequestBattery() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val powerManager = getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = android.net.Uri.parse("package:$packageName")
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        finalizeOnboarding()
    }

    private fun finalizeOnboarding() {
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            // Assim que finaliza permissões e configurações, planta a primeira semente do alarme para arrancar de vez
            notificationHelper.scheduleDailyNotificationsOptimized()
            
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                Toast.makeText(this@SettingsActivity, getString(R.string.toast_onboarding_done), Toast.LENGTH_LONG).show()
                val mainIntent = android.content.Intent(this@SettingsActivity, MainActivity::class.java)
                mainIntent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(mainIntent)
                finish()
            }
        }
    }
}
