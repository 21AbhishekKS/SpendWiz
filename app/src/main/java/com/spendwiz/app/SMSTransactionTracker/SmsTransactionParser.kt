package com.spendwiz.app.SMSTransactionTracker

import com.spendwiz.app.Database.money.Money
import com.spendwiz.app.Database.money.TransactionType
import java.text.SimpleDateFormat
import java.util.*


object SmsTransactionParser {

    // A list of regexes to find the transaction amount in various formats
    private val amountRegexes = listOf(
        Regex("""(?:rs|inr|₹)\.?\s*:?\s*([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""amount\s*(?:of)?\s*(?:inr|rs|₹)?\s*([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""(?:debited|credited)\s*by\s*(?:inr|rs|₹)?\s*([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
    )

    // A list of regexes to find reference numbers like UPI, UTR, etc.
    private val upiRefRegexes = listOf(
        Regex("""(?:UPI|IMPS)\s*(?:Ref|RRN|Ref\s*No)\.?\s*[:#-]?\s*(\w{10,})""", RegexOption.IGNORE_CASE),
        Regex("""Ref\s*no?\.?\s*[:#-]?\s*(\w{10,})""", RegexOption.IGNORE_CASE),
        Regex("""UTR\s*(?:Ref\.?)?\s*[:.\s]\s*(\w{10,})""", RegexOption.IGNORE_CASE),
        Regex("""\((?:UPI(?:\s*Ref\s*no)?)\s*(\w{10,})\)""", RegexOption.IGNORE_CASE)
    )

    // An ordered list of patterns to extract the payee/payer name or transaction description.
    private val nameExtractionPatterns = listOf(
        // Specific pattern for multi-line 'Sent...To...' messages (e.g., HDFC)
        Regex("""Sent\s+.*?To\s+([\w\s.@'-]+?)(?=\s*On|\n|Ref)""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)),
        // Pattern for transactions with "Info:" description (e.g., ICICI)
        Regex("""Info:\s*([\w\s./-]+?)(?=\s*Avl\s*Bal|$)""", RegexOption.IGNORE_CASE),
        // Pattern for "purchase at MERCHANT"
        Regex("""(?:purchase|spent)\s+at\s+([\w\s.&'-]+?)(?=\s*(?:on|with|making|avl)|$)""", RegexOption.IGNORE_CASE),
        // General pattern for 'to payee', 'paid to payee'
        Regex("""(?:to|paid\s*to)\s+(?!your\s+a/c|a/c)([\w\s.@'-]+?)(?=\s*,?\s*(?:on|at|for|via|ref|from|avl\s*bal|UPI|if|\n)|$)""", RegexOption.IGNORE_CASE),
        // Specific pattern for SBI NEFT description `NA PRINCIPAL JNNCE`
        Regex("""BATCHID:\d+\s+([\w\s]+?)\s+NA-""", RegexOption.IGNORE_CASE),
        // General pattern for 'from sender', 'by sender', 'from VPA', 'from beneficiary'
        Regex("""(?:from(?:\s+beneficiary|\s+VPA)?|by)\s+(?!your\s+a/c|a/c)([\w\s.@()'-]+?)(?=\s*,?\s*(?:on|at|for|via|ref|from|with\s+UTR|INFO:|avl\s*bal|\(|\n)|$)""", RegexOption.IGNORE_CASE),
        // Pattern for mandate creation messages (e.g., Spotify)
        Regex("""(?:mandate\s+on|on)\s+([\w\s.&-]+?)(?=\s*for|starting\s*from|\n|$)""", RegexOption.IGNORE_CASE),
        // Specific pattern for IOB debit description (e.g., SHIVAMOGGA)
        Regex("""\[Dr\.for POR\]\s*([\w\s]+?)\s*on""", RegexOption.IGNORE_CASE),
        // Specific pattern for descriptions like 'towards interest'
        Regex("""towards\s+([\w\s]+?)(?=\.|\n|$)""", RegexOption.IGNORE_CASE),
        // Specific pattern for cash deposit descriptions
        Regex("""(-?\s*(?:Deposit of Cash|Cash Deposit))""", RegexOption.IGNORE_CASE)
    )

    //  regex to find  bank names
    private val bankRegex = Regex(
        """\b(State\s*Bank\s*of\s*India|Karnataka\s*Gramin\s*Bank|KarnatakaBank|Union\s*Bank\s*of\s*India|Canara\s*Bank|Kotak(?:\s*Mahindra)?\s*Bank|HDFC\s*Bank|Indian\s*Overseas\s*Bank|ICICI\s*Bank|Axis\s*Bank|Punjab\s*National\s*Bank|Yes\s*Bank|IndusInd\s*Bank|IDBI\s*Bank|SBI|IOB|BOB|PNB)\b""",
        RegexOption.IGNORE_CASE
    )


    fun parse(body: String, timestamp: Long): ParsedTransaction? {
        if (!body.contains("debited", ignoreCase = true)
            && !body.contains("credited", ignoreCase = true)
            && !body.contains("upi", ignoreCase = true)
            && !body.contains("paid", ignoreCase = true)
            && !body.contains("sent", ignoreCase = true)
            && !body.contains("received", ignoreCase = true)
            && !body.contains("dr.", ignoreCase = true)
            && !body.contains("mandate", ignoreCase = true)
        ) return null

        val amountMatch = amountRegexes.firstNotNullOfOrNull { it.find(body) }
        val amount = amountMatch?.groups?.get(1)?.value
            ?.replace(",", "")
            ?.toDoubleOrNull() ?: return null

        val upiRef = upiRefRegexes.firstNotNullOfOrNull { it.find(body)?.groups?.get(1)?.value }

        val type = when {
            body.contains("debited", ignoreCase = true) ||
                    body.contains("dr.", ignoreCase = true) ||
                    body.contains("paid", ignoreCase = true) ||
                    body.contains("sent", ignoreCase = true) ||
                    body.contains("purchase", ignoreCase = true) ||
                    body.contains("mandate", ignoreCase = true) -> TransactionType.EXPENSE

            body.contains("credited", ignoreCase = true) ||
                    body.contains("cr.", ignoreCase = true) ||
                    body.contains("received", ignoreCase = true) -> TransactionType.INCOME

            else -> TransactionType.TRANSFER
        }

        val rawName = nameExtractionPatterns
            .firstNotNullOfOrNull { it.find(body)?.groups?.get(1)?.value }
            ?.trim()

        // **UPDATED:** Name fallback logic. If a name isn't found, use the amount as a description.
        val name = if (rawName.isNullOrBlank() ||
            rawName.contains("your account", ignoreCase = true) ||
            rawName.contains("your a/c", ignoreCase = true) ||
            rawName.contains("XXXX", ignoreCase = true)
        ) {
            // Create a fallback description like "Spent Rs. 50.00" or "Received Rs. 100.00"
            val typeString = if (type == TransactionType.EXPENSE) "Spent" else "Received"
            "$typeString Rs.${"%.2f".format(amount)}"
        } else {
            // Clean up the successfully extracted name
            rawName
                .replace("""\s+""".toRegex(), " ")
                .replace("""\s*[-.]+$""".toRegex(), "")
                .trim()
        }

        val bankName = bankRegex.find(body)?.groups?.get(1)?.value?.trim() ?: "Unknown Bank"

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
                upiRefNo = upiRef,
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