package com.example.base.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.base.util.NotificationHelper

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification()
    }
}
