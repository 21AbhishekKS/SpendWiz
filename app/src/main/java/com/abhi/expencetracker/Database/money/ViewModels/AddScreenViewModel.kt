package com.abhi.expencetracker.Database.money.ViewModels

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun addMoney(
        id: Int = 0,
        amount: Double,
        description: String,
        type: TransactionType
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val money = Money(
                id = id,
                amount = amount,
                description = description,
                type = type,
                date = formattedDateCustom
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
                        } ?: "Unknown"

                        val description = when (type) {
                            TransactionType.EXPENSE -> "Paid to $name"
                            TransactionType.INCOME -> "Received from $name"
                        }

                        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(Date(timestamp))

                        val money = Money(
                            id = 0,
                            amount = amount,
                            description = description,
                            type = type,
                            date = date,
                            upiRefNo = upiRefNo
                        )

                        moneyDao.addMoney(money)
                    }
                }
            }
        }
    }


    fun deleteMoney(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            moneyDao.deleteMoney(id)
        }
    }
}
