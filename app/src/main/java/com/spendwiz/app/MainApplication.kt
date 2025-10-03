package com.spendwiz.app

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Room
import com.spendwiz.app.Database.money.MoneyDatabase

class MainApplication : Application() {

    // A companion object holds properties that are static to the class.
    companion object {
        lateinit var moneyDatabase: MoneyDatabase
            private set // 'private set' means it can only be assigned a value within this file.
    }

    override fun onCreate() {
        super.onCreate()
        moneyDatabase = MoneyDatabase.getDatabase(this)
    }
}