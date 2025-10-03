package com.spendwiz.app.voiceAssistant.ExternalAssistant

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.spendwiz.app.Database.money.Money
import com.spendwiz.app.MainActivity
import com.spendwiz.app.MainApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object VoiceCommandHandler {

    private val commandScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @RequiresApi(Build.VERSION_CODES.O)
    fun processCommand(context: Context, command: String) {
        val moneyDao = MainApplication.moneyDatabase.getMoneyDao()
        val todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MM"))
        val currentYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"))

        when (val parsedResult = VoiceCommandParser.parse(command)) {

            is ParsedCommand.AddTransaction -> {
                commandScope.launch {
                    val newTransaction = Money(
                        amount = parsedResult.amount,
                        description = parsedResult.description,
                        type = parsedResult.type,
                        date = todayDate,
                        time = parsedResult.time ?: "",
                        category = parsedResult.category ?: "Others",
                        subCategory = null
                    )
                    val rowId = moneyDao.addMoney(newTransaction)
                    withContext(Dispatchers.Main) {
                        if (rowId != -1L) {
                            Toast.makeText(context, "✅ Added: ${parsedResult.description}", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "⚠️ Failed to add transaction.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

            is ParsedCommand.NavigateToScreen -> {
                val intent = Intent(context, MainActivity::class.java).apply {
                    putExtra("NAVIGATE_TO", parsedResult.route)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(intent)
                Toast.makeText(context, "Navigating to ${parsedResult.route}", Toast.LENGTH_SHORT).show()
            }

            is ParsedCommand.DeleteLastTransaction -> {
                commandScope.launch {
                    val lastTransaction = moneyDao.getLastTransaction()
                    if (lastTransaction != null) {
                        moneyDao.deleteMoney(lastTransaction)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "✅ Last transaction deleted.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "⚠️ No transactions to delete.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

            is ParsedCommand.UpdateLastTransactionAmount -> {
                commandScope.launch {
                    moneyDao.updateLastTransactionAmount(parsedResult.newAmount)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "✅ Last transaction amount updated to ${parsedResult.newAmount}", Toast.LENGTH_LONG).show()
                    }
                }
            }

            is ParsedCommand.UpdateLastTransactionCategory -> {
                commandScope.launch {
                    moneyDao.updateLastTransactionCategory(parsedResult.newCategory)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "✅ Last transaction category updated to ${parsedResult.newCategory}", Toast.LENGTH_LONG).show()
                    }
                }
            }

            is ParsedCommand.DeleteTodayTransactions -> {
                commandScope.launch {
                    moneyDao.deleteTransactionsByDate(todayDate)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "✅ All transactions from today have been deleted.", Toast.LENGTH_LONG).show()
                    }
                }
            }

            is ParsedCommand.QueryDailyTotal -> {
                commandScope.launch {
                    val total = moneyDao.getTotalForDate(todayDate, parsedResult.type.name) ?: 0.0
                    val formattedTotal = DecimalFormat("#,##0.00").format(total)
                    val typeString = parsedResult.type.name.lowercase()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Total $typeString for today: ₹$formattedTotal", Toast.LENGTH_LONG).show()
                    }
                }
            }

            is ParsedCommand.QueryMonthlySummary -> {
                commandScope.launch {
                    val summary = moneyDao.getMonthlySummary(currentMonth, currentYear)
                    val message = if (summary != null) {
                        "Summary for this month:\nIncome: ₹${summary.totalIncome}\nExpense: ₹${summary.totalExpense}"
                    } else {
                        "No data found for this month."
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            is ParsedCommand.QueryBiggestExpense -> {
                commandScope.launch {
                    val expense = moneyDao.getBiggestExpenseForMonth(currentMonth, currentYear)
                    val message = if (expense != null) {
                        "Biggest expense this month was ₹${expense.amount} for '${expense.description}'."
                    } else {
                        "No expenses recorded this month."
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            is ParsedCommand.ImportFromSMS -> {
                Toast.makeText(context, "SMS Import feature not yet implemented via voice.", Toast.LENGTH_LONG).show()
            }

            is ParsedCommand.Unrecognized -> {
                Toast.makeText(context, "Couldn't understand: \"$command\"", Toast.LENGTH_LONG).show()
            }

            else -> {
                Toast.makeText(context, "This voice command is not implemented yet.", Toast.LENGTH_LONG).show()
            }
        }
    }
}