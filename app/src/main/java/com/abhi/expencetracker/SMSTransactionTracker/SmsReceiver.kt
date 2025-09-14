package com.abhi.expencetracker.SMSTransactionTracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.annotation.RequiresApi
import com.abhi.expencetracker.Database.money.Money
import com.abhi.expencetracker.Database.money.TransactionType
import com.abhi.expencetracker.MainApplication
import com.abhi.expencetracker.Notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SmsReceiver : BroadcastReceiver() {
    private val TAG = "SmsReceiver"

    @RequiresApi(26)
    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION != intent.action) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) {
            Log.w(TAG, "No messages in intent")
            return
        }

        val dao = MainApplication.moneyDatabase.getMoneyDao()

        // Process each SmsMessage
        messages.forEach { sms ->
            try {
                val body = sms.messageBody ?: return@forEach
                val timestamp = sms.timestampMillis
                Log.d(TAG, "Received SMS body: $body")

                // quick filter
                if (!body.contains("debited", true)
                    && !body.contains("credited", true)
                    && !body.contains("UPI", true)
                ) {
                    Log.d(TAG, "SMS skipped, not transaction-like.")
                    return@forEach
                }

                // amount: accept ₹, Rs, INR and numbers with commas and optional decimals
                val amountRegex = Regex("""(?:₹|INR|Rs\.?)\s*([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
                val match = amountRegex.find(body)
                val amountStr = match?.groups?.get(1)?.value?.replace(",", "") ?: run {
                    Log.w(TAG, "Amount not found/couldn't parse in: $body")
                    return@forEach
                }
                val amount = amountStr.toDoubleOrNull() ?: run {
                    Log.w(TAG, "Amount parse failed: $amountStr")
                    return@forEach
                }

                // Try a couple UPI ref patterns (be liberal)
                val upiRefRegex1 = Regex("""UPI(?: Ref(?:\.?| No)?)?\s*[:#-]?\s*(\w{4,})""", RegexOption.IGNORE_CASE)
                val upiRefRegex2 = Regex("""Ref(?: No\.?| No|:)\s*(\w{4,})""", RegexOption.IGNORE_CASE)
                val upiRef = upiRefRegex1.find(body)?.groups?.get(1)?.value
                    ?: upiRefRegex2.find(body)?.groups?.get(1)?.value

                // if we don't get a UPI ref, try to use txn id keywords or fallback to timestamp-based id
                val upiRefNo = upiRef?.takeIf { it.isNotBlank() } ?: "TXN_${timestamp}"

                // check duplicate by upiRefNo
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val exists = dao.existsByUpiRefNo(upiRefNo)
                        if (exists > 0) {
                            Log.i(TAG, "Duplicate SMS (upiRefNo=$upiRefNo) skipped.")
                            return@launch
                        }

                        val type = when {
                            body.contains("debited", true) -> TransactionType.EXPENSE
                            body.contains("credited", true) -> TransactionType.INCOME
                            else -> TransactionType.TRANSFER
                        }

                        val name = when (type) {
                            TransactionType.EXPENSE -> Regex("""trf to ([A-Z\s0-9&\.-]+)""", RegexOption.IGNORE_CASE)
                                .find(body)?.groups?.get(1)?.value?.trim()
                            TransactionType.INCOME -> Regex("""from ([A-Z\s0-9&\.-]+)""", RegexOption.IGNORE_CASE)
                                .find(body)?.groups?.get(1)?.value?.trim()
                            else -> null
                        } ?: "Unknown"

                        // Bank extraction (simple)
                        var bankName: String? = null
                        val words = body.split("\\s+".toRegex())
                        for (i in words.indices) {
                            val word = words[i].replace("[,.:]$".toRegex(), "")
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

                        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(Date(timestamp))

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
                        Log.i(TAG, "Inserted money from SMS: $money")
                        NotificationHelper.showTransactionNotification(context, money)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error while inserting SMS transaction", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing SMS message", e)
            }
        }
    }
}
