package com.spendwiz.app

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Room
import com.spendwiz.app.Database.money.MoneyDatabase

class MainApplication : Application() {

    companion object {
        lateinit var moneyDatabase: MoneyDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        moneyDatabase = MoneyDatabase.getDatabase(this)
    }
}