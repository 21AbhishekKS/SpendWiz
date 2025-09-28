package com.spendwiz.app.utils

import java.text.SimpleDateFormat
import java.util.Locale

fun formatTimeTo12Hr(time24: String): String {
    val possibleFormats = listOf("HH:mm:ss", "HH:mm")
    for (pattern in possibleFormats) {
        try {
            val inputFormat = SimpleDateFormat(pattern, Locale.getDefault())
            val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = inputFormat.parse(time24)
            if (date != null) {
                return outputFormat.format(date)
            }
        } catch (_: Exception) {
            // ignore and try next pattern
        }
    }
    return time24 // fallback if nothing works
}

