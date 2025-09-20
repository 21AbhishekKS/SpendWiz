package com.spendwiz.app.SMSTransactionTracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.util.Log
import androidx.annotation.RequiresApi
import com.spendwiz.app.Database.money.Money
import com.spendwiz.app.Database.money.TransactionType
import com.spendwiz.app.MainApplication
import com.spendwiz.app.Notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SmsReceiver : BroadcastReceiver() {
    @RequiresApi(26)
    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION != intent.action) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val dao = MainApplication.moneyDatabase.getMoneyDao()

        Handler(Looper.getMainLooper()).postDelayed({
            messages.forEach { sms ->
                val body = sms.messageBody ?: return@forEach
                val timestamp = sms.timestampMillis

                val parsed = SmsTransactionParser.parse(body, timestamp) ?: return@forEach

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // fallback for duplicate check
                        val upiRefForCheck = parsed.money.upiRefNo ?: "TXN_${timestamp}"
                        if (dao.existsByUpiRefNo(upiRefForCheck) > 0) return@launch

                        val rowId = dao.addMoney(parsed.money)
                        if (rowId != -1L) {
                            val insertedMoney = parsed.money.copy(id = rowId.toInt())
                            NotificationHelper.showTransactionNotification(context, insertedMoney)
                        }
                    } catch (e: Exception) {
                        Log.e("SmsReceiver", "Error inserting SMS transaction", e)
                    }
                }
            }
        }, 6000)
    }
}
