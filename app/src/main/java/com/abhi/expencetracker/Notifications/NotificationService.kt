package com.abhi.expencetracker.Notifications

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.abhi.expencetracker.MainActivity
import com.abhi.expencetracker.R
import java.util.Calendar
import kotlin.random.Random

class NotificationService(
    private val context: Context,
    val notificationDescription: String
) {

    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @RequiresApi(Build.VERSION_CODES.N)
    fun showDailyNotification() {

        val intent = Intent(context, MainActivity::class.java) // Explicit intent to MainActivity

        // Flags for a new task and clearing the previous task (consider alternatives if needed)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "Daily_Remainder")
            .setContentTitle("Expense Tracker")
            .setContentText(notificationDescription)
            .setSmallIcon(R.drawable.add_transation_notification)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Set the PendingIntent for notification click
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_launcher_foreground,
                    "Add More",
                    pendingIntent
                ).build()
            )
            .build()

        notificationManager.notify(
            Random.nextInt(),
            notification
        )
    }

    fun setAlarm(context: Context) {

        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val alarmPendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent,
            PendingIntent.FLAG_IMMUTABLE)



        //passing intent of spent and earned money

          //  val alarmIntent = Intent(context, AlarmReceiver::class.java)
          //  alarmIntent.putExtra("notificationDescription", notificationDescription)
          //  val alarmPendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent,
         //       PendingIntent.FLAG_IMMUTABLE)



        // Set the alarm to start at 10:04 PM
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 18)
        }

        // If the alarm time has already passed, set it to the next day
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Set the alarm to repeat daily
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            alarmPendingIntent
        )

    }
}