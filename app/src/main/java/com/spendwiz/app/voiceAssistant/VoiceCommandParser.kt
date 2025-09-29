package com.spendwiz.app.voiceAssistant

import com.spendwiz.app.Database.money.TransactionType
import java.util.Locale

object VoiceCommandParser {

    // --- Add Transactions ---
    private val addWithCategoryRegex =
        "(?i)(?:add|new|spent)\\s+₹?(\\d+(?:\\.\\d+)?)\\s+(expense|income)?\\s*(?:for|on)?\\s*(.+?)\\s+(?:in|under|to)\\s+(.+?)(?:\\s+category)?(?:\\s+on\\s+([0-9]{1,2}\\s+\\w+))?(?:\\s+at\\s+([0-9]{1,2}(:[0-9]{2})?\\s*(am|pm)?))?"
            .toRegex()

    private val addSimpleRegex =
        "(?i)(?:add|spent)\\s+₹?(\\d+(?:\\.\\d+)?)\\s+(expense|income)?\\s*(?:for|on)?\\s*(.+)"
            .toRegex()

    // --- Delete / Edit ---
    private val deleteLastRegex =
        "(?i)(?:delete|remove|cancel)(?:\\s+the|\\s+my)?\\s+last\\s+(?:transaction|entry)".toRegex()

    private val updateLastAmountRegex =
        "(?i)update\\s+last\\s+transaction\\s+amount\\s+to\\s+₹?(\\d+(?:\\.\\d+)?)".toRegex()

    private val changeLastCategoryRegex =
        "(?i)change\\s+category\\s+of\\s+last\\s+(?:expense|transaction)\\s+to\\s+(.+)".toRegex()

    private val renameLastRegex =
        "(?i)rename\\s+['\"]?(.+?)['\"]?\\s+expense\\s+to\\s+['\"]?(.+?)['\"]?".toRegex()

    private val moveLastCategoryRegex =
        "(?i)move\\s+last\\s+transaction\\s+to\\s+['\"]?(.+?)['\"]?\\s+category".toRegex()

    private val deleteTodayRegex =
        "(?i)delete\\s+all\\s+transactions\\s+from\\s+today".toRegex()

    // --- Queries ---
    private val queryTodayTotalRegex =
        "(?i)(?:what('| i)?s|show|get)(?:\\s+my)?\\s+(?:total\\s+)?(expense|income|spending)\\s+(?:for\\s+)?today".toRegex()

    private val monthlySummaryRegex =
        "(?i)show\\s+my\\s+(?:expenses|summary)\\s+for\\s+(\\w+)".toRegex()

    private val categoryBreakdownRegex =
        "(?i)show\\s+category\\s+breakdown\\s+for\\s+(\\w+)".toRegex()

    private val categoryTimeBasedRegex =
        "(?i)how\\s+much\\s+did\\s+i\\s+spend\\s+on\\s+(.+?)\\s+(last\\s+week|this\\s+month|last\\s+month|today)".toRegex()

    private val biggestExpenseRegex =
        "(?i)what('| i)?s\\s+my\\s+biggest\\s+expense\\s+(?:this|for\\s+this)\\s+(month)".toRegex()

    private val yearlySpendingRegex =
        "(?i)show\\s+my\\s+yearly\\s+spending\\s+for\\s+(\\d{4})".toRegex()

    private val savingsThisMonthRegex =
        "(?i)what('| i)?s\\s+my\\s+saving(s)?\\s+(?:this|for\\s+this)\\s+month".toRegex()

    private val fastestGrowingRegex =
        "(?i)which\\s+category\\s+is\\s+growing\\s+fastest".toRegex()

    // --- Navigation ---
    private val openAddExpenseRegex =
        "(?i)open\\s+(add|new)\\s+expense\\s+screen".toRegex()

    private val switchInsightsRegex =
        "(?i)switch\\s+to\\s+insights\\s+view".toRegex()

    // --- Backup / Import ---
    private val backupRegex = "(?i)backup\\s+my\\s+data\\s+now".toRegex()
    private val importSMSRegex = "(?i)import\\s+transactions\\s+from\\s+sms".toRegex()


    fun parse(command: String): ParsedCommand {
        val text = command.trim()

        // 1️⃣ Add Transaction — With category/date/time
        addWithCategoryRegex.find(text)?.let {
            val amount = it.groupValues[1].toDouble()
            val type = parseType(it.groupValues[2])
            val description = it.groupValues[3].trim()
            val category = it.groupValues[4].trim()
            val date = it.groupValues.getOrNull(5)?.trim()?.ifEmpty { null }
            val time = it.groupValues.getOrNull(6)?.trim()?.ifEmpty { null }
            return ParsedCommand.AddTransaction(amount, type ?: TransactionType.EXPENSE, description, category, date, time)
        }

        // 2️⃣ Add Transaction — Simple
        addSimpleRegex.find(text)?.let {
            val amount = it.groupValues[1].toDouble()
            val type = parseType(it.groupValues[2])
            val description = it.groupValues[3].trim()
            return ParsedCommand.AddTransaction(amount, type ?: TransactionType.EXPENSE, description)
        }

        // 🧽 Delete / Edit
        deleteLastRegex.find(text)?.let { return ParsedCommand.DeleteLastTransaction }
        updateLastAmountRegex.find(text)?.let { return ParsedCommand.UpdateLastTransactionAmount(it.groupValues[1].toDouble()) }
        changeLastCategoryRegex.find(text)?.let { return ParsedCommand.UpdateLastTransactionCategory(it.groupValues[1].trim()) }
        renameLastRegex.find(text)?.let { return ParsedCommand.RenameLastTransaction(it.groupValues[2].trim()) }
        moveLastCategoryRegex.find(text)?.let { return ParsedCommand.MoveLastTransactionCategory(it.groupValues[1].trim()) }
        deleteTodayRegex.find(text)?.let { return ParsedCommand.DeleteTodayTransactions }

        // 📊 Queries
        queryTodayTotalRegex.find(text)?.let {
            val type = parseType(it.groupValues[2]) ?: TransactionType.EXPENSE
            return ParsedCommand.QueryDailyTotal(type)
        }

        monthlySummaryRegex.find(text)?.let {
            return ParsedCommand.QueryMonthlySummary(it.groupValues[1], getCurrentYear())
        }

        categoryBreakdownRegex.find(text)?.let {
            return ParsedCommand.QueryCategoryBreakdown(it.groupValues[1], getCurrentYear())
        }

        categoryTimeBasedRegex.find(text)?.let {
            return ParsedCommand.QueryCategorySpending(it.groupValues[1], it.groupValues[2])
        }

        biggestExpenseRegex.find(text)?.let {
            return ParsedCommand.QueryBiggestExpense(getCurrentMonth(), getCurrentYear())
        }

        yearlySpendingRegex.find(text)?.let {
            return ParsedCommand.QueryYearlySpending(it.groupValues[1])
        }

        savingsThisMonthRegex.find(text)?.let {
            return ParsedCommand.QueryMonthlySavings(getCurrentMonth(), getCurrentYear())
        }

        fastestGrowingRegex.find(text)?.let { return ParsedCommand.QueryFastestGrowingCategory }

        // 🧭 Navigation
        openAddExpenseRegex.find(text)?.let { return ParsedCommand.OpenAddExpenseScreen }
        switchInsightsRegex.find(text)?.let { return ParsedCommand.SwitchToInsightsView }

        // 💾 Backup / Import
        backupRegex.find(text)?.let { return ParsedCommand.BackupData }
        importSMSRegex.find(text)?.let { return ParsedCommand.ImportFromSMS }

        return ParsedCommand.Unrecognized
    }

    private fun parseType(typeString: String?): TransactionType? {
        if (typeString == null) return null
        return when (typeString.lowercase(Locale.getDefault())) {
            "expense", "spending" -> TransactionType.EXPENSE
            "income" -> TransactionType.INCOME
            else -> null
        }
    }

    private fun getCurrentMonth(): String {
        val cal = java.util.Calendar.getInstance()
        return String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1)
    }

    private fun getCurrentYear(): String {
        return java.util.Calendar.getInstance().get(java.util.Calendar.YEAR).toString()
    }
}
