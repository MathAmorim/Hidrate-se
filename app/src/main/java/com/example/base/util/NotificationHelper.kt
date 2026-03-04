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
import com.example.base.util.MotivationManager

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "water_reminder_channel"
        const val PERMANENT_CHANNEL_ID = "water_permanent_channel"
        const val NOTIFICATION_ID = 1001
        const val PERMANENT_NOTIFICATION_ID = 2002
        
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
            
            val permName = "Painel Fixo de Hidratação"
            val permDesc = "Atalhos contínuos para água"
            val permImportance = NotificationManager.IMPORTANCE_LOW // Silencioso
            val permChannel = NotificationChannel(PERMANENT_CHANNEL_ID, permName, permImportance).apply {
                description = permDesc
                setShowBadge(false)
            }
            
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(permChannel)
        }
    }

    // OBSOLETO: Mantido apenas por assinatura, roteando para a nova otimizada.
    suspend fun scheduleDailyNotifications() {
        scheduleDailyNotificationsOptimized()
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
    
    // Schedule next notification dynamically based on remaining water and wake/sleep times
    suspend fun scheduleDailyNotificationsOptimized() {
        val database = com.example.base.data.AppDatabase.getDatabase(context)
        val user = database.userDao().getUser() ?: return
        
        cancelNotifications() // Sempre limpar pendentes para agendar apenas O PRÓXIMO alvo

        val today = com.example.base.util.DateUtils.getCurrentDate()
        val history = database.waterRecordDao().getRecordsByDate(today)
        val totalWater = history.sumOf { it.amount }
        
        val wakeUpParts = user.wakeUpTime.split(":").mapNotNull { it.toIntOrNull() }
        val sleepParts = user.sleepTime.split(":").mapNotNull { it.toIntOrNull() }
        
        val startHour = if (wakeUpParts.size == 2) wakeUpParts[0] else 8
        val startMinute = if (wakeUpParts.size == 2) wakeUpParts[1] else 0
        val endHour = if (sleepParts.size == 2) sleepParts[0] else 22
        val endMinute = if (sleepParts.size == 2) sleepParts[1] else 0
        
        val now = java.util.Calendar.getInstance()
        
        // --- 1. Ponte Matinal para o dia seguinte ---
        val wakeUpToday = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, startHour)
            set(java.util.Calendar.MINUTE, startMinute)
            set(java.util.Calendar.SECOND, 0)
        }
        
        val sleepToday = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, endHour)
            set(java.util.Calendar.MINUTE, endMinute)
            set(java.util.Calendar.SECOND, 0)
        }
        
        // Se a hora de dormir for menor que acordar (ex: dorme à 01h e acorda às 08h), cruza a meia-noite
        if (sleepToday.timeInMillis <= wakeUpToday.timeInMillis) {
            if (now.get(java.util.Calendar.HOUR_OF_DAY) < 12) {
                // De manhã: a hora de dormir é hoje à noite/madrugada de amanhã
                 sleepToday.add(java.util.Calendar.DAY_OF_YEAR, 1)
            } else {
                 // De tarde/noite: a hora de dormir é amanhã de madrugada, wakeUp foi hoje de manhã
                 sleepToday.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }

        // Se a meta foi batida: programa a "Ponte Matinal" para acordar amanhã e finaliza
        if (totalWater >= user.dailyGoal) {
            scheduleNextDayPlanning(startHour, startMinute, alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
            val prefs = context.getSharedPreferences("hidrate_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("next_alarm_time_str", "Amanhã").apply()
            return
        }
        
        // --- 2. Fora de Horas (Madrugada): Agenda Alarme Estrito para o Acordar ---
        if (now.timeInMillis < wakeUpToday.timeInMillis || now.timeInMillis >= sleepToday.timeInMillis) {
             // É noite, agenda o Despertador amanhã ou hoje
             val nextWakeUp = if (now.timeInMillis >= sleepToday.timeInMillis) {
                 java.util.Calendar.getInstance().apply {
                     timeInMillis = wakeUpToday.timeInMillis
                     add(java.util.Calendar.DAY_OF_YEAR, 1) // É amanhã
                 }
             } else {
                 wakeUpToday // É hoje de manhã
             }
             
             scheduleAlarmWithId(nextWakeUp.timeInMillis, context.getSystemService(Context.ALARM_SERVICE) as AlarmManager, 0)
             saveAlarmTimeForUI(nextWakeUp.timeInMillis)
             return
        }

        // --- 3. Matemática e Jitter (Durante o dia) ---
        val remainingWater = user.dailyGoal - totalWater
        // Assumimos "Copo padrão" estimado em cerca de 250ml
        var glassesRemaining = remainingWater.toFloat() / 250f
        if (glassesRemaining < 1f) glassesRemaining = 1f // Mesmo se faltar apenas 50ml, trata como 1 ciclo

        val timeRemainingMillis = sleepToday.timeInMillis - now.timeInMillis
        var baseIntervalMillis = (timeRemainingMillis / glassesRemaining).toLong()

        // Garantir intervalos saudáveis (Mín. 45 min, Máx. 3.5 horas)
        if (baseIntervalMillis < 45 * 60 * 1000L) baseIntervalMillis = 45 * 60 * 1000L
        if (baseIntervalMillis > 3.5 * 60 * 60 * 1000L) baseIntervalMillis = (3.5 * 60 * 60 * 1000L).toLong()

        // Aplicar Variação (Jitter de ±20%)
        val variationPercent = (-20..20).random() / 100.0
        val jitterMillis = (baseIntervalMillis * variationPercent).toLong()
        
        var nextAlarmTime = now.timeInMillis + baseIntervalMillis + jitterMillis

        // Assegurar que o jitter não atire o alarme para depois de dormir ou para o passado
        if (nextAlarmTime > sleepToday.timeInMillis) {
            nextAlarmTime = sleepToday.timeInMillis - (15 * 60 * 1000L) // Limite de 15min antes de dormir
        }
        if (nextAlarmTime <= now.timeInMillis) {
             nextAlarmTime = now.timeInMillis + (15 * 60 * 1000L) // Minimo safe de alarme: daqui a 15 mins
        }

        // Agendar este único pulso (Com Jitter)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        scheduleAlarmWithId(nextAlarmTime, alarmManager, 0)
        saveAlarmTimeForUI(nextAlarmTime)
        
        // Certificar a ponte matinal, na dúvida
        scheduleNextDayPlanning(startHour, startMinute, alarmManager)
    }

    private fun saveAlarmTimeForUI(timeInMillis: Long) {
        val prefs = context.getSharedPreferences("hidrate_prefs", Context.MODE_PRIVATE)
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        prefs.edit().putString("next_alarm_time_str", sdf.format(timeInMillis)).apply()
    }

    private fun scheduleNextDayPlanning(startHour: Int, startMinute: Int, alarmManager: AlarmManager) {
        val nextDay = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.DAY_OF_YEAR, 1)
            set(java.util.Calendar.HOUR_OF_DAY, startHour)
            set(java.util.Calendar.MINUTE, startMinute)
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

        val goal = user?.dailyGoal ?: 2000
        val percentage = if (goal > 0) (totalWater * 100 / goal) else 0
        val motivationPhrase = MotivationManager.getPhrase(percentage)

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
            .setContentText(motivationPhrase)
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

    suspend fun showGoalReachedNotification() {
        val database = com.example.base.data.AppDatabase.getDatabase(context)
        val user = database.userDao().getUser() ?: return
        
        val today = com.example.base.util.DateUtils.getCurrentDate()
        val history = database.waterRecordDao().getRecordsByDate(today)
        val totalWater = history.sumOf { it.amount }
        
        // Only notify if goal is reached or exceeded
        if (totalWater >= user.dailyGoal) {
            // Check if it was JUST reached (i.e., without the last entry, it was below)
            // This prevents spamming if they drink more after reaching the goal
            val lastEntry = history.maxByOrNull { it.timestamp }
            val previousTotal = totalWater - (lastEntry?.amount ?: 0)
            
            if (previousTotal < user.dailyGoal) {
                val phrase = MotivationManager.getPhrase(100)
                
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent: PendingIntent = PendingIntent.getActivity(
                    context, 0, intent, PendingIntent.FLAG_IMMUTABLE
                )

                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_trophy_new)
                    .setContentTitle("Meta Batida! 🎉")
                    .setContentText(phrase)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                try {
                    with(NotificationManagerCompat.from(context)) {
                        notify(NOTIFICATION_ID + 1, builder.build()) // Different ID
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }
        
        // Sempre que o ecrã atualiza ou bate meta, se o widget fixo estiver ON, atualiza-o
        if (user.isPermanentNotificationEnabled) {
            showPermanentNotification()
        }
    }
    
    suspend fun showPermanentNotification() {
         val database = com.example.base.data.AppDatabase.getDatabase(context)
         val user = database.userDao().getUser()
         if (user == null || !user.isPermanentNotificationEnabled) return
         
         val today = com.example.base.util.DateUtils.getCurrentDate()
         val history = database.waterRecordDao().getRecordsByDate(today)
         val totalWater = history.sumOf { it.amount }
         val goal = user.dailyGoal
         
         val percentage = if (goal > 0) (totalWater * 100 / goal) else 0
         val displayPhrase = "${totalWater}ml / ${goal}ml"
         
         val motivationText = if (percentage >= 100) {
             MotivationManager.getPhrase(percentage)
         } else {
             "Faltam ${goal - totalWater}ml para a meta"
         }
         
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
 
         val builder = NotificationCompat.Builder(context, PERMANENT_CHANNEL_ID)
             .setSmallIcon(R.drawable.ic_droplet)
             .setContentTitle("Hidrate-se: $displayPhrase")
             .setContentText(motivationText)
             .setPriority(NotificationCompat.PRIORITY_LOW) // Silencioso
             .setOngoing(true) // Assinala como trabalho contínuo
             .setContentIntent(pendingIntent)
             
         if (percentage < 100) {
             builder.addAction(0, "+200ml", add200Pending)
                    .addAction(0, "+300ml", add300Pending)
                    .addAction(0, "+500ml", add500Pending)
         }
 
         try {
             val notification = builder.build()
             // Forçar inamovibilidade no Android
             notification.flags = notification.flags or NotificationCompat.FLAG_ONGOING_EVENT or NotificationCompat.FLAG_NO_CLEAR
             
             with(NotificationManagerCompat.from(context)) {
                 notify(PERMANENT_NOTIFICATION_ID, notification)
             }
         } catch (e: SecurityException) {
             e.printStackTrace()
         }
    }
    
    fun hidePermanentNotification() {
         try {
             with(NotificationManagerCompat.from(context)) {
                 cancel(PERMANENT_NOTIFICATION_ID)
             }
         } catch (e: SecurityException) {
             e.printStackTrace()
         }
    }
}
