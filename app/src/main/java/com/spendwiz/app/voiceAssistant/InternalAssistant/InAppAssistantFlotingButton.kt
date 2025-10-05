package com.spendwiz.app.voiceAssistant.InternalAssistant

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
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

    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isListening by remember { mutableStateOf(false) }

    // --- Voice Recognizer Setup (No changes needed here) ---
    val speechRecognizer = remember {
        SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { isListening = true }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
                override fun onEndOfSpeech() { isListening = false }

                override fun onError(error: Int) {
                    isListening = false
                    Log.e("VoiceAssistant", "Error: $error")
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "I didn't catch that. Try again."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Audio permission needed."
                        else -> "An error occurred."
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

    // ✨ --- UI Animation States --- ✨
    val glowScale by animateFloatAsState(
        // ✅ Reduced target value from 1.6f to 1.2f to make the glow smaller
        targetValue = if (isListening) 1.2f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "glowScale"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isListening) 0.3f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "glowAlpha"
    )

    // --- Main Composable Layout ---
    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { /* Can add logic here if needed */ }
                ) { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                // ✅ Reduced the container size for a tighter effect
                .size(70.dp)
                .clip(CircleShape) // Clipped the container to ensure a round shape
                .drawBehind {
                    scale(scale = glowScale) {
                        drawCircle(
                            color = Color(0xFF9C27B0), // Purple color
                            alpha = glowAlpha
                        )
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (!isListening) {
                                speechRecognizer.startListening(recognizerIntent)
                            } else {
                                speechRecognizer.stopListening()
                            }
                        }
                    )
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.nano),
                contentDescription = "Nano Voice Assistant",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    // ✅ Adjusted image size to fit the new container
                    .size(50.dp)
                    .clip(CircleShape)
            )
        }
    }
}