package com.abhi.expencetracker.Notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.abhi.expencetracker.MainActivity
import com.abhi.expencetracker.R
import com.abhi.expencetracker.Database.money.Money

object NotificationHelper {
    fun showTransactionNotification(context: Context, money: Money) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "transaction_channel")
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("New ${money.type.name.lowercase().replaceFirstChar { it.uppercase() }} added")
            .setContentText("${money.bankName}: ₹${money.amount} on ${money.date}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            manager.notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}
