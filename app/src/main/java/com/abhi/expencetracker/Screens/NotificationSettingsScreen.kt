package com.abhi.expencetracker.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhi.expencetracker.Notifications.PreferencesManager
import kotlinx.coroutines.launch

@Composable
fun NotificationSettingsScreen(prefs: PreferencesManager, onDailyToggle: (Boolean) -> Unit, onTransactionToggle: (Boolean) -> Unit) {
    val scope = rememberCoroutineScope()

    val dailyEnabled by prefs.dailyNotificationFlow.collectAsState(initial = true)
    val transactionEnabled by prefs.transactionNotificationFlow.collectAsState(initial = true)

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
                        onDailyToggle(enabled)
                    }
                }
            )
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
