package com.abhi.expencetracker.Database.money

import android.app.Application
import androidx.room.Database
import androidx.room.Room

class MainApplication : Application() {

    companion object{
        lateinit var moneyDatabase : MoneyDatabase
    }

    override fun onCreate() {
        super.onCreate()
        moneyDatabase = Room.databaseBuilder(
            applicationContext,
            MoneyDatabase::class.java,
            MoneyDatabase.NAME
        ).build()
    }
}