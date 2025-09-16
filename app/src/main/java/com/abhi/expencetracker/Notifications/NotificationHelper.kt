package com.abhi.expencetracker.Notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.abhi.expencetracker.Database.money.Money
import com.abhi.expencetracker.Database.money.MoneyDatabase
import com.abhi.expencetracker.Database.money.TransactionType
import com.abhi.expencetracker.MainActivity
import com.abhi.expencetracker.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object NotificationHelper {

    private const val KEY_CATEGORY_REPLY = "transaction_category"
    private const val PAGE_SIZE = 3

    fun showTransactionNotification(
        context: Context,
        money: Money,
        page: Int = 0
    ) {
        // Get database & DAO
        val database = MoneyDatabase.getDatabase(context)
        val categoryDao = database.getCategoryDao()

        // Fetch categories from database synchronously
        val categories: List<String> = runBlocking {
            when (money.type) {
                TransactionType.INCOME -> categoryDao.getCategoriesByType("Income").first().map { it.name }
                TransactionType.EXPENSE -> categoryDao.getCategoriesByType("Expense").first().map { it.name }
                TransactionType.TRANSFER -> categoryDao.getCategoriesByType("Transfer").first().map { it.name }
            }
        }

        // Pagination
        val start = page * PAGE_SIZE
        val end = minOf(start + PAGE_SIZE, categories.size)
        val currentPageCategories = categories.subList(start, end)

        // Remote input for category selection
        val remoteInput = RemoteInput.Builder(KEY_CATEGORY_REPLY)
            .setLabel("Choose category")
            .setChoices(currentPageCategories.toTypedArray())
            .build()

        val replyIntent = Intent(context, CategoryReceiver::class.java).apply {
            putExtra("transaction_id", money.id)
        }

        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            money.id,
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

        // Pagination actions
        if (end < categories.size) {
            val moreIntent = Intent(context, ShowMoreReceiver::class.java).apply {
                putExtra("transaction_id", money.id)
                putExtra("page", page + 1)
            }
            val morePendingIntent = PendingIntent.getBroadcast(
                context,
                money.id + 1000 + page,
                moreIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            builder.addAction(R.drawable.notification_icon, "Show More", morePendingIntent)
        } else if (categories.size > PAGE_SIZE) {
            val restartIntent = Intent(context, ShowMoreReceiver::class.java).apply {
                putExtra("transaction_id", money.id)
                putExtra("page", 0)
            }
            val restartPendingIntent = PendingIntent.getBroadcast(
                context,
                money.id + 2000,
                restartIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            builder.addAction(R.drawable.notification_icon, "Start Over", restartPendingIntent)
        }

        // Show notification
        val manager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            manager.notify(money.id, builder.build())
        }
    }

    fun showCategorizedTransactionNotification(context: Context, money: Money) {
        val channelId = "transaction_channel"
        val channelName = "Transactions"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Intent → open MainActivity with transaction ID
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("transactionId", money.id) // pass ID to edit screen
        }
        val pendingIntent = PendingIntent.getActivity(
            context, money.id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Transaction Categorized")
            .setContentText("₹${money.amount} categorized as ${money.category}/${money.subCategory}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(R.drawable.notification_icon, "Change", pendingIntent)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(money.id, notification)
    }
}
