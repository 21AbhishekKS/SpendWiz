package com.spendwiz.app.Screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.spendwiz.app.ViewModels.AddScreenViewModel
import com.spendwiz.app.navigation.Routes

data class OptionItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MoreOptionsScreen(
    viewModel: AddScreenViewModel,
    navController: NavHostController
) {
    var showFeedback by remember { mutableStateOf(false) }

    val options = listOf(
        OptionItem("Share", Icons.Filled.Share),
        OptionItem("Category", Icons.Filled.Settings),
        OptionItem("Notifications", Icons.Filled.Notifications),
        OptionItem("Smart settings", Icons.Filled.Settings),
        OptionItem("Feedback", Icons.Filled.Warning)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "More Options",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
            ) {
                items(options) { item ->
                    OptionCard(item = item, onClick = {
                        when (item.title) {
                            "Share" -> shareAppLink(navController.context)
                            "Category" -> navController.navigate(Routes.ManageCategoriesScreen.route)
                            "Notifications" -> navController.navigate(Routes.NotificationSettingsScreen.route)
                            "Smart settings" -> navController.navigate(Routes.SmartSettings.route)
                            "Feedback" -> showFeedback = true
                        }
                    })
                }
            }
        }

        if (showFeedback) {
            FeedbackDialog(onClose = { showFeedback = false })
        }
    }
}

@Composable
fun OptionCard(item: OptionItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(100.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = item.title, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun FeedbackDialog(onClose: () -> Unit) {
    Dialog(
        onDismissRequest = { onClose() },
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false    // allow full width
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),   // take full screen
            tonalElevation = 6.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { context ->
                        android.webkit.WebView(context).apply {
                            settings.javaScriptEnabled = true
                            loadUrl("https://spendwizfeedback.web.app")
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = { onClose() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.Red, shape = CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

private fun shareAppLink(context: android.content.Context) {
    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            android.content.Intent.EXTRA_TEXT,
            "Try SpendWiz! It automatically tracks transactions, so you can easily see where your money goes. Check it out here: https://play.google.com/store/apps/details?id=com.abhi.expencetracker&pcampaignid=web_share"
        )
    }
    context.startActivity(Intent.createChooser(shareIntent, "Invite your friends via"))
}
