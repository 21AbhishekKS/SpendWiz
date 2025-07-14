package com.abhi.expencetracker.Database.money.ViewModels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhi.expencetracker.MainApplication
import com.abhi.expencetracker.Database.money.Money
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.content.Context
import android.net.Uri
import android.provider.Telephony
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class AddScreenViewModel : ViewModel() {

    val moneyDao = MainApplication.moneyDatabase.getMoneyDao()

    val moneyList : LiveData<List<Money>> = moneyDao.getAllMoney()


    @RequiresApi(Build.VERSION_CODES.O)
    val today = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    val customFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    @RequiresApi(Build.VERSION_CODES.O)
    val formattedDateCustom = today.format(customFormatter)


    @RequiresApi(Build.VERSION_CODES.O)
    val todayMoneyList : LiveData<List<Money>> = moneyDao.getTodayTransactionByDate(formattedDateCustom)



    @RequiresApi(Build.VERSION_CODES.O)
    fun addMoney(
        id: Int,
        amount: String,
        description: String,
        type: String,)
        //date : Date,)
    {
        viewModelScope.launch(Dispatchers.IO) {
            moneyDao.addMoney(Money(
                id =  id,
                amount = amount ,
                discription = description ,
                type = type ,
               // date = "05/05/2024"))
               date = formattedDateCustom.toString()))



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
                        // Extract amount
                        val amountRegex = Regex("""(?:INR|Rs\.?)\s?([\d,]+\.?\d{0,2})""", RegexOption.IGNORE_CASE)
                        val match = amountRegex.find(body)
                        val amountStr = match?.groups?.get(1)?.value?.replace(",", "") ?: continue
                        val amount = amountStr.toDoubleOrNull() ?: continue

                        // Determine type
                        val type = when {
                            body.contains("debited", true) -> "Spent"
                            body.contains("credited", true) -> "Received"
                            else -> continue
                        }

                        // Extract name
                        val name = when {
                            type == "Spent" -> {
                                val regex = Regex("""trf to ([A-Z\s]+)""", RegexOption.IGNORE_CASE)
                                regex.find(body)?.groups?.get(1)?.value?.trim()
                            }
                            type == "Received" -> {
                                val regex = Regex("""from ([A-Z\s]+)""", RegexOption.IGNORE_CASE)
                                regex.find(body)?.groups?.get(1)?.value?.trim()
                            }
                            else -> null
                        } ?: "Unknown"

                        // Build user-friendly description
                        val description = if (type == "Spent") {
                            "Paid to $name"
                        } else {
                            "Received from $name"
                        }

                        // Format date
                        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(Date(timestamp))

                        val money = Money(
                            id = 0,
                            amount = amount.toString(),
                            discription = description,
                            type = type,
                            date = date
                        )

                        // Save to DB
                        moneyDao.addMoney(money)
                    }
                }
            }
        }
    }










    @RequiresApi(Build.VERSION_CODES.N)
        fun deleteMoney(id : Int) {
         viewModelScope.launch(Dispatchers.IO) {
             moneyDao.deleteMoney(id = id)

         }


     }
}