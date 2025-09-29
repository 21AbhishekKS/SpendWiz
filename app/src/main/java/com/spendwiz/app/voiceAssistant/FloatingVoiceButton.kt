package com.spendwiz.app.voiceAssistant

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FloatingVoiceButton(isListening: Boolean, onClick: () -> Unit) {
    val fabColor by animateColorAsState(
        targetValue = if (isListening) Color.Red else Color.Blue,
        animationSpec = tween(500)
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(64.dp),
        shape = CircleShape,
        backgroundColor = fabColor
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Voice Command",
            tint = Color.White
        )
    }
}