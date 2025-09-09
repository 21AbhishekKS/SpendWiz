package com.abhi.expencetracker.Notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.abhi.expencetracker.Database.money.Money
import com.abhi.expencetracker.Database.money.TransactionType
import com.abhi.expencetracker.R

object NotificationHelper {

    private const val KEY_CATEGORY_REPLY = "transaction_category"

    fun showTransactionNotification(context: Context, money: Money) {
        val categories = if (money.type == TransactionType.INCOME) {
            arrayOf("Salary", "Business", "Investments", "Others")
        } else {
            arrayOf("Food", "Transport", "Shopping", "Bills", "Misc", "Others")
        }

        // RemoteInput
        val remoteInput = RemoteInput.Builder(KEY_CATEGORY_REPLY)
            .setLabel("Choose category")
            .setChoices(categories)
            .build()

        // Intent → CategoryReceiver
        val replyIntent = Intent(context, CategoryReceiver::class.java).apply {
            putExtra("transaction_id", money.id)
        }

        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            money.id, // unique requestCode
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val action = NotificationCompat.Action.Builder(
            R.drawable.notification_icon,
            "",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        val notification = NotificationCompat.Builder(context, "transaction_channel")
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("New ${money.type.name.lowercase().replaceFirstChar { it.uppercase() }}")
            .setContentText("₹${money.amount} on ${money.date}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(action)
            .build()

        val manager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            manager.notify(money.id, notification)
        }
    }
}
