package com.spendwiz.app

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Room
import com.jakewharton.threetenabp.AndroidThreeTen
import com.spendwiz.app.Database.money.MoneyDatabase

class MainApplication : Application() {

    companion object {
        lateinit var moneyDatabase: MoneyDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        //To get time and date for below 26 API to remove required O annotation
        AndroidThreeTen.init(this)

        moneyDatabase = MoneyDatabase.getDatabase(this)
    }
}