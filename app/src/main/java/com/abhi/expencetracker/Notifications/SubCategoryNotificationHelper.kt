package com.abhi.expencetracker.Notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.abhi.expencetracker.Database.money.Money
import com.abhi.expencetracker.Database.money.MoneyDatabase
import com.abhi.expencetracker.Database.money.TransactionType
import com.abhi.expencetracker.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.math.min

object SubCategoryNotificationHelper {

    private const val KEY_SUBCATEGORY_REPLY = "transaction_subcategory"
    private const val PAGE_SIZE = 3

    fun showSubCategoryNotification(
        context: Context,
        money: Money,
        chosenCategory: String,
        page: Int = 0
    ) {
        val database = MoneyDatabase.getDatabase(context)
        val categoryDao = database.getCategoryDao()

        // Fetch subcategories from DB synchronously
        val subCategories: List<String> = runBlocking {
            // Get categoryId first
            val category = categoryDao.getCategoriesByTypeOnce(
                when (money.type) {
                    TransactionType.INCOME -> "Income"
                    TransactionType.EXPENSE -> "Expense"
                    TransactionType.TRANSFER -> "Transfer"
                }
            ).firstOrNull { it.name == chosenCategory }

            // Fetch subcategories using categoryId
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
            .setChoices(currentPageSubCategories.toTypedArray())
            .build()

        val replyIntent = Intent(context, SubCategoryReceiver::class.java).apply {
            putExtra("transaction_id", money.id)
            putExtra("category", chosenCategory)
        }

        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            money.id + 5000,
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
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
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
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            builder.addAction(R.drawable.notification_icon, "Start Over", restartPendingIntent)
        }

        NotificationManagerCompat.from(context).notify(money.id, builder.build())
    }
}
