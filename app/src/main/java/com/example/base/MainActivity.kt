package com.example.base

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.base.data.AppDatabase
import com.example.base.data.model.WaterRecord
import com.example.base.util.NotificationHelper
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var btnMenu: ImageButton

    // Database and Views
    private lateinit var db: AppDatabase
    private lateinit var tvCurrentIntake: TextView
    private lateinit var tvGoal: TextView
    private lateinit var tvPercentage: TextView
    private lateinit var progressWater: ProgressBar
    private lateinit var tvGreeting: TextView
    private lateinit var tvStreakDays: TextView
    private lateinit var waterWave: android.view.View
    private lateinit var tvNextNotification: TextView
    
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        btnMenu = findViewById(R.id.btn_menu)

        // Init Database
        db = AppDatabase.getDatabase(this)

        // Init Views
        tvCurrentIntake = findViewById(R.id.tv_current_intake)
        tvGoal = findViewById(R.id.tv_goal)
        tvPercentage = findViewById(R.id.tv_percentage)
        progressWater = findViewById(R.id.progress_water)
        tvGreeting = findViewById(R.id.tv_greeting)
        tvStreakDays = findViewById(R.id.tv_streak_days)
        waterWave = findViewById(R.id.water_wave)
        tvNextNotification = findViewById(R.id.tv_next_notification)
        
        notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()
        
        checkNotificationPermissions()
        scheduleNextNotification()

        setupNavigation()
        setupButtons()
        loadWaterData()
    }

    private fun setupNavigation() {
        // Open drawer on menu button click
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Already on home
                }
                R.id.nav_achievements -> {
                    startActivity(android.content.Intent(this, AchievementsActivity::class.java))
                }
                R.id.nav_settings -> {
                    startActivity(android.content.Intent(this, SettingsActivity::class.java))
                }
                R.id.nav_about -> {
                    startActivity(android.content.Intent(this, AboutActivity::class.java))
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // FAB Stats click
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_stats).setOnClickListener {
            startActivity(android.content.Intent(this, StatisticsActivity::class.java))
        }
    }

    private fun setupButtons() {
        val buttons = mapOf(
            R.id.btn_add_100 to 100,
            R.id.btn_add_250 to 250,
            R.id.btn_add_500 to 500,
            R.id.btn_add_750 to 750,
            R.id.btn_add_1000 to 1000
        )

        buttons.forEach { (id, amount) ->
            findViewById<Button>(id).setOnClickListener {
                addWater(amount)
            }
        }
    }

    private fun addWater(amount: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val record = WaterRecord(amount = amount, timestamp = System.currentTimeMillis())
            db.waterRecordDao().insertRecord(record)
            loadWaterData()
        }
    }

    private fun loadWaterData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val todayStart = getStartOfDay()
            val todayEnd = getEndOfDay()
            val total = db.waterRecordDao().getTotalForDay(todayStart, todayEnd) ?: 0
            
            // Get Goal (default 2000 if no user yet)
            val user = db.userDao().getUser()
            val goal = user?.dailyGoal ?: 2000
            val userName = user?.name ?: "Usuário"

            // Calculate Streak
            val timestamps = db.waterRecordDao().getAllTimestamps()
            val streak = calculateStreak(timestamps)

            withContext(Dispatchers.Main) {
                updateUI(total, goal, userName, streak)
            }
        }
    }

    private fun updateUI(current: Int, goal: Int, userName: String, streak: Int) {
        tvCurrentIntake.text = "${current}ml"
        tvGoal.text = "de ${goal}ml"
        val percentage = if (goal > 0) (current * 100 / goal) else 0
        tvPercentage.text = "$percentage%"
        progressWater.progress = percentage.coerceIn(0, 100)
        
        tvGreeting.text = "Olá, $userName"
        tvStreakDays.text = "$streak dias"
        
        animateWave(percentage)
    }

    private fun animateWave(percentage: Int) {
        val clampedPercentage = percentage.coerceIn(0, 100)
        // Calculate translation Y based on percentage (0% = bottom, 100% = top)
        // Assuming wave container height is approx 280dp. 
        // We need to measure it properly or use a fixed value. 
        // For now, let's assume the view height is available or use a rough estimate.
        // Better approach: Use View.post to get height if needed, or just translate relative to parent.
        
        waterWave.post {
            val height = (waterWave.parent as android.view.View).height
            val targetY = height - (height * (clampedPercentage / 100f))
            
            android.animation.ObjectAnimator.ofFloat(waterWave, "translationY", targetY).apply {
                duration = 1000
                interpolator = android.view.animation.DecelerateInterpolator()
                start()
            }
        }
    }

    private fun calculateStreak(timestamps: List<Long>): Int {
        if (timestamps.isEmpty()) return 0

        val distinctDays = timestamps.map { 
            val cal = Calendar.getInstance()
            cal.timeInMillis = it
            // Reset to start of day for comparison
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.distinct().sortedDescending()

        var streak = 0
        val today = getStartOfDay()
        val yesterday = today - 86400000 // 24 * 60 * 60 * 1000

        // Check if we have a record for today or yesterday to start the streak
        if (distinctDays.isEmpty()) return 0
        
        var currentCheck = if (distinctDays.contains(today)) today else yesterday
        
        if (!distinctDays.contains(currentCheck)) {
             return 0 // Streak broken if no record today or yesterday
        }

        for (day in distinctDays) {
            if (day == currentCheck) {
                streak++
                currentCheck -= 86400000
            } else if (day > currentCheck) {
                // Should not happen if sorted, but just in case
                continue 
            } else {
                // Gap found
                break
            }
        }
        return streak
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    private fun checkNotificationPermissions() {
        if (!notificationHelper.checkPermissions()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }
    
    private fun scheduleNextNotification() {
        // Schedule for 2 hours from now for demo purposes
        // In a real app, this would be based on user settings
        val delay = 2 * 60 * 60 * 1000L
        notificationHelper.scheduleNotification(delay)
        
        val nextTime = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis() + delay
        }
        val timeString = String.format("%02d:%02d", nextTime.get(Calendar.HOUR_OF_DAY), nextTime.get(Calendar.MINUTE))
        tvNextNotification.text = "Próximo: $timeString"
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
