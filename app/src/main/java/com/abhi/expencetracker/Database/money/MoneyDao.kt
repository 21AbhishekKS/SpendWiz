package com.abhi.expencetracker.Database.money

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert

@Dao
interface MoneyDao {

    @Query("select * from MONEY order by date asc")
    fun getAllMoney() : LiveData<List<Money>>

    @Query("DELETE FROM money WHERE id =  :id")
    fun deleteMoney(id : Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMoney(money: Money)

    //function to get today's transaction
    @Query("SELECT * from money where date = :date")
    fun getTodayTransactionByDate(date: String) : LiveData<List<Money>>


    @Query("SELECT * FROM money WHERE substr(date, 4, 2) = :month AND substr(date, 7, 4) = :year")
    fun getTransactionsByMonthAndYear(month: String, year: String): LiveData<List<Money>>


    @Query("SELECT * FROM money WHERE substr(date, 4, 7) LIKE :date")
    fun getTransactionsForMonth(date: String): LiveData<List<Money>>

    @Query("SELECT COUNT(*) FROM money WHERE upiRefNo = :upiRefNo")
    suspend fun existsByUpiRefNo(upiRefNo: String): Int



}






