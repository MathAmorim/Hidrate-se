package com.example.base.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.base.MainActivity
import com.example.base.R
import com.example.base.receiver.NotificationReceiver

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "water_reminder_channel"
        const val NOTIFICATION_ID = 1001
        
        const val ACTION_ADD_200 = "com.example.base.ACTION_ADD_200"
        const val ACTION_ADD_300 = "com.example.base.ACTION_ADD_300"
        const val ACTION_ADD_500 = "com.example.base.ACTION_ADD_500"
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Lembretes de Água"
            val descriptionText = "Notificações para beber água"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Schedule notifications for the day based on user's active hours
    suspend fun scheduleDailyNotifications() {
        val database = com.example.base.data.AppDatabase.getDatabase(context)
        val user = database.userDao().getUser() ?: return
        
        // Use default times if parsing fails, but try to parse user settings
        val wakeUpParts = user.wakeUpTime.split(":").mapNotNull { it.toIntOrNull() }
        val sleepParts = user.sleepTime.split(":").mapNotNull { it.toIntOrNull() }
        
        val startHour = if (wakeUpParts.size == 2) wakeUpParts[0] else 8
        val endHour = if (sleepParts.size == 2) sleepParts[0] else 22
        
        // Cancel existing alarms first
        cancelNotifications()
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = java.util.Calendar.getInstance()
        
        // Schedule a few random notifications throughout the day
        // For simplicity, let's aim for one every 2-3 hours within the window
        var currentHour = startHour
        
        while (currentHour < endHour) {
            val calendar = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, currentHour)
                set(java.util.Calendar.MINUTE, (0..59).random()) // Random minute
                set(java.util.Calendar.SECOND, 0)
            }
            
            // If the time is already passed for today, schedule for tomorrow? 
            // Actually, for "daily" scheduling, we usually run this when settings change or boot.
            // If it's passed, we just skip it for today.
            if (calendar.timeInMillis > now.timeInMillis) {
                scheduleAlarm(calendar.timeInMillis, alarmManager)
            }
            
            currentHour += (2..3).random() // Next reminder in 2-3 hours
        }
    }

    private fun scheduleAlarm(triggerTime: Long, alarmManager: AlarmManager) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.example.base.ACTION_SHOW_NOTIFICATION"
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            triggerTime.toInt(), // Unique ID based on time to allow multiple alarms
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    fun cancelNotifications() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
             action = "com.example.base.ACTION_SHOW_NOTIFICATION"
        }
        // We can't easily cancel all specific pending intents without their IDs.
        // A common strategy is to use a fixed range of IDs or just cancel the PendingIntent if we knew the ID.
        // For this simple implementation, we might need a more robust ID tracking if we want to cancel perfectly.
        // However, since we generate IDs based on time, it's tricky. 
        // A simpler approach for this scope: Just cancel the one "main" recurring alarm if we were using setRepeating.
        // Since we are scheduling multiple exact alarms, we should ideally store their IDs.
        // For now, let's assume we are just scheduling one next alarm or we accept that 'cancel' might be limited 
        // without a DB of scheduled alarms. 
        // ALTERNATIVE: Use a single PendingIntent request code if we only ever want ONE future alarm at a time.
        // But the requirement says "randomly within active hours".
        
        // To properly cancel, let's just try to cancel a range of potential IDs or use a specific one if we change logic.
        // Let's stick to the plan: The user might have multiple. 
        // For this iteration, let's just try to cancel the PendingIntent with ID 0 (legacy) and maybe we don't strictly cancel all previous ones 
        // if we don't track them, which is a limitation. 
        // IMPROVEMENT: Let's use a fixed set of Request Codes (e.g., 100, 101, 102...) for the daily slots.
        
        for (i in 0..10) { // Cancel up to 10 slots
             val pendingIntent = PendingIntent.getBroadcast(
                context,
                i,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
    
    // Modified to use fixed IDs for scheduling to allow cancellation
    private fun scheduleAlarmWithId(triggerTime: Long, alarmManager: AlarmManager, id: Int) {
         val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.example.base.ACTION_SHOW_NOTIFICATION"
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id, 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // ... (same alarm setting logic)
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    // Overloaded for the loop
    suspend fun scheduleDailyNotificationsOptimized() {
        val database = com.example.base.data.AppDatabase.getDatabase(context)
        val user = database.userDao().getUser() ?: return
        
        val wakeUpParts = user.wakeUpTime.split(":").mapNotNull { it.toIntOrNull() }
        val sleepParts = user.sleepTime.split(":").mapNotNull { it.toIntOrNull() }
        
        val startHour = if (wakeUpParts.size == 2) wakeUpParts[0] else 8
        val endHour = if (sleepParts.size == 2) sleepParts[0] else 22
        
        cancelNotifications() // Cancel previous 0..10 IDs
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = java.util.Calendar.getInstance()
        
        var currentHour = startHour
        var slotId = 0
        
        while (currentHour < endHour && slotId < 10) {
            val calendar = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, currentHour)
                set(java.util.Calendar.MINUTE, (0..59).random())
                set(java.util.Calendar.SECOND, 0)
            }
            
            if (calendar.timeInMillis > now.timeInMillis) {
                scheduleAlarmWithId(calendar.timeInMillis, alarmManager, slotId)
            }
            
            currentHour += (2..3).random()
            slotId++
        }
        
        // Schedule next day planning
        scheduleNextDayPlanning(startHour, alarmManager)
    }

    private fun scheduleNextDayPlanning(startHour: Int, alarmManager: AlarmManager) {
        val nextDay = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.DAY_OF_YEAR, 1)
            set(java.util.Calendar.HOUR_OF_DAY, startHour)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }
        
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "android.intent.action.BOOT_COMPLETED" // Reuse boot logic to reschedule
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            999, // Special ID for daily planner
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextDay.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextDay.timeInMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextDay.timeInMillis,
                pendingIntent
            )
        }
    }

    suspend fun showNotification() {
        val database = com.example.base.data.AppDatabase.getDatabase(context)
        
        // Check if goal is met
        val today = com.example.base.util.DateUtils.getCurrentDate()
        val history = database.waterRecordDao().getRecordsByDate(today)
        val totalWater = history.sumOf { it.amount }
        val user = database.userDao().getUser()
        
        if (user != null && totalWater >= user.dailyGoal) {
            return // Goal met, don't notify
        }

        // Create an explicit intent for an Activity in your app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        // Action Intents
        val add200Intent = Intent(context, NotificationReceiver::class.java).apply { action = ACTION_ADD_200 }
        val add200Pending = PendingIntent.getBroadcast(context, 200, add200Intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        
        val add300Intent = Intent(context, NotificationReceiver::class.java).apply { action = ACTION_ADD_300 }
        val add300Pending = PendingIntent.getBroadcast(context, 300, add300Intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        
        val add500Intent = Intent(context, NotificationReceiver::class.java).apply { action = ACTION_ADD_500 }
        val add500Pending = PendingIntent.getBroadcast(context, 500, add500Intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_droplet)
            .setContentTitle("Hora de se hidratar!")
            .setContentText("Beba um copo de água para manter sua meta em dia.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(0, "+200ml", add200Pending)
            .addAction(0, "+300ml", add300Pending)
            .addAction(0, "+500ml", add500Pending)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
