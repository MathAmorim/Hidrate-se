package com.example.base.util

import com.example.base.data.model.WaterRecord
import java.util.Calendar

object StreakManager {

    fun calculateStreak(records: List<WaterRecord>, dailyGoal: Int): Int {
        if (records.isEmpty()) return 0

        // Group by day and sum amounts
        val dailyTotals = records.groupBy { 
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.timestamp
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.mapValues { entry ->
            entry.value.sumOf { it.amount }
        }

        // Filter days where goal was met
        val successfulDays = dailyTotals.filter { it.value >= dailyGoal }
                                        .keys
                                        .sortedDescending()

        if (successfulDays.isEmpty()) return 0

        var streak = 0
        val today = getStartOfDay()
        val yesterday = today - 86400000

        // Check if the streak is active (today or yesterday must be successful)
        var currentCheck = if (successfulDays.contains(today)) today else yesterday
        
        if (!successfulDays.contains(currentCheck)) {
             return 0
        }

        for (day in successfulDays) {
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

    private fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
