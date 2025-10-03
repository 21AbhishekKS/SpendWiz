package com.spendwiz.app.voiceAssistant.ExternalAssistant

import com.spendwiz.app.Database.money.TransactionType

// Voice command model
sealed class ParsedCommand {

    // ✅ Add transactions
    data class AddTransaction(
        val amount: Double,
        val type: TransactionType,
        val description: String,
        val category: String? = null,
        val date: String? = null,
        val time: String? = null
    ) : ParsedCommand()

    // ✅ Delete / Edit
    object DeleteLastTransaction : ParsedCommand()
    data class UpdateLastTransactionAmount(val newAmount: Double) : ParsedCommand()
    data class UpdateLastTransactionCategory(val newCategory: String) : ParsedCommand()
    data class RenameLastTransaction(val newName: String) : ParsedCommand()
    data class MoveLastTransactionCategory(val newCategory: String) : ParsedCommand()
    object DeleteTodayTransactions : ParsedCommand()

    // ✅ Queries / summaries
    data class QueryDailyTotal(val type: TransactionType) : ParsedCommand()
    data class QueryMonthlySummary(val month: String, val year: String) : ParsedCommand()
    data class QueryCategoryBreakdown(val month: String, val year: String) : ParsedCommand()
    data class QueryCategorySpending(
        val category: String,
        val timeRange: String
    ) : ParsedCommand()
    data class QueryBiggestExpense(val month: String, val year: String) : ParsedCommand()
    data class QueryYearlySpending(val year: String) : ParsedCommand()
    data class QueryMonthlySavings(val month: String, val year: String) : ParsedCommand()
    object QueryFastestGrowingCategory : ParsedCommand()

    // ✅ Navigation commands
    data class NavigateToScreen(val route: String) : ParsedCommand()


    // ✅ Backup & Import
    object BackupData : ParsedCommand()
    object ImportFromSMS : ParsedCommand()

    object Unrecognized : ParsedCommand()
}
