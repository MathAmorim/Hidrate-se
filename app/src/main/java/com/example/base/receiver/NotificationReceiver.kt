package com.example.base.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.base.util.NotificationHelper
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationHelper = NotificationHelper(context)
        
        val pendingResult = goAsync()
        
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                        notificationHelper.scheduleDailyNotificationsOptimized()
                    }
                    NotificationHelper.ACTION_ADD_200 -> {
                        addWater(context, 200)
                        // Cancel notification after action
                        androidx.core.app.NotificationManagerCompat.from(context).cancel(NotificationHelper.NOTIFICATION_ID)
                    }
                    NotificationHelper.ACTION_ADD_300 -> {
                        addWater(context, 300)
                        androidx.core.app.NotificationManagerCompat.from(context).cancel(NotificationHelper.NOTIFICATION_ID)
                    }
                    NotificationHelper.ACTION_ADD_500 -> {
                        addWater(context, 500)
                        androidx.core.app.NotificationManagerCompat.from(context).cancel(NotificationHelper.NOTIFICATION_ID)
                    }
                    "com.example.base.ACTION_SHOW_NOTIFICATION" -> {
                        notificationHelper.showNotification()
                    }
                    else -> {
                        // Default behavior for simple alarm
                        notificationHelper.showNotification()
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
    
    private suspend fun addWater(context: Context, amount: Int) {
        val database = com.example.base.data.AppDatabase.getDatabase(context)
        val record = com.example.base.data.model.WaterRecord(
            amount = amount,
            date = com.example.base.util.DateUtils.getCurrentDate(),
            timestamp = System.currentTimeMillis()
        )
        database.waterRecordDao().insert(record)
        
        // Update Widget
        val intent = Intent(context, com.example.base.widget.WaterWidgetProvider::class.java)
        intent.action = com.example.base.widget.WaterWidgetProvider.ACTION_UPDATE_WIDGET
        context.sendBroadcast(intent)
    }
}
