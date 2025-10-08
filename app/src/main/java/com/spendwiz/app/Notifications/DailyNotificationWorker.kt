package com.spendwiz.app.Notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.spendwiz.app.R
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailyNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        showNotification()
        reschedule() // Add this call to schedule the next notification
        return Result.success()
    }

    private fun reschedule() {
        // Get the hour and minute from the input data
        val hour = inputData.getInt("hour", 22) // Default to 22:00 if not found
        val minute = inputData.getInt("minute", 5)  // Default to 05 if not found

        val targetTime = Calendar.getInstance().apply {
            // Set the time from input data
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            // Since this worker just ran, schedule for the next day
            add(Calendar.DAY_OF_MONTH, 1)
        }

        val delay = targetTime.timeInMillis - System.currentTimeMillis()

        // Pass the same time data to the next scheduled worker
        val workData = workDataOf(
            "hour" to hour,
            "minute" to minute
        )

        val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workData)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "daily_notification",
            ExistingWorkPolicy.REPLACE, // This policy is fine here
            dailyWorkRequest
        )
    }

    private fun showNotification() {
        val channelId = "daily_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Spendwiz")
            .setContentText("Quickly add and categorize any missing transactions to complete your daily record")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(1001, notification)
        }
    }
}