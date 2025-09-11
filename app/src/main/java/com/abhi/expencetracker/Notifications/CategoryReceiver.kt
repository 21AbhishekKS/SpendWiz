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
import kotlinx.coroutines.withContext

class CategoryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val results = RemoteInput.getResultsFromIntent(intent)
        val chosenCategory = results?.getCharSequence("transaction_category")?.toString()
        val transactionId = intent.getIntExtra("transaction_id", -1)

        if (!chosenCategory.isNullOrEmpty() && transactionId > 0) {
            val dao = MainApplication.moneyDatabase.getMoneyDao()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // ✅ Update DB with selected category
                    dao.updateCategory(id = transactionId, category = chosenCategory)

                    // ✅ Get transaction details
                    val money = dao.getTransactionById(transactionId)

                    // ✅ Show subcategory notification instead of final one
                    money?.let {
                        withContext(Dispatchers.Main) {
                            SubCategoryNotificationHelper.showSubCategoryNotification(
                                context,
                                it,
                                chosenCategory
                            )
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
