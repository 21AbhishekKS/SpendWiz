package com.spendwiz.app.voiceAssistant

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
import android.view.MotionEvent
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
import com.spendwiz.app.Database.money.Money
import com.spendwiz.app.Database.money.TransactionType
import com.spendwiz.app.MainActivity
import com.spendwiz.app.MainApplication
import com.spendwiz.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class VoiceAssistantService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: ComposeView
    private lateinit var params: WindowManager.LayoutParams

    private var speechRecognizer: SpeechRecognizer? = null
    private val isListening = mutableStateOf(false)

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        setupForegroundService()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatingView = ComposeView(this)

        // Make the ComposeView lifecycle-aware
        val lifecycleOwner = CustomLifecycleOwner()
        lifecycleOwner.performRestore(null)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        floatingView.setViewTreeLifecycleOwner(lifecycleOwner)
        floatingView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        val viewModelStoreOwner = object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
        floatingView.setViewTreeViewModelStoreOwner(viewModelStoreOwner)

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


        // Add touch listener for dragging
        floatingView.setOnTouchListener { _, event ->
            handleDrag(event)
            false // Return false to not consume the event if you have onClick listeners
        }

        windowManager.addView(floatingView, params)
        setupSpeechRecognizer()
    }

    private fun startListening() {
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
        isListening.value = true
    }

    private fun stopListening() {
        speechRecognizer?.stopListening()
        isListening.value = false
    }

    private fun handleDrag(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Not needed for simple drag
            }
            MotionEvent.ACTION_MOVE -> {
                params.x = event.rawX.toInt() - floatingView.width / 2
                params.y = event.rawY.toInt() - floatingView.height / 2
                windowManager.updateViewLayout(floatingView, params)
            }
        }
    }


    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening.value = false
            }

            override fun onError(error: Int) {
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
                    processCommand(command)
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    // In VoiceAssistantService.kt

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processCommand(command: String) {
        val moneyDao = MainApplication.moneyDatabase.getMoneyDao()
        val todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MM"))
        val currentYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"))


        when (val parsedResult = VoiceCommandParser.parse(command)) {

            is ParsedCommand.AddTransaction -> {
                serviceScope.launch(Dispatchers.IO) {
                    val newTransaction = Money(
                        amount = parsedResult.amount,
                        description = parsedResult.description,
                        type = parsedResult.type,
                        date = todayDate, // Can be enhanced to use parsed date
                        time = parsedResult.time ?: "", // Can be enhanced to use parsed time
                        category = parsedResult.category ?: "Others",
                        subCategory = null
                    )
                    val rowId = moneyDao.addMoney(newTransaction)
                    launch(Dispatchers.Main) {
                        if (rowId != -1L) {
                            Toast.makeText(applicationContext, "✅ Added: ${parsedResult.description}", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(applicationContext, "⚠️ Failed to add transaction.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

            is ParsedCommand.DeleteLastTransaction -> {
                serviceScope.launch(Dispatchers.IO) {
                    val lastTransaction = moneyDao.getLastTransaction()
                    if (lastTransaction != null) {
                        moneyDao.deleteMoney(lastTransaction)
                        launch(Dispatchers.Main) {
                            Toast.makeText(applicationContext, "✅ Last transaction deleted.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        launch(Dispatchers.Main) {
                            Toast.makeText(applicationContext, "⚠️ No transactions to delete.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

            is ParsedCommand.UpdateLastTransactionAmount -> {
                serviceScope.launch(Dispatchers.IO) {
                    moneyDao.updateLastTransactionAmount(parsedResult.newAmount)
                    launch(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "✅ Last transaction amount updated to ${parsedResult.newAmount}", Toast.LENGTH_LONG).show()
                    }
                }
            }

            is ParsedCommand.UpdateLastTransactionCategory -> {
                serviceScope.launch(Dispatchers.IO) {
                    moneyDao.updateLastTransactionCategory(parsedResult.newCategory)
                    launch(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "✅ Last transaction category updated to ${parsedResult.newCategory}", Toast.LENGTH_LONG).show()
                    }
                }
            }

            is ParsedCommand.DeleteTodayTransactions -> {
                serviceScope.launch(Dispatchers.IO) {
                    moneyDao.deleteTransactionsByDate(todayDate)
                    launch(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "✅ All transactions from today have been deleted.", Toast.LENGTH_LONG).show()
                    }
                }
            }


            is ParsedCommand.QueryDailyTotal -> {
                serviceScope.launch(Dispatchers.IO) {
                    val total = moneyDao.getTotalForDate(todayDate, parsedResult.type.name) ?: 0.0
                    val formattedTotal = DecimalFormat("#,##0.00").format(total)
                    val typeString = parsedResult.type.name.lowercase()

                    launch(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Total $typeString for today: ₹$formattedTotal", Toast.LENGTH_LONG).show()
                    }
                }
            }

            is ParsedCommand.QueryMonthlySummary -> {
                serviceScope.launch(Dispatchers.IO) {
                    val summary = moneyDao.getMonthlySummary(currentMonth, currentYear)
                    val message = if (summary != null) {
                        "Summary for this month:\nIncome: ₹${summary.totalIncome}\nExpense: ₹${summary.totalExpense}"
                    } else {
                        "No data found for this month."
                    }
                    launch(Dispatchers.Main) {
                        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            is ParsedCommand.QueryBiggestExpense -> {
                serviceScope.launch(Dispatchers.IO) {
                    val expense = moneyDao.getBiggestExpenseForMonth(currentMonth, currentYear)
                    val message = if (expense != null) {
                        "Biggest expense this month was ₹${expense.amount} for '${expense.description}'."
                    } else {
                        "No expenses recorded this month."
                    }
                    launch(Dispatchers.Main) {
                        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            is ParsedCommand.OpenAddExpenseScreen -> {
                // Navigation requires starting an activity from the service
                val intent = Intent(applicationContext, MainActivity::class.java).apply {
                    putExtra("NAVIGATE_TO", "ADD_EXPENSE") // Add a key to handle in MainActivity
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
            }

            is ParsedCommand.ImportFromSMS -> {

                Toast.makeText(this, "SMS Import feature not yet implemented via voice.", Toast.LENGTH_LONG).show()
            }


            is ParsedCommand.Unrecognized -> {
                Toast.makeText(this, "Couldn't understand: \"$command\"", Toast.LENGTH_LONG).show()
            }

            // Catch-all for parsed but not implemented commands
            else -> {
                Toast.makeText(this, "This voice command is not implemented yet.", Toast.LENGTH_LONG).show()
            }
        }
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
            .setSmallIcon(R.drawable.add_transation_notification)
            .setContentIntent(pendingIntent)
            .build()

        // Correctly call startForeground with the type
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 and above, specify the type
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            // For older versions, use the old method
            startForeground(1, notification)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        speechRecognizer?.destroy()
        if (::windowManager.isInitialized && ::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}


// A trick to provide a LifecycleOwner to the ComposeView running in a Service
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