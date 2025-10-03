package com.spendwiz.app.Screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
// Import from material3
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spendwiz.app.AppStyle.AppColors.customCardColors

// Data class remains the same
data class FAQItem(
    val question: String,
    val answer: String
)

@Composable
fun FaqScreen() {
    val faqList = remember {
        listOf(
            FAQItem(
                "How do I add a new expense?",
                "Go to the Home screen and tap the '+' button. Fill in the details and save."
            ),
            FAQItem(
                "Can I edit or delete a transaction?",
                "Yes. Tap on the transaction from the history list and choose 'Edit' or 'Delete'."
            ),
            FAQItem(
                "Is my data backed up?",
                "Yes. All your data is securely stored and synced with the cloud using Firebase."
            ),
            FAQItem(
                "How do I switch between dark and light mode?",
                "You can change the theme from the Settings page under the 'Appearance' section."
            ),
            FAQItem(
                "How can I contact support?",
                "Go to Settings → Help & Support → Contact Us. You can also email us directly."
            )
        )
    }

    // M3 Surface automatically uses the theme's background color
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background // Use .colorScheme
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Frequently Asked Questions",
                style = MaterialTheme.typography.headlineSmall, // Use M3 typography
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground, // Use .colorScheme
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(faqList) { faq ->
                    FAQCard(faq = faq)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQCard(faq: FAQItem) {
    var expanded by remember { mutableStateOf(false) }

    // Use the M3 Card for better styling and elevation handling
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = customCardColors(),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.titleMedium, // Use M3 typography
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface, // Use .colorScheme
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary // Use .colorScheme
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300))
            ) {
                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodyMedium, // Use M3 typography
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // A good choice for secondary text in M3
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}