package com.abhi.expencetracker.ViewModels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhi.expencetracker.Database.money.MainApplication
import com.abhi.expencetracker.Database.money.Money
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Date

class AddScreenViewModel : ViewModel() {

    val moneyDao = MainApplication.moneyDatabase.getMoneyDao()

    val moneyList : LiveData<List<Money>> = moneyDao.getAllMoney()



    @RequiresApi(Build.VERSION_CODES.O)
    fun addMoney(
        amount: String,
        description: String,
        type: String)
    {
        viewModelScope.launch(Dispatchers.IO) {
            moneyDao.addMoney(Money(amount = amount ,
                discription = description ,
                type = type ,
                date = Date.from(Instant.now()))
            )
        }


        //this function is called so that it show list in UI


    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun deleteMoney(id : Int){
        viewModelScope.launch(Dispatchers.IO) {
        moneyDao.deleteMoney(id = id)

    }
}}