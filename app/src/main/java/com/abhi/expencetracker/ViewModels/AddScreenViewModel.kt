package com.abhi.expencetracker.ViewModels

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.aay.compose.donutChart.model.PieChartData
import com.abhi.expencetracker.Database.money.CategoryExpense
import com.abhi.expencetracker.MainApplication
import com.abhi.expencetracker.Database.money.Money
import com.abhi.expencetracker.Database.money.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class AddScreenViewModel : ViewModel() {

    val moneyDao = MainApplication.moneyDatabase.getMoneyDao()

    val moneyList: LiveData<List<Money>> = moneyDao.getAllMoney()

    @RequiresApi(Build.VERSION_CODES.O)
    private val today = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    private val customFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    @RequiresApi(Build.VERSION_CODES.O)
    private val formattedDateCustom = today.format(customFormatter)

    @RequiresApi(Build.VERSION_CODES.O)
    val todayMoneyList: LiveData<List<Money>> = moneyDao.getTodayTransactionByDate(formattedDateCustom)

    fun updateMoney(
        id: Int,
        amount: Double,
        description: String,
        type: TransactionType,
        category: String,
        subCategory: String,
        date: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        val existing = moneyDao.getMoneyById(id) ?: return@launch

        val updated = existing.copy(
            amount = amount,
            description = description,
            type = type,
            category = category,
            subCategory = subCategory,
            date = date
        )
        moneyDao.updateMoney(updated)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun addMoney1(
        id: Int = 0,
        amount: Double,
        description: String,
        type: TransactionType,
        category: String = "Others",
        subCategory: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val money = Money(
                id = id,
                amount = amount,
                description = description,
                type = type,
                date = formattedDateCustom,
                category = category,
                subCategory = subCategory
            )
            moneyDao.addMoney(money)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertTransactionsFromSms(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val uri = Uri.parse("content://sms/inbox")
            val cursor = context.contentResolver.query(
                uri,
                null,
                null,
                null,
                "date DESC"
            )

            cursor?.use {
                val bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = cursor.getColumnIndex(Telephony.Sms.DATE)

                while (cursor.moveToNext()) {
                    val body = cursor.getString(bodyIndex)
                    val timestamp = cursor.getLong(dateIndex)

                    if (body.contains("debited", true) || body.contains("credited", true) || body.contains("UPI", true)) {
                        val amountRegex = Regex("""(?:INR|Rs\.?)\s?([\d,]+\.?\d{0,2})""", RegexOption.IGNORE_CASE)
                        val match = amountRegex.find(body)
                        val amountStr = match?.groups?.get(1)?.value?.replace(",", "") ?: continue
                        val amount = amountStr.toDoubleOrNull() ?: continue

                        val upiRefRegex = Regex("""(?:UPI:?|UPI Ref no)\s*[:#]?\s*(\d{6,})""", RegexOption.IGNORE_CASE)
                        val upiMatch = upiRefRegex.find(body)
                        val upiRefNo = upiMatch?.groups?.get(1)?.value ?: continue

                        // Check if UPI Ref No already exists
                        val exists = moneyDao.existsByUpiRefNo(upiRefNo)
                        if (exists > 0) continue  // Duplicate, skip

                        val type = when {
                            body.contains("debited", true) -> TransactionType.EXPENSE
                            body.contains("credited", true) -> TransactionType.INCOME
                            else -> continue
                        }

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

                        val description = when (type) {
                            TransactionType.EXPENSE -> "Paid to $name"
                            TransactionType.INCOME -> "Received from $name"
                            TransactionType.TRANSFER -> "Transferred with $name"
                        }


                        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(Date(timestamp))

                        // ---------------- BANK NAME EXTRACTION ----------------
                        var bankName: String? = null
                        val words = body.split(" ", "\n", "\t")

                        for (i in words.indices) {
                            val word = words[i]

                            // Case 1: word is exactly "bank" → take previous word + " Bank"
                            if (word.equals("bank", ignoreCase = true) && i > 0) {
                                bankName = words[i - 1].replace("[^A-Za-z]".toRegex(), "") + " Bank"
                                break
                            }

                            // Case 2: word ends with "bank"
                            if (word.lowercase().endsWith("bank")) {
                                bankName = word.replace("[^A-Za-z]".toRegex(), "")
                                    .replaceFirstChar { it.uppercase() }
                                break
                            }
                        }

                        // Fallback if not found
                        if (bankName.isNullOrBlank()) bankName = "Unknown Bank"

                        // ------------------------------------------------------

                        val money = Money(
                            id = 0,
                            amount = amount,
                            description = description,
                            type = type,
                            date = date,
                            upiRefNo = upiRefNo,
                            bankName = bankName   // <-- added field
                        )

                        moneyDao.addMoney(money)
                    }
                }
            }
        }
    }

    fun getCategoryExpensesForMonth(month: String, year: String): LiveData<List<PieChartData>> {
        return moneyDao.getCategoryExpensesByMonthAndYear(month, year).map { categoryList ->
            mapCategoryExpensesToPieChartData(categoryList)
        }
    }

    private fun mapCategoryExpensesToPieChartData(expenses: List<CategoryExpense>): List<PieChartData> {
        val colors = listOf(
            Color(0xFF8B0000), // Very Dark Red (Maroon)
            Color(0xFFC62828), // Dark Red
            Color(0xFF6A1B1A), // Red-Brown
            Color(0xFFFFCDD2), // Light Red
            Color(0xFF4E342E), // Coffee Brown
            Color(0xFFEF9A9A), // Soft Red
            Color(0xFF3E2723), // Dark Coffee
            Color(0xFFE57373), // Medium Red
            Color(0xFF5D4037),  // Rich Brown
            Color(0xFFB71C1C)  // Deep Red
        )





        return expenses.mapIndexed { index, item ->
            PieChartData(
                partName = item.category+": "+item.total,
                data = item.total,
                color = colors[index % colors.size]
            )
        }
    }


    fun deleteMoney(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            moneyDao.deleteMoney(id)
        }
    }
}
