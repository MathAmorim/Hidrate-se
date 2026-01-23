package com.example.base

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.base.data.AppDatabase
import com.example.base.data.model.WaterRecord
import com.example.base.util.DateUtils
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class StatisticsActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    
    // Views
    private lateinit var tvTodayTotal: TextView
    private lateinit var tvWeeklyAvg: TextView
    private lateinit var tvCurrentStreak: TextView
    private lateinit var tvTotalVolume: TextView
    private lateinit var tvBestDay: TextView
    private lateinit var tvBestDayAmount: TextView
    private lateinit var chartWeekly: BarChart
    private lateinit var chartMonthly: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        db = AppDatabase.getDatabase(this)

        initViews()
        setupToolbar()
        setupCharts()
        loadStatistics()
    }

    private fun initViews() {
        tvTodayTotal = findViewById(R.id.tv_today_total)
        tvWeeklyAvg = findViewById(R.id.tv_weekly_avg)
        tvCurrentStreak = findViewById(R.id.tv_current_streak)
        tvTotalVolume = findViewById(R.id.tv_total_volume)
        tvBestDay = findViewById(R.id.tv_best_day)
        tvBestDayAmount = findViewById(R.id.tv_best_day_amount)
        chartWeekly = findViewById(R.id.chart_weekly)
        chartMonthly = findViewById(R.id.chart_monthly)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupCharts() {
        // Weekly Chart Setup
        chartWeekly.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            axisRight.isEnabled = false
            axisLeft.textColor = Color.WHITE
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.WHITE
            xAxis.setDrawGridLines(false)
            setTouchEnabled(false)
            animateY(1000)
        }

        // Monthly Chart Setup
        chartMonthly.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            axisRight.isEnabled = false
            axisLeft.textColor = Color.WHITE
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.WHITE
            xAxis.setDrawGridLines(false)
            setTouchEnabled(true)
            animateX(1000)
        }
    }

    private fun loadStatistics() {
        lifecycleScope.launch(Dispatchers.IO) {
            val todayStart = DateUtils.getStartOfDay()
            val todayEnd = DateUtils.getEndOfDay()
            
            // 1. Today's Total
            val todayTotal = db.waterRecordDao().getTotalForDay(todayStart, todayEnd) ?: 0

            // 2. Total Volume
            val totalVolume = db.waterRecordDao().getTotalVolume() ?: 0L

            // 3. Streak
            val timestamps = db.waterRecordDao().getAllTimestamps()
            val streak = calculateStreak(timestamps)

            // 4. Weekly Data (Last 7 days)
            val weeklyData = getDailyTotals(7)
            val weeklyAvg = if (weeklyData.isNotEmpty()) weeklyData.values.sum() / 7 else 0

            // 5. Monthly Data (Last 30 days)
            val monthlyData = getDailyTotals(30)

            // 6. Best Day
            // Fetch all records to find best day. 
            // For efficiency in a real app with years of data, we'd use a custom query.
            // Here we'll just use the last 365 days or all if feasible.
            // Let's use the last 365 days for "Best Day" to keep it reasonable.
            val yearlyData = getDailyTotals(365)
            val bestDayEntry = yearlyData.maxByOrNull { it.value }

            withContext(Dispatchers.Main) {
                updateUI(todayTotal, totalVolume, streak, weeklyAvg, weeklyData, monthlyData, bestDayEntry)
            }
        }
    }

    private suspend fun getDailyTotals(daysBack: Int): Map<Long, Int> {
        val calendar = Calendar.getInstance()
        val end = DateUtils.getEndOfDay()
        
        calendar.add(Calendar.DAY_OF_YEAR, -daysBack + 1) // +1 to include today
        val start = DateUtils.getStartOfDay(calendar.timeInMillis)

        val records = db.waterRecordDao().getRecordsBetween(start, end)
        
        // Group by day
        val dailyTotals = mutableMapOf<Long, Int>()
        
        // Initialize all days with 0
        for (i in 0 until daysBack) {
            val dayStart = DateUtils.getStartOfDay(start + (i * 86400000L))
            dailyTotals[dayStart] = 0
        }

        records.forEach { record ->
            val dayStart = DateUtils.getStartOfDay(record.timestamp)
            val current = dailyTotals[dayStart] ?: 0
            dailyTotals[dayStart] = current + record.amount
        }

        return dailyTotals.toSortedMap()
    }

    private fun calculateStreak(timestamps: List<Long>): Int {
        if (timestamps.isEmpty()) return 0

        val distinctDays = timestamps.map { DateUtils.getStartOfDay(it) }
            .distinct()
            .sortedDescending()

        var streak = 0
        val today = DateUtils.getStartOfDay()
        val yesterday = today - 86400000

        if (distinctDays.isEmpty()) return 0
        
        var currentCheck = if (distinctDays.contains(today)) today else yesterday
        
        if (!distinctDays.contains(currentCheck)) {
             return 0
        }

        for (day in distinctDays) {
            if (day == currentCheck) {
                streak++
                currentCheck -= 86400000
            } else if (day > currentCheck) {
                continue 
            } else {
                break
            }
        }
        return streak
    }

    private fun updateUI(
        todayTotal: Int,
        totalVolume: Long,
        streak: Int,
        weeklyAvg: Int,
        weeklyData: Map<Long, Int>,
        monthlyData: Map<Long, Int>,
        bestDayEntry: Map.Entry<Long, Int>?
    ) {
        // Cards
        tvTodayTotal.text = "${todayTotal}ml"
        tvTotalVolume.text = String.format("%.1fL", totalVolume / 1000f)
        tvCurrentStreak.text = "$streak dias"
        tvWeeklyAvg.text = "${weeklyAvg}ml"

        if (bestDayEntry != null) {
            tvBestDay.text = DateUtils.formatDate(bestDayEntry.key)
            tvBestDayAmount.text = "${bestDayEntry.value}ml"
        } else {
            tvBestDay.text = "--"
            tvBestDayAmount.text = "0ml"
        }

        // Weekly Chart
        setupWeeklyChart(weeklyData)

        // Monthly Chart
        setupMonthlyChart(monthlyData)
    }

    private fun setupWeeklyChart(data: Map<Long, Int>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        
        var index = 0f
        data.forEach { (timestamp, amount) ->
            entries.add(BarEntry(index, amount.toFloat()))
            labels.add(DateUtils.getDayOfWeek(timestamp))
            index++
        }

        val dataSet = BarDataSet(entries, "Consumo Diário")
        dataSet.color = Color.parseColor("#3b82f6") // Primary Color
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 10f

        val barData = BarData(dataSet)
        chartWeekly.data = barData
        chartWeekly.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chartWeekly.invalidate()
    }

    private fun setupMonthlyChart(data: Map<Long, Int>) {
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()
        
        var index = 0f
        data.forEach { (timestamp, amount) ->
            entries.add(Entry(index, amount.toFloat()))
            labels.add(DateUtils.getDayOfMonth(timestamp))
            index++
        }

        val dataSet = LineDataSet(entries, "Consumo Mensal")
        dataSet.color = Color.parseColor("#10b981") // Secondary Color
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 9f
        dataSet.lineWidth = 2f
        dataSet.setCircleColor(Color.WHITE)
        dataSet.circleRadius = 3f
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#10b981")
        dataSet.fillAlpha = 50

        val lineData = LineData(dataSet)
        chartMonthly.data = lineData
        chartMonthly.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        // Show fewer labels for monthly to avoid crowding
        chartMonthly.xAxis.labelCount = 6 
        chartMonthly.invalidate()
    }
}
