package com.spendwiz.app.voiceAssistant

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
    Image(
        painter = painterResource(
            id = R.drawable.splash
        ), // 👈 use two images if you want (one for active, one for idle)
        contentDescription = "Voice Assistant",
        modifier = Modifier
            .size(55.dp) // increase size a bit for better tapping
            .clip(RoundedCornerShape(50.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() }
                )
            }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
    )
}