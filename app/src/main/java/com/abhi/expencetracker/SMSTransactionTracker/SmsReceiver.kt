package com.abhi.expencetracker.SMSTransactionTracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.annotation.RequiresApi
import com.abhi.expencetracker.Database.money.TransactionType
import com.abhi.expencetracker.Database.money.Money
import com.abhi.expencetracker.MainApplication
import com.abhi.expencetracker.Notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SmsReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {
            val bundle = intent.extras ?: return
            val pdus = bundle["pdus"] as? Array<*>
            pdus?.forEach {
                val sms = Telephony.Sms.Intents.getMessagesFromIntent(intent)[0]
                val body = sms.messageBody ?: return

                // same regex parsing as in your ViewModel
                if (body.contains("debited", true) || body.contains("credited", true) || body.contains("UPI", true)) {
                    val amountRegex = Regex("""(?:INR|Rs\.?)\s?([\d,]+\.?\d{0,2})""", RegexOption.IGNORE_CASE)
                    val match = amountRegex.find(body)
                    val amountStr = match?.groups?.get(1)?.value?.replace(",", "") ?: return
                    val amount = amountStr.toDoubleOrNull() ?: return

                    val upiRefRegex = Regex("""(?:UPI:?|UPI Ref no)\s*[:#]?\s*(\d{6,})""", RegexOption.IGNORE_CASE)
                    val upiMatch = upiRefRegex.find(body)
                    val upiRefNo = upiMatch?.groups?.get(1)?.value ?: return

                    val dao = MainApplication.moneyDatabase.getMoneyDao()

                    CoroutineScope(Dispatchers.IO).launch {
                        // skip duplicates
                        if (dao.existsByUpiRefNo(upiRefNo) > 0) return@launch

                        val type = when {
                            body.contains("debited", true) -> TransactionType.EXPENSE
                            body.contains("credited", true) -> TransactionType.INCOME
                            else -> TransactionType.TRANSFER
                        }

                        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(Date(sms.timestampMillis))

                        val money = Money(
                            id = 0,
                            amount = amount,
                            description = "Auto SMS Entry",
                            type = type,
                            date = date,
                            upiRefNo = upiRefNo,
                            bankName = "Unknown Bank"
                        )

                        dao.addMoney(money)
                        NotificationHelper.showTransactionNotification(context, money)
                    }
                }
            }
        }
    }
}
