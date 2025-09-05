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
            val dao = MainApplication.moneyDatabase.getMoneyDao()

            pdus?.forEach {
                val sms = Telephony.Sms.Intents.getMessagesFromIntent(intent)[0]
                val body = sms.messageBody ?: return

                if (body.contains("debited", true) || body.contains("credited", true) || body.contains("UPI", true)) {
                    val amountRegex = Regex("""(?:INR|Rs\.?)\s?([\d,]+\.?\d{0,2})""", RegexOption.IGNORE_CASE)
                    val match = amountRegex.find(body)
                    val amountStr = match?.groups?.get(1)?.value?.replace(",", "") ?: return
                    val amount = amountStr.toDoubleOrNull() ?: return

                    val upiRefRegex = Regex("""(?:UPI:?|UPI Ref no)\s*[:#]?\s*(\d{6,})""", RegexOption.IGNORE_CASE)
                    val upiMatch = upiRefRegex.find(body)
                    val upiRefNo = upiMatch?.groups?.get(1)?.value ?: return

                    CoroutineScope(Dispatchers.IO).launch {
                        if (dao.existsByUpiRefNo(upiRefNo) > 0) return@launch

                        val type = when {
                            body.contains("debited", true) -> TransactionType.EXPENSE
                            body.contains("credited", true) -> TransactionType.INCOME
                            else -> TransactionType.TRANSFER
                        }

                        // ---------------- NAME EXTRACTION ----------------
                        val name = when (type) {
                            TransactionType.EXPENSE -> {
                                val regex = Regex("""trf to ([A-Z\s]+)""", RegexOption.IGNORE_CASE)
                                regex.find(body)?.groups?.get(1)?.value?.trim()
                            }
                            TransactionType.INCOME -> {
                                val regex = Regex("""from ([A-Z\s]+)""", RegexOption.IGNORE_CASE)
                                regex.find(body)?.groups?.get(1)?.value?.trim()
                            }
                            TransactionType.TRANSFER -> {
                                val regex = Regex("""transfer (?:to|from) ([A-Z\s]+)""", RegexOption.IGNORE_CASE)
                                regex.find(body)?.groups?.get(1)?.value?.trim()
                            }
                        } ?: "Unknown"

                        // ---------------- BANK NAME EXTRACTION ----------------
                        var bankName: String? = null
                        val words = body.split(" ", "\n", "\t")

                        for (i in words.indices) {
                            val word = words[i]
                            if (word.equals("bank", ignoreCase = true) && i > 0) {
                                bankName = words[i - 1].replace("[^A-Za-z]".toRegex(), "") + " Bank"
                                break
                            }
                            if (word.lowercase().endsWith("bank")) {
                                bankName = word.replace("[^A-Za-z]".toRegex(), "")
                                    .replaceFirstChar { it.uppercase() }
                                break
                            }
                        }
                        if (bankName.isNullOrBlank()) bankName = "Unknown Bank"
                        // ------------------------------------------------------

                        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(Date(sms.timestampMillis))

                        val money = Money(
                            id = 0,
                            amount = amount,
                            description = name,
                            type = type,
                            date = date,
                            upiRefNo = upiRefNo,
                            bankName = bankName
                        )

                        dao.addMoney(money)
                        NotificationHelper.showTransactionNotification(context, money)
                    }
                }
            }
        }
    }
}
