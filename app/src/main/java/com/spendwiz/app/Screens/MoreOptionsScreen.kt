package com.spendwiz.app.Screens

import android.content.Intent
import android.os.Build
import androidx.annotation.DrawableRes
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.spendwiz.app.Ads.CommonNativeAd
import com.spendwiz.app.AppStyle.AppColors.customCardColors
import com.spendwiz.app.R // Make sure to import your R file
import com.spendwiz.app.ViewModels.AddScreenViewModel
import com.spendwiz.app.navigation.Routes

/**
 * Updated data class to hold either an ImageVector or a Drawable resource ID.
 * A check ensures that at least one of them is provided.
 */
data class OptionItem(
    val title: String,
    val iconVector: ImageVector? = null,
    @DrawableRes val iconDrawableRes: Int? = null
) {
    init {
        require(iconVector != null || iconDrawableRes != null) {
            "Either iconVector or iconDrawableRes must be provided."
        }
    }
}

@Composable
fun MoreOptionsScreen(
    viewModel: AddScreenViewModel,
    navController: NavHostController
) {
    var showFeedback by remember { mutableStateOf(false) }

    // Updated list of options using the new OptionItem class.
    // Replace R.drawable.your_icon with your actual drawable resource IDs.
    val options = listOf(
        OptionItem("Backup", iconDrawableRes = R.drawable.baseline_backup_24),
        OptionItem("Category", iconVector = Icons.Filled.Settings),
        OptionItem("Notifications",  iconDrawableRes = R.drawable.notification),

        OptionItem("Assistant", iconDrawableRes = R.drawable.assistant),
        OptionItem("Scan Bill", iconDrawableRes = R.drawable.document_scanner),
        OptionItem("Feedback", iconDrawableRes = R.drawable.feedback),
        OptionItem("FAQ", iconDrawableRes = R.drawable.faq),
        OptionItem("Share", iconVector = Icons.Filled.Share),
    )


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            CommonNativeAd(Modifier ,
                stringResource(id = R.string.ad_unit_id_more_option_screen)
            )
        }
    ) { innerPadding ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.fillMaxSize()
                    .padding(
                        start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                        end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = innerPadding.calculateBottomPadding()
                    )
                    .padding(16.dp)
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
                                "FAQ" -> navController.navigate(Routes.FaqScreen.route)
                                "Backup" -> navController.navigate(Routes.BackupRestoreScreen.route)
                                "Feedback" -> showFeedback = true
                                "Scan Bill" -> navController.navigate(Routes.ReceiptScanScreen.route)
                                "Assistant" -> navController.navigate(Routes.VoiceAssistantSettingsScreen.route)
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

/**
 * Updated OptionCard to conditionally display an Icon from either
 * an ImageVector or a painterResource (drawable ID).
 */
@Composable
fun OptionCard(item: OptionItem, onClick: () -> Unit) {
    val customCardColors = customCardColors()
    Card(
        modifier = Modifier
            .size(100.dp)
            .clickable { onClick() },
        colors = customCardColors
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Conditionally render the icon based on what is provided
            item.iconVector?.let {
                Icon(
                    imageVector = it,
                    contentDescription = item.title,
                    modifier = Modifier.size(32.dp)
                )
            }
            item.iconDrawableRes?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = item.title,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
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
            "Try SpendWiz! It automatically tracks transactions, so you can easily see where your money goes. Check it out here: https://play.google.com/store/apps/details?id=com.spendwiz.app&pcampaignid=web_share"
        )
    }
    context.startActivity(Intent.createChooser(shareIntent, "Invite your friends via"))
}