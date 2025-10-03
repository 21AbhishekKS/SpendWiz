package com.spendwiz.app.voiceAssistant.InternalAssistant

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.spendwiz.app.R
import com.spendwiz.app.voiceAssistant.ExternalAssistant.VoiceCommandHandler
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun InAppVoiceAssistantFab() {
    val context = LocalContext.current

    // State to hold the button's offset (position)
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // State to track if the assistant is currently listening
    var isListening by remember { mutableStateOf(false) }

    // Setup the SpeechRecognizer and its listener
    val speechRecognizer = remember {
        SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { isListening = true }
                override fun onBeginningOfSpeech() { /* Mic is active */ }
                override fun onEndOfSpeech() { isListening = false }
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onError(error: Int) {
                    isListening = false
                    Log.e("VoiceAssistant", "Error: $error")
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "I didn't catch that. Please try again."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Please grant audio recording permission."
                        else -> "An error occurred. Please try again."
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }

                override fun onResults(results: Bundle?) {
                    val spokenText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                    if (!spokenText.isNullOrBlank()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            VoiceCommandHandler.processCommand(context, spokenText)
                        }
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer.destroy()
        }
    }

    val recognizerIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
    }

    // Box to contain the FAB and handle dragging
    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        FloatingActionButton(
            onClick = {
                if (!isListening) {
                    speechRecognizer.startListening(recognizerIntent)
                } else {
                    speechRecognizer.stopListening()
                }
            },
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            containerColor = Color.Transparent,
            // Set elevation to 0dp to remove the shadow and make the background truly transparent
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Image(
                painter = painterResource(id = R.drawable.splash),
                contentDescription = "Voice Assistant",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
    }
}