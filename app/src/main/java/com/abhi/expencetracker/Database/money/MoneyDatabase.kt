package com.abhi.expencetracker.Database.money

import androidx.room.Database
import androidx.room.Entity
import androidx.room.RoomDatabase

@Database(entities = [Money::class], version = 1)
abstract class MoneyDatabase : RoomDatabase() {

    companion object{
        const val NAME = "Money_DB"
    }

    abstract fun getMoneyDao() : MoneyDao

}