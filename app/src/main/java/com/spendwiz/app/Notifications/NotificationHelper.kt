package com.spendwiz.app.Notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.spendwiz.app.Database.money.Money
import com.spendwiz.app.Database.money.MoneyDatabase
import com.spendwiz.app.Database.money.TransactionType
import com.spendwiz.app.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object NotificationHelper {

    private const val KEY_CATEGORY_REPLY = "transaction_category"
    private const val PAGE_SIZE = 3
    private const val CHANNEL_ID = "transaction_channel"

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Transactions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Transaction notifications"
            }
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showTransactionNotification(
        context: Context,
        money: Money,
        page: Int = 0
    ) {
        createChannel(context)

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

        // Remote input
        val remoteInput = RemoteInput.Builder(KEY_CATEGORY_REPLY)
            .setLabel("Choose category")
            .setChoices(currentPageCategories.toTypedArray()) // some OEMs may ignore this
            .build()

        // Correct flags for all Android versions
        val replyFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val replyIntent = Intent(context, CategoryReceiver::class.java).apply {
            putExtra("transaction_id", money.id)
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            money.id,
            replyIntent,
            replyFlags
        )

        // ✅ Allow generated replies explicitly
        val selectAction = NotificationCompat.Action.Builder(
            R.drawable.notification_icon,
            "Select Category",
            replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true) // critical for some OEMs
            .build()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("New ${money.type.name.lowercase().replaceFirstChar { it.uppercase() }}")
            .setContentText("₹${money.amount} on ${money.date}")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Heads-up
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
                replyFlags
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
                replyFlags
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
}
