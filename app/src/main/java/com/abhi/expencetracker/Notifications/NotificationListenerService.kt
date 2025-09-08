package com.abhi.expencetracker.Notifications

import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.abhi.expencetracker.Database.money.Money
import com.abhi.expencetracker.Database.money.TransactionType
import com.abhi.expencetracker.MainApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SmsNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val extras = sbn.notification.extras

        val messageBody = buildString {
            val singleLine = extras.getCharSequence("android.text")?.toString()
            if (!singleLine.isNullOrEmpty()) append(singleLine)

            val lines = extras.getCharSequenceArray("android.textLines")
            lines?.forEach { append(" ").append(it.toString()) }
        }.trim()

        Log.d("SmsNotificationListener", "Complete Notification Body: $messageBody")

        if (!messageBody.contains("debited", true) &&
            !messageBody.contains("credited", true) &&
            !messageBody.contains("UPI", true)) return

        val dao = MainApplication.moneyDatabase.getMoneyDao()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Extract amount
                val amountRegex = Regex("""(?:INR|Rs\.?)\s?([\d,]+\.?\d{0,2})""", RegexOption.IGNORE_CASE)
                val match = amountRegex.find(messageBody)
                val amountStr = match?.groups?.get(1)?.value?.replace(",", "") ?: return@launch
                val amount = amountStr.toDoubleOrNull() ?: return@launch

                // Transaction type
                val type = when {
                    messageBody.contains("debited", true) -> TransactionType.EXPENSE
                    messageBody.contains("credited", true) -> TransactionType.INCOME
                    else -> TransactionType.TRANSFER
                }

                // Extract UPI Ref No
                val upiRegex = Regex("""(?:UPI:?|UPI Ref(?:erence)? no\.?)\s*[:#]?\s*([0-9A-Za-z]{6,})""", RegexOption.IGNORE_CASE)
                val upiRefNo = upiRegex.find(messageBody)?.groups?.get(1)?.value

                // Date extraction
                val dateRegex = Regex("""\b(\d{2}-\d{2}-\d{2})\b""")
                val date = dateRegex.find(messageBody)?.groups?.get(1)?.value?.let {
                    val sdfInput = SimpleDateFormat("dd-MM-yy", Locale.getDefault())
                    val sdfOutput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    sdfInput.parse(it)?.let { dateObj -> sdfOutput.format(dateObj) }
                } ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

                // Fallback Unique ID when UPI Ref No is missing
                val uniqueId = upiRefNo ?: (messageBody + amount.toString() + date).hashCode().toString()

                // ---------------- DUPLICATE CHECK ----------------
                val exists = dao.existsByUpiRefNo(uniqueId)
                if (exists > 0) {
                    Log.d("SmsNotificationListener", "Duplicate transaction skipped: $uniqueId")
                    return@launch
                }

                // Extract Name
                val name = when (type) {
                    TransactionType.EXPENSE -> {
                        Regex("""trf to ([A-Z\s]+)""", RegexOption.IGNORE_CASE)
                            .find(messageBody)?.groups?.get(1)?.value?.trim()
                    }
                    TransactionType.INCOME -> {
                        Regex("""from ([A-Z\s]+)""", RegexOption.IGNORE_CASE)
                            .find(messageBody)?.groups?.get(1)?.value?.trim()
                    }
                    else -> {
                        Regex("""transfer (?:to|from) ([A-Z\s]+)""", RegexOption.IGNORE_CASE)
                            .find(messageBody)?.groups?.get(1)?.value?.trim()
                    }
                } ?: "Unknown"

                // Extract Bank Name
                var bankName: String? = null
                val words = messageBody.split(" ", "\n", "\t")
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

                // Save transaction
                val money = Money(
                    id = 0,
                    amount = amount,
                    description = name,
                    type = type,
                    date = date,
                    upiRefNo = uniqueId,
                    bankName = bankName
                )

                dao.addMoney(money)

                // Show Notification after a delay
                val delayMillis = 6_000L
                Handler(Looper.getMainLooper()).postDelayed({
                    NotificationHelper.showTransactionNotification(applicationContext, money)
                }, delayMillis)

                Log.d("SmsNotificationListener", "Saved Transaction: $money")
            } catch (e: Exception) {
                Log.e("SmsNotificationListener", "Error parsing notification: ${e.message}")
            }
        }
    }
}
