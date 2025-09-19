package com.spendwiz.app.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spendwiz.app.Notifications.PreferencesManager
import kotlinx.coroutines.launch

@Composable
fun SmartSettings(preferencesManager: PreferencesManager) {
    val scope = rememberCoroutineScope()
    val autoCategorization by preferencesManager.autoCategorizationFlow.collectAsState(initial = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Automatic Categorization")
            Switch(
                checked = autoCategorization,
                onCheckedChange = { checked ->
                    scope.launch {
                        preferencesManager.setAutoCategorization(checked)
                    }
                }
            )
        }
    }
}
