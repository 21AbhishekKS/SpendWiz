package com.abhi.expencetracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Room
import com.abhi.expencetracker.Database.money.MoneyDatabase

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

        val notificationChannel = NotificationChannel(
            "Daily_Remainder",
            "Daily Remainder",
            NotificationManager.IMPORTANCE_HIGH
        )

        notificationChannel.description = "Channel to implement Daily Remainder Notifications"

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(notificationChannel)

        //NotificationService(this, "Notification description").setAlaram(this)


























    }
}