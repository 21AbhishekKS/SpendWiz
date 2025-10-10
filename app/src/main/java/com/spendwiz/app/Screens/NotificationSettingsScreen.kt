package com.spendwiz.app.Screens

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.spendwiz.app.Ads.CommonNativeAd
import com.spendwiz.app.AppStyle.AppColors.customSwitchColors
import com.spendwiz.app.R
import com.spendwiz.app.Notifications.PreferencesManager
import kotlinx.coroutines.CoroutineScope
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

    var showPermissionSettingsDialog by remember { mutableStateOf(false) }
    var pendingPermissionAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val activity = LocalContext.current as Activity
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            pendingPermissionAction?.invoke()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)) {
                showPermissionSettingsDialog = true
            }
        }
        pendingPermissionAction = null
    }

    val configuration = LocalConfiguration.current // Get screen configuration

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Only show bottom bar ad in portrait mode
            if (configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                CommonNativeAd(
                    Modifier,
                    stringResource(id = R.string.ad_unit_id_notification_settings_screen)
                )
            }
        }
    ) { innerPadding ->
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // --- Landscape Layout: 50% content, 50% ad ---
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // Apply padding from Scaffold
            ) {
                // Left half: Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Notification Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    // We reuse PortraitLayout here because it stacks the settings vertically,
                    // which is what we want in the left half of the landscape view.
                    PortraitLayout(
                        prefs, scope, dailyEnabled, dailyHour, dailyMinute, transactionEnabled,
                        onDailyToggle, onTransactionToggle,
                        onPermissionRequested = { action ->
                            pendingPermissionAction = action
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    )
                }

                // Right half: Ad
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CommonNativeAd(
                        Modifier,
                        stringResource(id = R.string.ad_unit_id_notification_settings_screen)
                    )
                }
            }
        } else {
            // --- Portrait Layout (existing logic) ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                        end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = innerPadding.calculateBottomPadding()
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "Notification Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                PortraitLayout(
                    prefs, scope, dailyEnabled, dailyHour, dailyMinute, transactionEnabled,
                    onDailyToggle, onTransactionToggle,
                    onPermissionRequested = { action ->
                        pendingPermissionAction = action
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                )
            }
        }
    }

    // --- Dialog for permanently denied permission (remains unchanged) ---
    if (showPermissionSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionSettingsDialog = false },
            title = { Text("Permission Required") },
            text = { Text("Notifications are disabled for this app. To receive reminders and alerts, please enable notification permissions in the app settings.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionSettingsDialog = false
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

// --- Portrait Layout Composable ---
@Composable
private fun PortraitLayout(
    prefs: PreferencesManager,
    scope: CoroutineScope,
    dailyEnabled: Boolean,
    dailyHour: Int,
    dailyMinute: Int,
    transactionEnabled: Boolean,
    onDailyToggle: (Boolean, Int, Int) -> Unit,
    onTransactionToggle: (Boolean) -> Unit,
    onPermissionRequested: (() -> Unit) -> Unit
) {
    Column {
        DailyReminderSetting(
            prefs, scope, dailyEnabled, dailyHour, dailyMinute, onDailyToggle, onPermissionRequested
        )
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        TransactionAlertsSetting(
            prefs, scope, transactionEnabled, onTransactionToggle, onPermissionRequested
        )
    }
}

// --- Reusable Daily Reminder Setting ---
@Composable
private fun DailyReminderSetting(
    prefs: PreferencesManager,
    scope: CoroutineScope,
    dailyEnabled: Boolean,
    dailyHour: Int,
    dailyMinute: Int,
    onDailyToggle: (Boolean, Int, Int) -> Unit,
    onPermissionRequested: (() -> Unit) -> Unit
) {
    val context = LocalContext.current
    val buttonColor = colorResource(id = R.color.button_color)
    val customSwitchColors = customSwitchColors()

    val onCheckedChangeAction: (Boolean) -> Unit = { enabled ->
        val action: () -> Unit = {
            scope.launch {
                prefs.setDailyNotification(enabled)
                onDailyToggle(enabled, dailyHour, dailyMinute)
            }
        }
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    action()
                } else {
                    onPermissionRequested(action)
                }
            } else {
                action()
            }
        } else {
            action()
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Daily Reminder", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Get a summary of your daily spending.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = dailyEnabled,
            onCheckedChange = onCheckedChangeAction,
            colors = customSwitchColors
        )
    }

    AnimatedVisibility(
        visible = dailyEnabled,
        enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
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
                        true
                    ).show()
                }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Reminder Time", style = MaterialTheme.typography.titleMedium)
            Text(
                text = String.format("%02d:%02d", dailyHour, dailyMinute),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = buttonColor
            )
        }
    }
}

// --- Reusable Transaction Alerts Setting ---
@Composable
private fun TransactionAlertsSetting(
    prefs: PreferencesManager,
    scope: CoroutineScope,
    transactionEnabled: Boolean,
    onTransactionToggle: (Boolean) -> Unit,
    onPermissionRequested: (() -> Unit) -> Unit
) {
    val context = LocalContext.current
    val customSwitchColors = customSwitchColors()

    val onCheckedChangeAction: (Boolean) -> Unit = { enabled ->
        val action: () -> Unit = {
            scope.launch {
                prefs.setTransactionNotification(enabled)
                onTransactionToggle(enabled)
            }
        }
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    action()
                } else {
                    onPermissionRequested(action)
                }
            } else {
                action()
            }
        } else {
            action()
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Transaction Alerts", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Receive an alert for every new transaction.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = transactionEnabled,
            onCheckedChange = onCheckedChangeAction,
            colors = customSwitchColors
        )
    }
}

