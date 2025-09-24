package com.spendwiz.app.utils

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
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
                        try {
                            calendar.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                            calendar.set(Calendar.MINUTE, parts[1].toInt())
                        } catch (e: NumberFormatException) {
                            // Handle cases where the selectedTime is not a valid time format
                        }
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
                    // Use true for 24-hour format, false for 12-hour format with AM/PM
                    false
                ).show()
            }
    ) {
        TextField(
            value = selectedTime,
            onValueChange = {},
            readOnly = true,
            // We use readOnly=true, so enabled=false is not strictly necessary
            // but can be kept for clarity and to prevent focus.
            enabled = false,
            label = { Text("Select Time") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                // Use theme-aware colors instead of hardcoded values
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContainerColor = Color.Transparent
            )
        )
    }
}