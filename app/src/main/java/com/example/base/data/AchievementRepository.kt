package com.example.base.data

import com.example.base.R
import com.example.base.data.dao.UserDao
import com.example.base.data.dao.WaterRecordDao
import com.example.base.data.model.Achievement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AchievementRepository(
    private val waterRecordDao: WaterRecordDao,
    private val userDao: UserDao
) {

    suspend fun getAchievements(): List<Achievement> = withContext(Dispatchers.IO) {
        val records = waterRecordDao.getAllRecords()
        val user = userDao.getUser()
        
        val achievements = mutableListOf<Achievement>()

        // --- Unique Achievements ---

        // 1. O Primeiro Gole (First Sip)
        achievements.add(
            Achievement(
                id = "first_sip",
                title = "O Primeiro Gole",
                description = "Registrou o primeiro consumo de água no aplicativo.",
                iconResId = R.drawable.ic_droplet,
                isUnlocked = records.isNotEmpty(),
                progress = if (records.isNotEmpty()) 1 else 0,
                maxProgress = 1
            )
        )

        // 2. Hidratação Expressa (Express Hydration - Notification)
        val hasNotificationSource = records.any { it.source == "NOTIFICATION" }
        achievements.add(
            Achievement(
                id = "express_hydration",
                title = "Hidratação Expressa",
                description = "Adicionou água diretamente pela Notificação.",
                iconResId = R.drawable.ic_bell,
                isUnlocked = hasNotificationSource,
                progress = if (hasNotificationSource) 1 else 0,
                maxProgress = 1
            )
        )

        // 3. Sempre à Mão (Always Handy - Widget)
        val hasWidgetSource = records.any { it.source == "WIDGET" }
        achievements.add(
            Achievement(
                id = "always_handy",
                title = "Sempre à Mão",
                description = "Adicionou água utilizando o Widget.",
                iconResId = R.drawable.ic_star, // Placeholder if no widget icon
                isUnlocked = hasWidgetSource,
                progress = if (hasWidgetSource) 1 else 0,
                maxProgress = 1
            )
        )

        // 4. Madrugador (Early Bird - Before 09:00)
        val hasEarlyDrink = records.any {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = it.timestamp
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            hour < 9
        }
        achievements.add(
            Achievement(
                id = "early_bird",
                title = "Madrugador",
                description = "Bebeu água antes das 09:00 da manhã.",
                iconResId = R.drawable.ic_day_forecast,
                isUnlocked = hasEarlyDrink,
                progress = if (hasEarlyDrink) 1 else 0,
                maxProgress = 1
            )
        )

        // 5. Até a Última Gota (Last Drop - Last hour of active time)
        // Need user sleep time. Assuming format "HH:mm"
        var hasLastHourDrink = false
        if (user != null) {
            val sleepHour = user.sleepTime.split(":")[0].toIntOrNull() ?: 22
            hasLastHourDrink = records.any {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = it.timestamp
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                hour == (sleepHour - 1 + 24) % 24 // Simple check for previous hour
            }
        }
        achievements.add(
            Achievement(
                id = "last_drop",
                title = "Até a Última Gota",
                description = "Registrou consumo na última hora do seu horário ativo.",
                iconResId = R.drawable.ic_night_moon,
                isUnlocked = hasLastHourDrink,
                progress = if (hasLastHourDrink) 1 else 0,
                maxProgress = 1
            )
        )

        // 6. Super-Hidratado (200% goal)
        var hasSuperHydrated = false
        if (user != null && user.dailyGoal > 0) {
            val dailyTotals = records.groupBy { it.date }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
            hasSuperHydrated = dailyTotals.any { it.value >= user.dailyGoal * 2 }
        }
        achievements.add(
            Achievement(
                id = "super_hydrated",
                title = "Super-Hidratado",
                description = "Atingiu 200% da meta diária em um único dia.",
                iconResId = R.drawable.ic_water_tank,
                isUnlocked = hasSuperHydrated,
                progress = if (hasSuperHydrated) 1 else 0,
                maxProgress = 1
            )
        )

        // --- Repetitive Achievements ---

        // Streak Calculation
        // This is complex. We need to check consecutive days meeting the goal.
        // For simplicity, let's assume we calculate the *current* streak or *max* streak.
        // The requirement implies "levels", so we should check if they *ever* reached that streak.
        // But calculating "max streak ever" from raw records is heavy.
        // Let's calculate the current streak for now, or a simple max streak if possible.
        // Actually, we can just group by date, sum amounts, check against goal, then find max sequence.
        
        var maxStreak = 0
        if (user != null && user.dailyGoal > 0) {
            val dailyTotals = records.groupBy { it.date } // date format YYYY-MM-DD expected
                .mapValues { entry -> entry.value.sumOf { it.amount } }
            
            val sortedDates = dailyTotals.keys.sorted()
            var currentStreak = 0
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            // We need to iterate through dates and check continuity
            // This is a simplified version. A robust one would fill in missing dates with 0.
            // But here we only have records for days they drank.
            // If a day is missing, streak breaks.
            
            // Let's iterate from first recorded day to last.
            if (sortedDates.isNotEmpty()) {
                val firstDate = sdf.parse(sortedDates.first())
                val lastDate = sdf.parse(sortedDates.last())
                val calendar = Calendar.getInstance()
                calendar.time = firstDate!!
                
                while (!calendar.time.after(lastDate)) {
                    val dateStr = sdf.format(calendar.time)
                    val total = dailyTotals[dateStr] ?: 0
                    if (total >= user.dailyGoal) {
                        currentStreak++
                    } else {
                        maxStreak = maxOf(maxStreak, currentStreak)
                        currentStreak = 0
                    }
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
                maxStreak = maxOf(maxStreak, currentStreak)
            }
        }

        val streakLevels = listOf(3, 7, 14, 30, 100)
        streakLevels.forEachIndexed { index, target ->
            achievements.add(
                Achievement(
                    id = "streak_$target",
                    title = "Chama da Consistência Nível ${index + 1}",
                    description = "$target dias seguidos batendo a meta.",
                    iconResId = R.drawable.ic_fire,
                    isUnlocked = maxStreak >= target,
                    progress = minOf(maxStreak, target),
                    maxProgress = target
                )
            )
        }

        // Volume Calculation
        val totalVolume = records.sumOf { it.amount.toLong() } / 1000 // Liters
        val volumeLevels = listOf(10, 50, 100, 250)
        volumeLevels.forEachIndexed { index, target ->
             achievements.add(
                Achievement(
                    id = "volume_$target",
                    title = "Oceano Interior Nível ${index + 1}",
                    description = "Bebeu $target Litros no total.",
                    iconResId = R.drawable.ic_glass_water_full,
                    isUnlocked = totalVolume >= target,
                    progress = minOf(totalVolume.toInt(), target),
                    maxProgress = target
                )
            )
        }

        // Goals Met Calculation
        var goalsMetCount = 0
        if (user != null && user.dailyGoal > 0) {
             val dailyTotals = records.groupBy { it.date }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
             goalsMetCount = dailyTotals.count { it.value >= user.dailyGoal }
        }
        
        val goalLevels = listOf(10, 50, 365)
        goalLevels.forEachIndexed { index, target ->
             achievements.add(
                Achievement(
                    id = "goals_met_$target",
                    title = "Na Mosca Nível ${index + 1}",
                    description = "Bateu a meta $target vezes.",
                    iconResId = R.drawable.ic_target,
                    isUnlocked = goalsMetCount >= target,
                    progress = minOf(goalsMetCount, target),
                    maxProgress = target
                )
            )
        }

        achievements
    }
}
