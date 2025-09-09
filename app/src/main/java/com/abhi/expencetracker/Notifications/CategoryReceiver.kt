package com.abhi.expencetracker.Notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.abhi.expencetracker.MainApplication
import com.abhi.expencetracker.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val results = RemoteInput.getResultsFromIntent(intent)
        val chosenCategory = results?.getCharSequence("transaction_category")?.toString()
        val transactionId = intent.getIntExtra("transaction_id", -1)

        Log.d("CategoryReceiver", "Chosen category = $chosenCategory, TxnId = $transactionId")

        if (!chosenCategory.isNullOrEmpty() && transactionId > 0) {
            val dao = MainApplication.moneyDatabase.getMoneyDao()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // ✅ Update DB with new category
                    dao.updateCategory(id = transactionId, category = chosenCategory)
                    Log.d(
                        "CategoryReceiver",
                        "DB Updated → TxnId=$transactionId, Category=$chosenCategory"
                    )

                    // ✅ Get transaction details for title (make sure DAO has getTransactionById)
                    val money = dao.getTransactionById(transactionId)

                    // ✅ Build updated notification
                    val updatedNotification = NotificationCompat.Builder(context, "transaction_channel")
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(money?.type?.name ?: "Transaction Updated")
                        .setContentText("Categorized as $chosenCategory")
                        .setStyle(
                            NotificationCompat.BigTextStyle()
                                .bigText("Categorized as $chosenCategory")
                                .setSummaryText("✔ Updated")
                        )
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true) // avoid vibration/sound again
                        .build()

                    // ✅ Replace old notification with updated one
                    NotificationManagerCompat.from(context).notify(transactionId, updatedNotification)
                    Log.d("CategoryReceiver", "Notification updated for TxnId=$transactionId")

                } catch (e: Exception) {
                    Log.e("CategoryReceiver", "DB update failed", e)
                }
            }
        } else {
            Log.w("CategoryReceiver", "Invalid category or transactionId")
        }
    }
}
