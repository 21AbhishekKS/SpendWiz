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








     @RequiresApi(Build.VERSION_CODES.N)
        fun deleteMoney(id : Int) {
         viewModelScope.launch(Dispatchers.IO) {
             moneyDao.deleteMoney(id = id)

         }


     }
}