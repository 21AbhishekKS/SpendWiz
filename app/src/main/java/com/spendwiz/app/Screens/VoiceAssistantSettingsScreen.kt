package com.spendwiz.app.Screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spendwiz.app.AppStyle.AppColors.customButtonColors
import com.spendwiz.app.AppStyle.AppColors.customCardColors
import com.spendwiz.app.AppStyle.AppColors.customSwitchColors
import com.spendwiz.app.R
import com.spendwiz.app.voiceAssistant.VoiceAssistantService

// This class remains unchanged as it handles data logic, not UI.
class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("voice_assistant_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_SERVICE_ENABLED = "key_service_enabled"
    }

    fun setServiceEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_SERVICE_ENABLED, enabled).apply()
    }

    fun isServiceEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_SERVICE_ENABLED, false)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistantSettingsScreen() {
    val context = LocalContext.current
    val prefsManager = remember { PreferencesManager(context) }
    var isServiceEnabled by remember { mutableStateOf(prefsManager.isServiceEnabled()) }

    // Launcher to request audio permission.
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceService(context)
        } else {
            Toast.makeText(context, "Audio permission is required to use the voice assistant.", Toast.LENGTH_LONG).show()
            // Revert the switch state if permission is denied.
            isServiceEnabled = false
            prefsManager.setServiceEnabled(false)
        }
    }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    "Voice Assistant",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = customCardColors()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Voice Assistant",
                            // M3 Typography: Replaced h6 with titleLarge
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Allow Spendwiz mini to appear over other apps to take voice commands.",
                            // M3 Typography: Replaced body2 with bodyMedium
                            style = MaterialTheme.typography.bodyMedium,
                            // M3 Color Scheme: Replaced onSurface with onSurfaceVariant for secondary text
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        colors = customSwitchColors(),
                        checked = isServiceEnabled,
                        onCheckedChange = { isChecked ->
                            isServiceEnabled = isChecked
                            prefsManager.setServiceEnabled(isChecked)

                            if (isChecked) {
                                checkPermissionsAndStartService(context) {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            } else {
                                stopVoiceService(context)
                            }
                        }
                    )
                }
            }

            // Button to open Text-to-Speech settings.
            Button(
                colors = customButtonColors(),
                onClick = {
                    val intent = Intent("com.android.settings.TTS_SETTINGS").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not open Text-to-Speech settings.", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Download Offline Voice Package")
            }

            // Display a card with example voice commands.
            VoiceCommandNoticeCard()
        }

}

// Helper function to check "Draw over other apps" permission before requesting audio.
private fun checkPermissionsAndStartService(context: Context, requestAudioPermission: () -> Unit) {
    if (!Settings.canDrawOverlays(context)) {
        Toast.makeText(context, "Please grant 'Draw over other apps' permission.", Toast.LENGTH_LONG).show()
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        context.startActivity(intent)
        // Note: You might need to handle the result of this action to update the switch state.
        return
    }
    requestAudioPermission()
}

// Helper function to start the VoiceAssistantService.
private fun startVoiceService(context: Context) {
    val intent = Intent(context, VoiceAssistantService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}

// Helper function to stop the VoiceAssistantService.
private fun stopVoiceService(context: Context) {
    context.stopService(Intent(context, VoiceAssistantService::class.java))
}

@Composable
fun VoiceCommandNoticeCard() {
    val commands = listOf(
        "Add 150 expense for lunch",
        "Add 50000 income for salary",
        "Add 700 expense for fuel in transportation category",
        "Delete last transaction",
        "Update last transaction amount to 250",
        "Change category of last expense to utilities",
        "Delete all transactions from today",
        "What is my total expense today",
        "What's my income for today",
        "Show my summary for this month",
        "What is my biggest expense this month"
    )

    // Using Card instead of Surface for better semantics in M3.
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        // M3 Color Scheme: Using surfaceVariant for a distinct but harmonious background.
        colors = customCardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quick Voice Commands",
                // M3 Typography: Replaced h6 with titleMedium
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                // M3 Color Scheme: onSurfaceVariant is suitable for titles on a surfaceVariant background.
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            // HorizontalDivider is the new M3 component for Divider.
            HorizontalDivider(
                // M3 Color Scheme: outline is the standard color for dividers.
                color = MaterialTheme.colorScheme.outline,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Using Column with Arrangement.spacedBy for cleaner spacing.
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                commands.forEach { command ->
                    Text(
                        text = "• $command",
                        // M3 Typography: Replaced custom font size with bodyMedium
                        style = MaterialTheme.typography.bodyMedium,
                        // M3 Color Scheme: onSurfaceVariant for body text.
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}