package com.abhi.expencetracker.Notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.abhi.expencetracker.MainApplication
import com.abhi.expencetracker.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
class SubCategoryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val results = RemoteInput.getResultsFromIntent(intent)
        val chosenSubCategory = results?.getCharSequence("transaction_subcategory")?.toString()
        val transactionId = intent.getIntExtra("transaction_id", -1)

        if (!chosenSubCategory.isNullOrEmpty() && transactionId > 0) {
            val dao = MainApplication.moneyDatabase.getMoneyDao()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // ✅ Update DB with subcategory
                    dao.updateSubCategory(id = transactionId, subCategory = chosenSubCategory)

                    val money = dao.getTransactionById(transactionId)

                    // ✅ Show final updated notification
                    val updatedNotification = NotificationCompat.Builder(context, "transaction_channel")
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(money?.type?.name ?: "Transaction Updated")
                        .setContentText("Category: ${money?.category}, SubCategory: $chosenSubCategory")
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true)
                        .build()

                    NotificationManagerCompat.from(context).notify(transactionId, updatedNotification)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
