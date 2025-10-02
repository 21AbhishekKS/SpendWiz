package com.spendwiz.app.Screens

import android.app.TimePickerDialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spendwiz.app.AppStyle.AppColors.customSwitchColors
import com.spendwiz.app.R // Assuming R class is in this package
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

    // Use the specified color for UI emphasis.
    val buttonColor = colorResource(id = R.color.button_color)

    // Define custom colors for the Switch using the button color for the 'on' state.
    val customSwitchColors = customSwitchColors()

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                onCheckedChange = { enabled ->
                    scope.launch {
                        prefs.setDailyNotification(enabled)
                        onDailyToggle(enabled, dailyHour, dailyMinute)
                    }
                },
                colors = customSwitchColors // Apply custom colors here
            )
        }

        // --- Animated Time Picker Setting ---
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
                onCheckedChange = { enabled ->
                    scope.launch {
                        prefs.setTransactionNotification(enabled)
                        onTransactionToggle(enabled)
                    }
                },
                colors = customSwitchColors
            )
        }
    }
}