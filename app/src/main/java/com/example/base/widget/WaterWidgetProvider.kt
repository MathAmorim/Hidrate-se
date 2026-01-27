package com.example.base.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.base.R
import com.example.base.data.AppDatabase
import com.example.base.data.model.WaterRecord
import com.example.base.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WaterWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_ADD_200 = "com.example.base.widget.ACTION_ADD_200"
        const val ACTION_ADD_500 = "com.example.base.widget.ACTION_ADD_500"
        const val ACTION_UPDATE_WIDGET = "com.example.base.widget.ACTION_UPDATE_WIDGET"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val action = intent.action
        if (action == ACTION_ADD_200 || action == ACTION_ADD_500) {
            val amount = if (action == ACTION_ADD_200) 200 else 500
            
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val record = WaterRecord(
                        amount = amount,
                        date = DateUtils.getCurrentDate(),
                        timestamp = System.currentTimeMillis()
                    )
                    db.waterRecordDao().insert(record)
                    
                    // Trigger update for all widgets
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val componentName = ComponentName(context, WaterWidgetProvider::class.java)
                    val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                    
                    appWidgetIds.forEach { appWidgetId ->
                        updateAppWidget(context, appWidgetManager, appWidgetId)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        } else if (action == ACTION_UPDATE_WIDGET) {
             val appWidgetManager = AppWidgetManager.getInstance(context)
             val componentName = ComponentName(context, WaterWidgetProvider::class.java)
             val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
             onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val todayStart = DateUtils.getStartOfDay()
                val todayEnd = DateUtils.getEndOfDay()
                
                val current = db.waterRecordDao().getTotalForDay(todayStart, todayEnd) ?: 0
                val user = db.userDao().getUser()
                val goal = user?.dailyGoal ?: 2000
                
                val views = RemoteViews(context.packageName, R.layout.widget_small)
                
                // Update Texts
                views.setTextViewText(R.id.widget_tv_progress, "${current}ml / ${goal}ml")
                
                // Update Progress
                val percentage = if (goal > 0) (current * 100 / goal) else 0
                views.setProgressBar(R.id.widget_progress_bar, 100, percentage.coerceIn(0, 100), false)
                
                // Update Incentive
                val incentive = when {
                    percentage >= 100 -> "Meta Batida! 🎉"
                    percentage >= 75 -> "Quase lá! 💪"
                    percentage >= 50 -> "Metade já foi! 💧"
                    else -> "Mantenha-se hidratado!"
                }
                views.setTextViewText(R.id.widget_tv_incentive, incentive)

                // Setup Buttons
                views.setOnClickPendingIntent(R.id.widget_btn_add_200, getPendingIntent(context, ACTION_ADD_200))
                views.setOnClickPendingIntent(R.id.widget_btn_add_500, getPendingIntent(context, ACTION_ADD_500))

                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun getPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, WaterWidgetProvider::class.java)
        intent.action = action
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
