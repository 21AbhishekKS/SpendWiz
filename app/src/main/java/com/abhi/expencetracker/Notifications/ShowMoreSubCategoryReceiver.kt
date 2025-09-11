package com.abhi.expencetracker.Notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.abhi.expencetracker.MainApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShowMoreSubCategoryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val transactionId = intent.getIntExtra("transaction_id", -1)
        val chosenCategory = intent.getStringExtra("category")
        val page = intent.getIntExtra("page", 0)

        if (transactionId > 0 && !chosenCategory.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = MainApplication.moneyDatabase.getMoneyDao()
                val money = dao.getTransactionById(transactionId)
                money?.let {
                    withContext(Dispatchers.Main) {
                        SubCategoryNotificationHelper.showSubCategoryNotification(
                            context,
                            it,
                            chosenCategory,
                            page
                        )
                    }
                }
            }
        }
    }
}
