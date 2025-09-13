package com.abhi.expencetracker.utils

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar

@Composable
fun TimePickerField(
    selectedTime: String,
    onTimeSelected: (String) -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val calendar = Calendar.getInstance()

                // Pre-fill with already selected time if available
                if (selectedTime.isNotEmpty()) {
                    val parts = selectedTime.split(":")
                    if (parts.size == 2) {
                        calendar.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                        calendar.set(Calendar.MINUTE, parts[1].toInt())
                    }
                }

                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                TimePickerDialog(
                    context,
                    { _, selectedHour: Int, selectedMinute: Int ->
                        val formattedTime =
                            String.format("%02d:%02d", selectedHour, selectedMinute)
                        onTimeSelected(formattedTime)
                    },
                    hour,
                    minute,
                    false
                ).show()
            }
    ) {
        TextField(
            value = selectedTime,
            onValueChange = {},
            readOnly = true,
            enabled = false, // disable manual typing
            label = { Text("Select Time") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledIndicatorColor = Color.Gray, // underline color
                disabledLabelColor = Color.Gray,
                disabledContainerColor = Color.Transparent
            )
        )
    }
}
