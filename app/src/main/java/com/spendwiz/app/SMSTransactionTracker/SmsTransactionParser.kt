package com.spendwiz.app.SMSTransactionTracker

import android.util.Log
import com.spendwiz.app.Database.money.Money
import com.spendwiz.app.Database.money.TransactionType
import java.text.SimpleDateFormat
import java.util.*

object SmsTransactionParser {

    private val amountRegex =
        Regex("""(?:₹|INR|Rs\.?)\s*([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)

    private val upiRefRegexes = listOf(
        Regex("""(?:UPI(?: Ref(?:\.?| No)?)?)\s*[:#-]?\s*(\w{4,})""", RegexOption.IGNORE_CASE),
        Regex("""Ref(?: No\.?| No|:)\s*(\w{4,})""", RegexOption.IGNORE_CASE)
    )

    fun parse(body: String, timestamp: Long): ParsedTransaction? {
        if (!body.contains("debited", true)
            && !body.contains("credited", true)
            && !body.contains("upi", true)
            && !body.contains("paid", true)
            && !body.contains("sent", true)
        ) return null

        // Amount
        val amountMatch = amountRegex.find(body)
        val amount = amountMatch?.groups?.get(1)?.value
            ?.replace(",", "")
            ?.toDoubleOrNull() ?: return null

        // UPI ref (nullable)
        val upiRef = upiRefRegexes.firstNotNullOfOrNull { it.find(body)?.groups?.get(1)?.value }

        // Transaction type
        val type = when {
            body.contains("debited", true) ||
                    body.contains("paid", true) ||
                    body.contains("sent", true) ||
                    body.contains("purchase", true) -> TransactionType.EXPENSE

            body.contains("credited", true) ||
                    body.contains("received", true) -> TransactionType.INCOME

            else -> TransactionType.TRANSFER
        }

        // Name
        // Name
        val rawName = when (type) {
            TransactionType.EXPENSE ->
                Regex("""to\s+([A-Z0-9.&\-\s]+?)(?=,|\s+(?:on|for|at|via|UPI|XXXX)|$)""", RegexOption.IGNORE_CASE)
                    .find(body)?.groups?.get(1)?.value?.trim()

            TransactionType.INCOME ->
                Regex("""from\s+([A-Z0-9.&\-\s]+?)(?=,|\s+(?:on|for|at|via|UPI|XXXX)|$)""", RegexOption.IGNORE_CASE)
                    .find(body)?.groups?.get(1)?.value?.trim()

            else -> null
        }

        // Filter out useless names like "your account XXXXX61406"
        val name = if (rawName.isNullOrBlank() ||
            rawName.contains("your account", true) ||
            rawName.contains("XXXX", true)
        ) {
            "Enter name manually"
        } else rawName


        // Bank
        val bankName = body.split("\\s+".toRegex()).let { words ->
            var bank: String? = null
            for (i in words.indices) {
                val word = words[i].replace("[,.:]$".toRegex(), "")
                if (word.equals("bank", true) && i > 0) {
                    bank = words[i - 1].replace("[^A-Za-z]".toRegex(), "") + " Bank"
                    break
                }
                if (word.lowercase().endsWith("bank")) {
                    bank = word.replace("[^A-Za-z]".toRegex(), "").replaceFirstChar { it.uppercase() }
                    break
                }
            }
            bank ?: "Unknown Bank"
        }

        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

        return ParsedTransaction(
            money = Money(
                id = 0,
                amount = amount,
                description = name,
                type = type,
                date = date,
                time = time,
                upiRefNo = upiRef, // nullable
                bankName = bankName
            ),
            rawBody = body
        )
    }
}

data class ParsedTransaction(
    val money: Money,
    val rawBody: String
)
