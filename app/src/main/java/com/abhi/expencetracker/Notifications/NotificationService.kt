package com.abhi.expencetracker.Notifications

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.abhi.expencetracker.R
import kotlin.random.Random

class NotificationService(
    private val context: Context ,
    val notificationDescription :String
) {

   private val notificationManager = context.getSystemService(NotificationManager::class.java)

    @RequiresApi(Build.VERSION_CODES.N)
    fun showDailyNotification(){


        val notification = NotificationCompat.Builder(context, "Daily_Remainder" //channel created in MainApplication not in MainActivity
        ).setContentTitle("Expense Tracker")
            .setContentText(notificationDescription) //Description of notification
            .setSmallIcon(R.drawable.add_transation_notification)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setAutoCancel(true)
            .build()


        notificationManager.notify(
            Random.nextInt(),
            notification
        )

    }
}