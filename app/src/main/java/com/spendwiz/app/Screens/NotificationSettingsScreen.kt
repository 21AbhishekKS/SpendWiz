package com.spendwiz.app.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.spendwiz.app.Notifications.PreferencesManager
import kotlinx.coroutines.launch

@Composable
fun NotificationSettingsScreen(
    prefs: PreferencesManager,
    onDailyToggle: (Boolean, Int, Int) -> Unit, // now also passes time
    onTransactionToggle: (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()

    val dailyEnabled by prefs.dailyNotificationFlow.collectAsState(initial = true)
    val dailyHour by prefs.dailyHourFlow.collectAsState(initial = 22)
    val dailyMinute by prefs.dailyMinuteFlow.collectAsState(initial = 5)

    val transactionEnabled by prefs.transactionNotificationFlow.collectAsState(initial = true)

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Notification Settings", style = MaterialTheme.typography.titleLarge)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Daily Reminder")
            Switch(
                checked = dailyEnabled,
                onCheckedChange = { enabled ->
                    scope.launch {
                        prefs.setDailyNotification(enabled)
                        onDailyToggle(enabled, dailyHour, dailyMinute)
                    }
                }
            )
        }

        if (dailyEnabled) {
            Button(onClick = {
                val picker = android.app.TimePickerDialog(
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
                )
                picker.show()
            }) {
                Text("Set Daily Reminder Time (${String.format("%02d:%02d", dailyHour, dailyMinute)})")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Transaction Alerts")
            Switch(
                checked = transactionEnabled,
                onCheckedChange = { enabled ->
                    scope.launch {
                        prefs.setTransactionNotification(enabled)
                        onTransactionToggle(enabled)
                    }
                }
            )
        }
    }
}
