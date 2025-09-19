package com.spendwiz.app.Notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class DismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val manager = NotificationManagerCompat.from(context!!)
        manager.cancel(1001)
    }
}
