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
import com.abhi.expencetracker.Database.money.CategoryData.*

object NotificationHelper {

    private const val KEY_CATEGORY_REPLY = "transaction_category"
    private const val PAGE_SIZE = 3 // change as you like

    fun showTransactionNotification(context: Context, money: Money, page: Int = 0) {
        val categories = when (money.type) {
            TransactionType.INCOME -> incomeCategories
            TransactionType.EXPENSE -> expenseCategories
            TransactionType.TRANSFER -> transferCategories
        }

        val start = page * PAGE_SIZE
        val end = minOf(start + PAGE_SIZE, categories.size)
        val currentPageCategories = categories.subList(start, end)

        // RemoteInput for current page
        val remoteInput = RemoteInput.Builder(KEY_CATEGORY_REPLY)
            .setLabel("Choose category")
            .setChoices(currentPageCategories.toTypedArray())
            .build()

        // Reply intent (category selection)
        val replyIntent = Intent(context, CategoryReceiver::class.java).apply {
            putExtra("transaction_id", money.id)
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            money.id, // unique requestCode for reply
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val selectAction = NotificationCompat.Action.Builder(
            R.drawable.notification_icon,
            "",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        val builder = NotificationCompat.Builder(context, "transaction_channel")
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("New ${money.type.name.lowercase().replaceFirstChar { it.uppercase() }}")
            .setContentText("₹${money.amount} on ${money.date}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .addAction(selectAction)

        // If there are more pages, show "Show More" -> page+1
        if (end < categories.size) {
            val moreIntent = Intent(context, ShowMoreReceiver::class.java).apply {
                putExtra("transaction_id", money.id)
                putExtra("page", page + 1)
            }

            // use a requestCode that depends on page to avoid reuse surprises
            val morePendingIntent = PendingIntent.getBroadcast(
                context,
                money.id + 1000 + page,
                moreIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            builder.addAction(R.drawable.notification_icon, "Show More", morePendingIntent)

            // If this is the last page AND there was more than one page total, show "Start Over"
        } else if (categories.size > PAGE_SIZE) {
            val restartIntent = Intent(context, ShowMoreReceiver::class.java).apply {
                putExtra("transaction_id", money.id)
                putExtra("page", 0) // restart from beginning
            }

            val restartPendingIntent = PendingIntent.getBroadcast(
                context,
                money.id + 2000, // different requestCode for restart
                restartIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            builder.addAction(R.drawable.notification_icon, "Start Over", restartPendingIntent)
        }

        val notification = builder.build()

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
