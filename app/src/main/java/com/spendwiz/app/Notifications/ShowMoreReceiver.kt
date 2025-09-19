package com.spendwiz.app.Notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.spendwiz.app.MainApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShowMoreReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val transactionId = intent.getIntExtra("transaction_id", -1)
        val page = intent.getIntExtra("page", 0)

        if (transactionId > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = MainApplication.moneyDatabase.getMoneyDao()
                val money = dao.getTransactionById(transactionId)
                money?.let {
                    withContext(Dispatchers.Main) {
                        NotificationHelper.showTransactionNotification(context, it, page)
                    }
                }
            }
        }
    }
}
