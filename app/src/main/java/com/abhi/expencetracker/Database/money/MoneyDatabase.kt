package com.abhi.expencetracker.Database.money

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Money::class], version = 1)
@TypeConverters(Converters::class)
abstract class MoneyDatabase : RoomDatabase() {

    companion object {
        const val NAME = "Money_DB"
    }

    abstract fun getMoneyDao(): MoneyDao
}
