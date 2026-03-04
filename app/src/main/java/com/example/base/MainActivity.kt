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
import com.example.base.util.MotivationManager
import com.example.base.adapter.HistoryAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var tvMotivation: TextView
    private lateinit var rvHistory: RecyclerView
    private lateinit var tvTotalEntries: TextView
    private lateinit var historyAdapter: HistoryAdapter
    
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Init Database
        db = AppDatabase.getDatabase(this)

        lifecycleScope.launch(Dispatchers.Main) {
            val user = withContext(Dispatchers.IO) { db.userDao().getUser() }
            if (user == null || !user.onboardingCompleted) {
                startActivity(android.content.Intent(this@MainActivity, SettingsActivity::class.java))
                finish()
                return@launch
            }
            
            setupMainActivityContent()
        }
    }

    private fun setupMainActivityContent() {
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        btnMenu = findViewById(R.id.btn_menu)

        // Init Views
        tvCurrentIntake = findViewById(R.id.tv_current_intake)
        tvGoal = findViewById(R.id.tv_goal)
        tvPercentage = findViewById(R.id.tv_percentage)
        progressWater = findViewById(R.id.progress_water)
        tvGreeting = findViewById(R.id.tv_greeting)
        tvStreakDays = findViewById(R.id.tv_streak_days)
        waterWave = findViewById(R.id.water_wave)
        tvNextNotification = findViewById(R.id.tv_next_notification)
        tvMotivation = findViewById(R.id.tv_motivation)
        tvTotalEntries = findViewById(R.id.tv_total_entries)
        rvHistory = findViewById(R.id.rv_history)

        setupHistoryRecyclerView()
        
        notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()
            
        // Reagendar alarmes (apenas 1 futuro através da dinâmica otimizada) e redesenhar janela
        lifecycleScope.launch {
            notificationHelper.scheduleDailyNotificationsOptimized()
            notificationHelper.showPermanentNotification()
        }
        
        checkNotificationPermissions()
        scheduleNextNotification()

        setupNavigation()
        setupButtons()
        loadWaterData()
    }

    override fun onResume() {
        super.onResume()
        if (::tvCurrentIntake.isInitialized) {
            loadWaterData()
        }
    }

    private fun setupHistoryRecyclerView() {
        historyAdapter = HistoryAdapter()
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = historyAdapter
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
                R.id.nav_statistics -> {
                    startActivity(android.content.Intent(this, StatisticsActivity::class.java))
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


    }

    private fun setupButtons() {
        val buttons = mapOf(
            R.id.btn_add_200 to 200,
            R.id.btn_add_300 to 300,
            R.id.btn_add_500 to 500
        )

        buttons.forEach { (id, amount) ->
            findViewById<Button>(id).setOnClickListener {
                addWater(amount)
            }
        }
        
        animateEntry()
    }

    private fun addWater(amount: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val record = WaterRecord(
                amount = amount, 
                date = com.example.base.util.DateUtils.getCurrentDate(),
                timestamp = System.currentTimeMillis(),
                source = "APP"
            )
            db.waterRecordDao().insert(record)
            
            // Check for goal reached notification
            notificationHelper.showGoalReachedNotification()

            loadWaterData()
            
            // Update Widget
            val intent = android.content.Intent(this@MainActivity, com.example.base.widget.WaterWidgetProvider::class.java)
            intent.action = com.example.base.widget.WaterWidgetProvider.ACTION_UPDATE_WIDGET
            sendBroadcast(intent)
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
            val allRecords = db.waterRecordDao().getAllRecords()
            val streak = com.example.base.util.StreakManager.calculateStreak(allRecords, goal)

            // Get History
            val history = db.waterRecordDao().getRecordsForDay(todayStart, todayEnd)

            withContext(Dispatchers.Main) {
                updateUI(total, goal, userName, streak, history)
            }
        }
    }

    private fun updateUI(current: Int, goal: Int, userName: String, streak: Int, history: List<WaterRecord>) {
        tvCurrentIntake.text = "${current}ml"
        tvGoal.text = "de ${goal}ml"
        val percentage = if (goal > 0) (current * 100 / goal) else 0
        tvPercentage.text = "$percentage%"
        
        // Smooth progress animation
        val targetProgress = percentage.coerceIn(0, 100)
        android.animation.ObjectAnimator.ofInt(progressWater, "progress", progressWater.progress, targetProgress).apply {
            duration = 1000 // 1 second animation
            interpolator = android.view.animation.DecelerateInterpolator()
            start()
        }
        
        tvGreeting.text = "Olá, $userName"
        tvStreakDays.text = "$streak dias"
        
        // Update Motivation
        tvMotivation.text = MotivationManager.getPhrase(percentage)
        
        // Update History
        historyAdapter.updateData(history)
        tvTotalEntries.text = "${history.size} entradas"

        animateWave(percentage)
        animateFire(current, goal)
    }
    
    private fun animateWave(percentage: Int) {
        // User requested to remove the rising animation and keep it in initial state.
        // We only start the horizontal flow if not already running.
        
        waterWave.post {
            if (waterWave.animation == null) {
                val flowAnim = android.animation.ObjectAnimator.ofFloat(waterWave, "translationX", -100f, 100f).apply {
                    duration = 3000
                    repeatCount = android.animation.ValueAnimator.INFINITE
                    repeatMode = android.animation.ValueAnimator.REVERSE
                    interpolator = android.view.animation.LinearInterpolator()
                }
                flowAnim.start()
            }
        }
    }

    private fun animateFire(totalConsumed: Int, dailyGoal: Int) {
        val fireIcon = findViewById<android.widget.ImageView>(R.id.iv_streak_fire)
        
        val scaleX = android.animation.ObjectAnimator.ofFloat(fireIcon, "scaleX", 1f, 1.3f, 0.9f, 1f)
        val scaleY = android.animation.ObjectAnimator.ofFloat(fireIcon, "scaleY", 1f, 1.3f, 0.9f, 1f)
        
        android.animation.AnimatorSet().apply {
            // updateProgressUI(totalConsumed, dailyGoal) // This line is not part of animateFire's responsibility
            // checkGoalReached(totalConsumed, dailyGoal) // This line is not part of animateFire's responsibility
            
            // Ensure permanent notification represents truth
            lifecycleScope.launch {
                notificationHelper.showPermanentNotification()
            }
            interpolator = android.view.animation.BounceInterpolator()
            scaleX.repeatCount = android.animation.ValueAnimator.INFINITE
            scaleY.repeatCount = android.animation.ValueAnimator.INFINITE
            start()
        }
    }
    
    private fun animateEntry() {
        val buttons = listOf(
            findViewById<android.view.View>(R.id.btn_add_200),
            findViewById<android.view.View>(R.id.btn_add_300),
            findViewById<android.view.View>(R.id.btn_add_500)
        )
        
        buttons.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 100f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(index * 100L + 500)
                .setDuration(500)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }
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
        // Recuperar a hora do próximo alarme salvo pelo Helper
        val prefs = getSharedPreferences("hidrate_prefs", android.content.Context.MODE_PRIVATE)
        val nextTimeStr = prefs.getString("next_alarm_time_str", "---")
        
        tvNextNotification.text = "Próximo: $nextTimeStr"
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
