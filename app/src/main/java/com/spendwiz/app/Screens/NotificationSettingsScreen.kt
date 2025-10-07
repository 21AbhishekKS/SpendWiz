package com.spendwiz.app.Screens

import android.Manifest // <-- ADDED
import android.app.Activity // <-- ADDED
import android.app.TimePickerDialog
import android.content.Intent // <-- ADDED
import android.content.pm.PackageManager // <-- ADDED
import android.net.Uri // <-- ADDED
import android.os.Build // <-- ADDED
import android.provider.Settings // <-- ADDED
import androidx.activity.compose.rememberLauncherForActivityResult // <-- ADDED
import androidx.activity.result.contract.ActivityResultContracts // <-- ADDED
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat // <-- ADDED
import androidx.core.content.ContextCompat // <-- ADDED
import com.spendwiz.app.Ads.BannerAdView
import com.spendwiz.app.Ads.CommonNativeAd
import com.spendwiz.app.AppStyle.AppColors.customSwitchColors
import com.spendwiz.app.R
import com.spendwiz.app.Notifications.PreferencesManager
import kotlinx.coroutines.launch

@Composable
fun NotificationSettingsScreen(
    prefs: PreferencesManager,
    onDailyToggle: (Boolean, Int, Int) -> Unit,
    onTransactionToggle: (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val dailyEnabled by prefs.dailyNotificationFlow.collectAsState(initial = false)
    val dailyHour by prefs.dailyHourFlow.collectAsState(initial = 22)
    val dailyMinute by prefs.dailyMinuteFlow.collectAsState(initial = 5)
    val transactionEnabled by prefs.transactionNotificationFlow.collectAsState(initial = false)

    // --- ADDED: State for permission dialog and deferred action ---
    var showPermissionSettingsDialog by remember { mutableStateOf(false) }
    var pendingPermissionAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val buttonColor = colorResource(id = R.color.button_color)
    val customSwitchColors = customSwitchColors()

    // --- ADDED: Launcher for Notification Permission Request ---
    val activity = LocalContext.current as Activity
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission was granted, so execute the action that was waiting.
            pendingPermissionAction?.invoke()
        } else {
            // Permission was denied. Check if it was denied permanently.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)) {
                // User has permanently denied the permission. Show the settings dialog.
                showPermissionSettingsDialog = true
            }
        }
        // Clean up the pending action
        pendingPermissionAction = null
    }


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            CommonNativeAd(Modifier ,
                stringResource(id = R.string.ad_unit_id_notification_settings_screen)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = innerPadding.calculateBottomPadding()
                )
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            // --- Header ---
            Text(
                text = "Notification Settings",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // --- Daily Reminder Setting ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily Reminder",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Get a summary of your daily spending.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = dailyEnabled,
                    // --- MODIFIED: onCheckedChange with permission logic ---
                    onCheckedChange = { enabled ->
                        // Add the explicit type here
                        val action: () -> Unit = {
                            scope.launch {
                                prefs.setDailyNotification(enabled)
                                onDailyToggle(enabled, dailyHour, dailyMinute)
                            }
                        }
                        if (enabled) { // Only check permission when turning the switch ON
                            // ... rest of the logic is correct
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    action()
                                } else {
                                    pendingPermissionAction = action
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                action()
                            }
                        } else {
                            action()
                        }
                    }, colors = customSwitchColors
                )
            }

            // --- Animated Time Picker Setting ---
            AnimatedVisibility(
                visible = dailyEnabled,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(
                    animationSpec = tween(
                        300
                    )
                ),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(
                    animationSpec = tween(
                        300
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, hour: Int, minute: Int ->
                                    scope.launch {
                                        prefs.setDailyNotificationTime(hour, minute)
                                        onDailyToggle(true, hour, minute)
                                    }
                                },
                                dailyHour,
                                dailyMinute,
                                true // 24-hour format
                            ).show()
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Reminder Time",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = String.format("%02d:%02d", dailyHour, dailyMinute),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = buttonColor
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // --- Transaction Alerts Setting ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Transaction Alerts",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Receive an alert for every new transaction.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = transactionEnabled,
                    // --- MODIFIED: onCheckedChange with permission logic ---
                    onCheckedChange = { enabled ->
                        // Also add the explicit type here
                        val action: () -> Unit = {
                            scope.launch {
                                prefs.setTransactionNotification(enabled)
                                onTransactionToggle(enabled)
                            }
                        }
                        if (enabled) { // Only check permission when turning the switch ON
                            // ... rest of the logic is correct
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    action()
                                } else {
                                    pendingPermissionAction = action
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                action()
                            }
                        } else {
                            action()
                        }
                    },
                    colors = customSwitchColors
                )
            }
        }
    }

    // --- ADDED: Dialog for permanently denied permission ---
    if (showPermissionSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionSettingsDialog = false },
            title = { Text("Permission Required") },
            text = { Text("Notifications are disabled for this app. To receive reminders and alerts, please enable notification permissions in the app settings.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionSettingsDialog = false
                        // Create an Intent to open the app's settings screen
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", context.packageName, null)
                        intent.data = uri
                        context.startActivity(intent)
                    }
                ) {
                    Text("Go to Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}