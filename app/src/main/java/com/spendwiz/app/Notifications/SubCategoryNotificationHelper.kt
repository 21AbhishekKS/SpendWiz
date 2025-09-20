package com.spendwiz.app.Notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.spendwiz.app.Database.money.Money
import com.spendwiz.app.Database.money.MoneyDatabase
import com.spendwiz.app.Database.money.TransactionType
import com.spendwiz.app.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object SubCategoryNotificationHelper {

    private const val KEY_SUBCATEGORY_REPLY = "transaction_subcategory"
    private const val PAGE_SIZE = 3
    private const val CHANNEL_ID = "transaction_channel"

    // ✅ Ensure proper channel with IMPORTANCE_HIGH
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

    fun showSubCategoryNotification(
        context: Context,
        money: Money,
        chosenCategory: String,
        page: Int = 0
    ) {
        createChannel(context)

        val database = MoneyDatabase.getDatabase(context)
        val categoryDao = database.getCategoryDao()

        // Fetch subcategories synchronously
        val subCategories: List<String> = runBlocking {
            val category = categoryDao.getCategoriesByTypeOnce(
                when (money.type) {
                    TransactionType.INCOME -> "Income"
                    TransactionType.EXPENSE -> "Expense"
                    TransactionType.TRANSFER -> "Transfer"
                }
            ).firstOrNull { it.name == chosenCategory }

            if (category != null) {
                categoryDao.getSubCategories(category.id).first().map { it.name }
            } else {
                listOf("Others")
            }
        }

        val start = page * PAGE_SIZE
        val end = minOf(start + PAGE_SIZE, subCategories.size)
        val currentPageSubCategories = subCategories.subList(start, end)

        val remoteInput = RemoteInput.Builder(KEY_SUBCATEGORY_REPLY)
            .setLabel("Choose subcategory")
            .setChoices(currentPageSubCategories.toTypedArray()) // some OEMs ignore choices
            .build()

        // ✅ Correct PendingIntent flags
        val replyFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val replyIntent = Intent(context, SubCategoryReceiver::class.java).apply {
            putExtra("transaction_id", money.id)
            putExtra("category", chosenCategory)
        }

        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            money.id + 5000,
            replyIntent,
            replyFlags
        )

        // ✅ Add setAllowGeneratedReplies(true)
        val selectAction = NotificationCompat.Action.Builder(
            R.drawable.notification_icon,
            "Select Subcategory",
            replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .build()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Select Subcategory")
            .setContentText("Category: $chosenCategory")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .addAction(selectAction)

        // Handle paging
        if (end < subCategories.size) {
            val moreIntent = Intent(context, ShowMoreSubCategoryReceiver::class.java).apply {
                putExtra("transaction_id", money.id)
                putExtra("category", chosenCategory)
                putExtra("page", page + 1)
            }

            val morePendingIntent = PendingIntent.getBroadcast(
                context,
                money.id + 6000 + page,
                moreIntent,
                replyFlags
            )
            builder.addAction(R.drawable.notification_icon, "Show More", morePendingIntent)

        } else if (subCategories.size > PAGE_SIZE) {
            val restartIntent = Intent(context, ShowMoreSubCategoryReceiver::class.java).apply {
                putExtra("transaction_id", money.id)
                putExtra("category", chosenCategory)
                putExtra("page", 0)
            }

            val restartPendingIntent = PendingIntent.getBroadcast(
                context,
                money.id + 7000,
                restartIntent,
                replyFlags
            )
            builder.addAction(R.drawable.notification_icon, "Start Over", restartPendingIntent)
        }

        NotificationManagerCompat.from(context).notify(money.id, builder.build())
    }
}
