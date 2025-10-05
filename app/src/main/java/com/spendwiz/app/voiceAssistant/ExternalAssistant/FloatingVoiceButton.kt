package com.spendwiz.app.voiceAssistant.ExternalAssistant

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.spendwiz.app.R

@Composable
fun FloatingVoiceButton(
    isListening: Boolean,
    onClick: () -> Unit,
    onDrag: (dx: Float, dy: Float) -> Unit
) {
    Log.d("FloatingVoiceButton", "Recomposing... isListening = $isListening")

    val glowScale by animateFloatAsState(
        targetValue = if (isListening) 1.4f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "glowScale"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isListening) 0.3f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "glowAlpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(75.dp)
            .clip(CircleShape)
            .drawBehind {
                scale(scale = glowScale) {
                    drawCircle(
                        color = Color(0xFF9C27B0), // Purple color
                        alpha = glowAlpha
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() })
            }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.turbo),
            contentDescription = "Voice Assistant",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape) // This clip is still good practice
        )
    }
}