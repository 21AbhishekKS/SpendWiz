package com.abhi.expencetracker.ViewModels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.abhi.expencetracker.Database.money.Money

class AddScreenViewModel : ViewModel() {

    val moneyList : LiveData<List<Money>> = _MoneyList

    fun getAllMoney()  {
        _MoneyList.value = Manager.getAllMoney()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addMoney(
        amount: String,
        description: String,
        type: String)
    {
        Manager.addMoney(
             amount , description , type
        )

        //this function is called so that it show list in UI
        getAllMoney()

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun deleteMoney(id : Int){
        Manager.deleteMoney(id)

        getAllMoney()
    }
}