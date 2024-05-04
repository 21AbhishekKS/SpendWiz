package com.abhi.expencetracker.Database.money

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MoneyDao {

    @Query("select * from MONEY")
    fun getAllMoney() : LiveData<List<Money>>

    @Query("DELETE FROM money WHERE id =  :id")
    fun deleteMoney(id : Int)

    @Insert
    fun addMoney(money: Money)


}