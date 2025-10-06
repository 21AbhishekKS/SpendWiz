package com.spendwiz.app.Screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.spendwiz.app.Ads.BannerAdView
import com.spendwiz.app.AppStyle.AppColors.customButtonColors
import com.spendwiz.app.AppStyle.AppColors.customCardColors
import com.spendwiz.app.AppStyle.AppColors.customSwitchColors
import com.spendwiz.app.R
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

class PermissionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("permission_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_MIC_DENIAL_COUNT = "mic_denial_count"
    }

    fun getMicrophoneDenialCount(): Int {
        return prefs.getInt(KEY_MIC_DENIAL_COUNT, 0)
    }

    fun incrementMicrophoneDenialCount() {
        val count = getMicrophoneDenialCount()
        prefs.edit().putInt(KEY_MIC_DENIAL_COUNT, count + 1).apply()
    }

    fun resetMicrophoneDenialCount() {
        prefs.edit().remove(KEY_MIC_DENIAL_COUNT).apply()
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
    val activity = context as Activity
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }

    val permissionManager = remember { PermissionManager(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            permissionManager.resetMicrophoneDenialCount()
            Toast.makeText(context, "Permission Granted. Please enable the feature again.", Toast.LENGTH_SHORT).show()
        } else {
            permissionManager.incrementMicrophoneDenialCount()
            Toast.makeText(context, "Microphone permission is required.", Toast.LENGTH_SHORT).show()
        }
        // Ensure switches are off if permission is denied during the request.
        onServiceToggle(false)
        onInAppAssistantToggle(false)
    }

    if (showPermissionRationaleDialog) {
        PermissionRationaleDialog(
            onDismiss = { showPermissionRationaleDialog = false },
            onConfirm = {
                showPermissionRationaleDialog = false
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        )
    }

    // This helper function ensures the microphone permission is granted before executing an action.
    // It does NOT modify the UI state itself, which was the source of the original bug.
    fun ensureMicrophonePermission(onGranted: () -> Unit) {
        val isMicPermissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (isMicPermissionGranted) {
            onGranted() // Permission is already granted, so we can proceed.
            return
        }

        // Logic for requesting microphone permission if it's not already granted.
        val denialCount = permissionManager.getMicrophoneDenialCount()
        val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            activity, Manifest.permission.RECORD_AUDIO
        )

        if (denialCount >= 2 || (denialCount > 0 && !shouldShowRationale)) {
            showPermissionRationaleDialog = true
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BannerAdView(
                adUnitId = stringResource(id = R.string.ad_unit_id_assistant_screen),
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = innerPadding.calculateBottomPadding()
                )
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
                            if (isChecked) {
                                // First, check for microphone permission.
                                ensureMicrophonePermission {
                                    // Only update the state *after* permission is confirmed.
                                    onInAppAssistantToggle(true)
                                }
                            } else {
                                onInAppAssistantToggle(false)
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
                            if (isChecked) {
                                // 1. Check for microphone permission.
                                ensureMicrophonePermission {
                                    // 2. This lambda only runs if mic permission is granted.
                                    //    Now, check for the overlay permission.
                                    checkPermissionsAndStartService(context) {
                                        // 3. This lambda only runs if overlay permission is also granted.
                                        //    Now, it's safe to update the state and start the service.
                                        onServiceToggle(true)
                                        startVoiceService(context)
                                    }
                                }
                            } else {
                                onServiceToggle(false)
                                stopVoiceService(context)
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
                        Toast.makeText(
                            context,
                            "Could not open Text-to-Speech settings.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Download Offline Voice Package")
            }

            AssistantDragNoticeCard()

            VoiceCommandNoticeCard()
        }
    }
}

@Composable
private fun PermissionRationaleDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission Required") },
        text = { Text("Microphone access has been denied. To use this feature, please manually grant the permission in the app settings.") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun checkPermissionsAndStartService(context: Context, onPermissionsGranted: () -> Unit) {
    if (!Settings.canDrawOverlays(context)) {
        Toast.makeText(context, "Please grant 'Draw over other apps' permission.", Toast.LENGTH_LONG).show()
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        context.startActivity(intent)
        // Do not call onPermissionsGranted() if permission is missing.
        return
    }
    // Only call this when the permission is confirmed.
    onPermissionsGranted()
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
        "What is my biggest expense this month",
        "Go to Home. (Instead of 'Home', you can use other screen name)",
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

@Composable
fun AssistantDragNoticeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = customCardColors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Information",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "You can long press and drag the assistant to adjust its position on the screen.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}