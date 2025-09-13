package com.abhi.expencetracker

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.abhi.expencetracker.Database.money.MoneyDatabase
import com.abhi.expencetracker.Notifications.DailyNotificationWorker
import com.abhi.expencetracker.Notifications.NotificationReceiver
import com.abhi.expencetracker.Notifications.PreferencesManager
import com.abhi.expencetracker.ViewModels.AddScreenViewModel
import com.abhi.expencetracker.ViewModels.CategoryViewModel
import com.abhi.expencetracker.ViewModels.CategoryViewModelFactory
import com.abhi.expencetracker.navigation.BottomNav
import com.abhi.expencetracker.ui.theme.ExpenceTrackerTheme
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var moneyViewModel: AddScreenViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var prefs: PreferencesManager

    private fun isNotificationServiceEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(packageName) == true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //To blend status bar with app
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (!isNotificationServiceEnabled()) {
            showNotificationAccessDialog()
        }

        installSplashScreen()
        moneyViewModel = ViewModelProvider(this)[AddScreenViewModel::class.java]
        val database = MoneyDatabase.getDatabase(this)
        val dao = database.getCategoryDao()
        val factory = CategoryViewModelFactory(dao)
        categoryViewModel = ViewModelProvider(this, factory)[CategoryViewModel::class.java]
        prefs = PreferencesManager(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        createNotificationChannels()

        setContent {
            ExpenceTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    val navController = rememberNavController()

                    BottomNav(
                        navController,
                        moneyViewModel,
                        categoryViewModel,
                        prefs = prefs,
                        onDailyToggle = { enabled, hour, minute ->
                            if (enabled) {
                                scheduleDailyNotification(hour, minute)
                            } else {
                                cancelDailyNotification()
                            }
                        },
                        onTransactionToggle = { enabled ->
                            lifecycleScope.launch {
                                prefs.setTransactionNotification(enabled)
                            }
                        }
                    )

                }
            }
        }
    }

    private fun showNotificationAccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable Notification Access")
            .setMessage(
                "To automatically detect transactions from SMS, the app needs access to notifications. " +
                        "You will be redirected to the settings to enable this permission."
            )
            .setPositiveButton("Go to Settings") { _: DialogInterface, _: Int ->
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val dailyChannel = NotificationChannel(
                "daily_channel",
                "Daily Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders about categorizing daily transactions"
            }

            val transactionChannel = NotificationChannel(
                "transaction_channel",
                "Transaction Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Instant alerts for detected SMS transactions"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(dailyChannel)
            manager.createNotificationChannel(transactionChannel)
        }
    }

    fun scheduleDailyNotification(hour: Int, minute: Int) {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // If time already passed today, schedule for tomorrow
        if (targetTime.before(currentTime)) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        val delay = targetTime.timeInMillis - currentTime.timeInMillis

        val dailyWork = PeriodicWorkRequestBuilder<DailyNotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_notification",
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyWork
        )
    }

    fun cancelDailyNotification() {
        WorkManager.getInstance(this).cancelUniqueWork("daily_notification")
    }


}
