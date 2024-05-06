package com.abhi.expencetracker.Database.money

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MoneyDao {

    @Query("select * from MONEY")
    fun getAllMoney() : LiveData<List<Money>>

    @Query("DELETE FROM money WHERE id =  :id")
    fun deleteMoney(id : Int)

    @Insert
    fun addMoney(money: Money)

    //function to get today's transaction

    @Query("SELECT * from money where date = :date")
    fun getTodayTransaction(date: String) : LiveData<List<Money>>

}