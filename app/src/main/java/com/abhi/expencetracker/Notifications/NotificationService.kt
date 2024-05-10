package com.abhi.expencetracker.Notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.abhi.expencetracker.MainActivity
import com.abhi.expencetracker.R
import kotlin.random.Random

class NotificationService(
    private val context: Context,
    val notificationDescription: String
) {

    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    @RequiresApi(Build.VERSION_CODES.N)
    fun showDailyNotification() {

        val intent = Intent(context, MainActivity::class.java) // Explicit intent to MainActivity

        // Flags for a new task and clearing the previous task (consider alternatives if needed)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_MUTABLE
        )

        val notification = NotificationCompat.Builder(context, "Daily_Remainder")
            .setContentTitle("Expense Tracker")
            .setContentText(notificationDescription)
            .setSmallIcon(R.drawable.add_transation_notification)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Set the PendingIntent for notification click
            .build()

        notificationManager.notify(
            Random.nextInt(),
            notification
        )
    }
}
