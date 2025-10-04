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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spendwiz.app.AppStyle.AppColors.customButtonColors
import com.spendwiz.app.AppStyle.AppColors.customCardColors
import com.spendwiz.app.AppStyle.AppColors.customSwitchColors
import com.spendwiz.app.voiceAssistant.ExternalAssistant.VoiceAssistantService

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("voice_assistant_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_SERVICE_ENABLED = "key_service_enabled"
        const val KEY_IN_APP_ASSISTANT_ENABLED = "key_in_app_assistant_enabled"
    }

    fun setServiceEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_SERVICE_ENABLED, enabled).apply()
    }

    fun isServiceEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_SERVICE_ENABLED, false)
    }

    fun setInAppAssistantEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IN_APP_ASSISTANT_ENABLED, enabled).apply()
    }

    fun isInAppAssistantEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IN_APP_ASSISTANT_ENABLED, false)
    }
}

@Composable
fun VoiceAssistantSettingsScreen(
    isServiceEnabled: Boolean,
    isInAppAssistantEnabled: Boolean,
    onServiceToggle: (Boolean) -> Unit,
    onInAppAssistantToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current

    val externalAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceService(context)
        } else {
            Toast.makeText(context, "Audio permission is required for the external assistant.", Toast.LENGTH_LONG).show()
            onServiceToggle(false)
        }
    }

    val inAppAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Audio permission is required for the in-app assistant.", Toast.LENGTH_LONG).show()
            onInAppAssistantToggle(false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        text = "Turn on Turbo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Spendwiz Turbo is a voice assistant that works even if the app is closed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    colors = customSwitchColors(),
                    checked = isServiceEnabled,
                    onCheckedChange = { isChecked ->
                        onServiceToggle(isChecked)
                        if (isChecked) {
                            checkPermissionsAndStartService(context) {
                                externalAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        } else {
                            stopVoiceService(context)
                        }
                    }
                )
            }
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
                        text = "Turn on Nano",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Spendwiz Nano is a voice assistant that works only within the app.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    colors = customSwitchColors(),
                    checked = isInAppAssistantEnabled,
                    onCheckedChange = { isChecked ->
                        onInAppAssistantToggle(isChecked)
                        if (isChecked) {
                            inAppAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                )
            }
        }


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

        VoiceCommandNoticeCard()
    }
}

private fun checkPermissionsAndStartService(context: Context, requestAudioPermission: () -> Unit) {
    if (!Settings.canDrawOverlays(context)) {
        Toast.makeText(context, "Please grant 'Draw over other apps' permission.", Toast.LENGTH_LONG).show()
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        context.startActivity(intent)
        return
    }
    requestAudioPermission()
}

private fun startVoiceService(context: Context) {
    val intent = Intent(context, VoiceAssistantService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}

private fun stopVoiceService(context: Context) {
    context.stopService(Intent(context, VoiceAssistantService::class.java))
}

@Composable
fun VoiceCommandNoticeCard() {
    val commands = listOf(
        "Add 150 expense for lunch",
        "Add 50000 income for salary",
        "Delete last transaction",
        "Update last transaction amount to 250",
        "Delete all transactions from today",
        "What is my total expense today",
        "What's my income for today",
        "Show my summary for this month",
        "What is my biggest expense this month"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = customCardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quick Voice Commands",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                commands.forEach { command ->
                    Text(
                        text = "• $command",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}