package com.spendwiz.app.Screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.spendwiz.app.Ads.CommonNativeAd
import com.spendwiz.app.AppStyle.AppColors.customCardColors
import com.spendwiz.app.R

// Data class remains the same
data class FAQItem(
    val question: String,
    val answer: String
)

@Composable
fun FaqScreen() {
    // The new FAQ content has been added here
    val faqList = remember {
        listOf(
            FAQItem(
                question = "Do you read all my personal SMS messages?",
                answer = "No, absolutely not. Our app is designed to only read transactional SMS from banks and businesses. It completely ignores your personal conversations with friends and family. Your privacy is a top priority."
            ),
            FAQItem(
                question = "How do you keep my financial data safe?",
                answer = """We take your security very seriously and protect your data in three key ways:

1. On-Device Encryption: All your financial data is encrypted and stored securely on your phone itself, not on our servers.

2. No Third-Party Sharing: We never share your personal or financial data with any third-party companies.

3. You Control Your Backup: When you back up your data, it's saved directly to your own personal cloud drive (like Google Drive). We never have access to it."""
            ),
            FAQItem(
                question = "What is the difference between the Nano and Turbo assistants?",
                answer = """The assistants help process your transactions through voice commands. You can choose the one that best fits your needs:

• Nano Assistant: This mode is light and efficient. It only works when the app is open and running on your screen.

• Turbo Assistant: This is our most powerful mode. It works in the background even when the app is closed."""
            ),
            FAQItem(
                question = "The app isn't detecting my transaction SMS automatically. What should I do?",
                answer = """This can usually be fixed with a couple of simple steps:

1. Check SMS Permissions: First, please go to your phone's Settings > Apps > Spendwiz > Permissions and make sure that SMS permission is allowed.

2. Send Us an Example: If the permission is correct and it's still not working, please go to the Feedback screen in the app and send us the full, exact transaction message you received, along with your bank's name. This will help our team add support for it."""
            ),
            FAQItem(
                question = "Why can't I categorize my spending directly from the notification on my phone?",
                answer = "This feature's availability depends on your phone's manufacturer. Some brands, unfortunately, restrict certain advanced notification actions. If you can't see the option, it's likely a limitation set by your phone's brand, not a bug in our app."
            )
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            CommonNativeAd(Modifier ,
                stringResource(id = R.string.ad_unit_id_faq_screen)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = innerPadding.calculateBottomPadding()
                )
                .padding(16.dp)
        ) {
            Text(
                text = "Frequently Asked Questions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300))
            ) {
                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Justify
                )
            }
        }
    }
}