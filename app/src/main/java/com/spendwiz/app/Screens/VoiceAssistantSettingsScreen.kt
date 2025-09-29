package com.spendwiz.app.Screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendwiz.app.voiceAssistant.VoiceAssistantService

@Composable
fun VoiceAssistantSettingsScreen() {
    val context = LocalContext.current
    var isServiceEnabled by remember { mutableStateOf(false) } // This should be backed by a preference

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceService(context)
            isServiceEnabled = true
        } else {
            Toast.makeText(context, "Audio permission is required for voice commands.", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Voice Assistant Settings") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Voice Assistant", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("A floating button will appear to take voice commands.", fontSize = 14.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isServiceEnabled,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                checkPermissionsAndStartService(context) {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            } else {
                                stopVoiceService(context)
                                isServiceEnabled = false
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
                // Intent to download offline speech recognition files
                val intent = Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not open language settings.", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Download Offline Voice Package")
            }
        }
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

    // After overlay permission is handled, request audio permission
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