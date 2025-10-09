package com.spendwiz.app.voiceAssistant.ExternalAssistant

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.spendwiz.app.MainActivity
import com.spendwiz.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.Locale

class VoiceAssistantService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: ComposeView
    private lateinit var params: WindowManager.LayoutParams

    private var speechRecognizer: SpeechRecognizer? = null
    private val isListening = mutableStateOf(false)

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    // ✅ Make lifecycleOwner a property to access it in onDestroy
    private lateinit var lifecycleOwner: CustomLifecycleOwner

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        setupForegroundService()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Setup ComposeView and its required owners
        floatingView = ComposeView(this).apply {
            lifecycleOwner = CustomLifecycleOwner()
            lifecycleOwner.performRestore(null)
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)

            val viewModelStoreOwner = object : ViewModelStoreOwner {
                override val viewModelStore = ViewModelStore()
            }
            setViewTreeViewModelStoreOwner(viewModelStoreOwner)
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }

        floatingView.setContent {
            FloatingVoiceButton(
                // Reading .value here correctly subscribes composable to state changes
                isListening = isListening.value,
                onClick = {
                    if (!isListening.value) startListening() else stopListening()
                },
                onDrag = { dx, dy ->
                    params.x += dx.toInt()
                    params.y += dy.toInt()
                    windowManager.updateViewLayout(floatingView, params)
                }
            )
        }

        windowManager.addView(floatingView, params)
        setupSpeechRecognizer()

        // ✅ CRUCIAL FIX: Move the lifecycle to RESUMED to enable recomposition
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    private fun startListening() {
        Log.d("VoiceAssistantService", "startListening() called")
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer?.startListening(intent)
    }

    private fun stopListening() {
        Log.d("VoiceAssistantService", "stopListening() called")
        speechRecognizer?.stopListening()
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("VoiceAssistantService", "onReadyForSpeech -> isListening = true")
                isListening.value = true
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.d("VoiceAssistantService", "onEndOfSpeech -> isListening = false")
                isListening.value = false
            }

            override fun onError(error: Int) {
                Log.d("VoiceAssistantService", "onError($error) -> isListening = false")
                isListening.value = false
                Log.e("VoiceAssistant", "Error: $error")
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech input matched. Please try again."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions. Please grant audio recording permission."
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network error. Check your connection or download offline speech package."
                    else -> "An error occurred. Please try again."
                }
                Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show();
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val command = matches[0]
                    Log.i("VoiceAssistant", "Recognized: $command")
                    VoiceCommandHandler.processCommand(applicationContext, command)
                }
                // ✅ After getting results, the recognizer has stopped. Update the state.
                isListening.value = false
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun setupForegroundService() {
        val channelId = "voice_assistant_channel"
        val channelName = "Voice Assistant Service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_MIN)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Voice Assistant Active")
            .setContentText("Tap the floating button to add expenses.")
            .setSmallIcon(R.drawable.notification_icon)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(1, notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // ✅ Clean up the lifecycle owner to prevent leaks
        if (::lifecycleOwner.isInitialized) {
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }

        serviceJob.cancel()
        speechRecognizer?.destroy()
        if (::windowManager.isInitialized && ::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}

// This custom owner class is correct and needs no changes
class CustomLifecycleOwner : SavedStateRegistryOwner {
    private val store = ViewModelStore()
    private var mLifecycleRegistry = androidx.lifecycle.LifecycleRegistry(this)
    private var mSavedStateRegistryController = SavedStateRegistryController.create(this)

    override val savedStateRegistry: SavedStateRegistry get() = mSavedStateRegistryController.savedStateRegistry
    override val lifecycle: Lifecycle get() = mLifecycleRegistry

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        mLifecycleRegistry.handleLifecycleEvent(event)
    }

    fun performRestore(savedState: Bundle?) {
        mSavedStateRegistryController.performRestore(savedState)
    }
}