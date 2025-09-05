package com.abhi.expencetracker.Notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.abhi.expencetracker.Database.money.MoneyDatabase
import com.abhi.expencetracker.MainActivity
import com.abhi.expencetracker.MainApplication
import com.abhi.expencetracker.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        val dao = MainApplication.moneyDatabase.getMoneyDao()


        CoroutineScope(Dispatchers.IO).launch {
            // Format today's date the same way you store it in DB
            val today = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

            val todayList = dao.getTodayTransactionsRaw(today)

            val title: String
            val message: String
            if (todayList.isEmpty()) {
                title = "No Transactions Today"
                message = "No transactions were detected today. If you're missing one, you can add it manually"
            } else {
                title = "Transactions Found"
                message = "You have transactions today. Please categorize them for proper analysis"
            }

            val notificationIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
            )

            val dismissIntent = Intent(context, DismissReceiver::class.java)
            val dismissPendingIntent = PendingIntent.getBroadcast(
                context, 0, dismissIntent, PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, "daily_channel")
                .setSmallIcon(R.drawable.notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_foreground))
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_menu_view, "Open App", pendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
                .build()

            val manager = NotificationManagerCompat.from(context)
            if (ActivityCompat.checkSelfPermission(
                    context, android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                manager.notify(1001, notification)
            }
        }
    }
}
