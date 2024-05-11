package com.abhi.expencetracker.Notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

class AlarmReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.N)
            override fun onReceive(context: Context, intent: Intent) {
                NotificationService(context , " Did you spend today? Edit your entries now.").showDailyNotification()
           }


    //Today's spending tracked! Add more if needed.
    //Did you spend today? Edit your entries now.
    //Wallet feeling thin? Fix your entries with a grin!






  //  @RequiresApi(Build.VERSION_CODES.N)
   // override fun onReceive(context: Context, intent: Intent) {
   //     val notificationDescription = intent.getStringExtra("notificationDescription")
   //     NotificationService(context, notificationDescription!!).showDailyNotification()
  //  }


}

