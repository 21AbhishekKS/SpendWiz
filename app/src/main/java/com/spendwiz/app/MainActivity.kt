package com.spendwiz.app

import AppUpdateChecker
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.gms.ads.MobileAds
import com.spendwiz.app.Ads.InterstitialAdManager
import com.spendwiz.app.Database.money.MoneyDatabase
import com.spendwiz.app.Notifications.DailyNotificationWorker
import com.spendwiz.app.Notifications.PreferencesManager
import com.spendwiz.app.ViewModels.AddScreenViewModel
import com.spendwiz.app.ViewModels.CategoryViewModel
import com.spendwiz.app.ViewModels.CategoryViewModelFactory
import com.spendwiz.app.navigation.BottomNav
import com.spendwiz.app.ui.theme.ExpenceTrackerTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var moneyViewModel: AddScreenViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var prefs: PreferencesManager

    // All required permissions
    private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.RECEIVE_SMS,
            android.Manifest.permission.READ_SMS
        )
    } else {
        arrayOf(
            android.Manifest.permission.RECEIVE_SMS,
            android.Manifest.permission.READ_SMS
        )
    }

    // Launcher for multiple permissions
    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val smsGranted = permissions[android.Manifest.permission.READ_SMS] == true
        val receiveSmsGranted = permissions[android.Manifest.permission.RECEIVE_SMS] == true

        if (smsGranted && receiveSmsGranted) {
            moneyViewModel.runSmsImportOnce(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //To blend status bar with app
        WindowCompat.setDecorFitsSystemWindows(window, false)
        installSplashScreen()
        moneyViewModel = ViewModelProvider(this)[AddScreenViewModel::class.java]
        val database = MoneyDatabase.getDatabase(this)
        val dao = database.getCategoryDao()
        val factory = CategoryViewModelFactory(dao)
        categoryViewModel = ViewModelProvider(this, factory)[CategoryViewModel::class.java]
        prefs = PreferencesManager(this)

        requestAllPermissions()
        createNotificationChannels()
        // Schedule notification automatically on the app's first launch
        scheduleNotificationOnFirstLaunch()

        //Ads sdk AdMob - Initialize and pre-load the first ad.
        MobileAds.initialize(this) {
            InterstitialAdManager.loadAd(this)
        }
        setContent {
            ExpenceTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {

                    //Check in app updates
                    AppUpdateChecker()
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

    // Ask all required permissions
    private fun requestAllPermissions() {
        val notGranted = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            // Show Prominent Disclosure BEFORE asking system permission
            AlertDialog.Builder(this)
                .setTitle("SMS Permission Required")
                .setMessage(
                    "This app needs access to your SMS messages to automatically detect " +
                            "bank and UPI transaction alerts. These are used only to track your " +
                            "expenses and show insights like graphs and reports. Personal messages " +
                            "are not read, stored, or shared. SMS data never leaves your device."
                )
                .setPositiveButton("Allow") { _, _ ->
                    permissionsLauncher.launch(REQUIRED_PERMISSIONS)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false) // Prevent closing without choosing
                .show()
        } else {
            // ✅ Permissions already granted
            moneyViewModel.runSmsImportOnce(this)
        }
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

        // Create a Data object to pass the time to the worker
        val workData = workDataOf(
            "hour" to hour,
            "minute" to minute
        )

        // Use OneTimeWorkRequestBuilder instead of PeriodicWorkRequestBuilder
        val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workData) // Pass the time data
            .build()

        // Use enqueueUniqueWork with ExistingWorkPolicy.REPLACE
        WorkManager.getInstance(this).enqueueUniqueWork(
            "daily_notification",
            ExistingWorkPolicy.REPLACE,
            dailyWorkRequest
        )
    }
    fun cancelDailyNotification() {
        WorkManager.getInstance(this).cancelUniqueWork("daily_notification")
    }
    private fun scheduleNotificationOnFirstLaunch() {
        lifecycleScope.launch {
            // Assumes 'isFirstLaunchFlow' exists in your PreferencesManager.
            // We use .first() to get the current value from the Flow.
            if (prefs.isFirstLaunchFlow.first()) {
                // 1. Schedule notification with the default time (22:05)
                scheduleDailyNotification(22, 5)

                // 2. Explicitly save the notification's "enabled" state
                prefs.setDailyNotification(true)

                // 3. Update the flag so this block never runs again
                // Assumes 'setFirstLaunchCompleted' exists in your PreferencesManager.
                prefs.setFirstLaunchCompleted()
            }
        }
    }

}
