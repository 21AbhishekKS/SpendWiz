package com.spendwiz.app

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Room
import com.spendwiz.app.Database.money.MoneyDatabase

class MainApplication : Application() {

    companion object{
        lateinit var moneyDatabase : MoneyDatabase
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {


        super.onCreate()
        moneyDatabase = Room.databaseBuilder(
            applicationContext,
            MoneyDatabase::class.java,
            MoneyDatabase.NAME
        ).build()


    }
}