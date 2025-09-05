package com.abhi.expencetracker.Screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.abhi.expencetracker.ViewModels.AddScreenViewModel

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
    val options = listOf(
        OptionItem("Backup", Icons.Filled.Settings),
        OptionItem("Share", Icons.Filled.Share),
        OptionItem("Settings", Icons.Filled.Settings),
        OptionItem("Notifications", Icons.Filled.Notifications),
        OptionItem("History", Icons.Filled.Settings),
        OptionItem("Help", Icons.Filled.Settings)
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
                    .padding(bottom = 16.dp) // ensures last row isn't cut
            ) {
                items(options) { item ->
                    OptionCard(item = item, onClick = {
                        when (item.title) {
                            "Backup" -> { /* call backup function */ }
                            "Share" -> { shareAppLink(navController.context)  }
                            "Settings" -> { /* open settings */ }
                            "Notifications" -> { /* open notifications */ }
                            "History" -> { /* open history */ }
                            "Help" -> { /* open help */ }
                        }
                    })
                }
            }
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

private fun shareAppLink(context: android.content.Context) {
    val appPackageName = context.packageName
    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            android.content.Intent.EXTRA_TEXT,
            "Try SpendWiz! It automatically tracks transactions, so you can easily see where your money goes. Check it out here: https://play.google.com/store/apps/details?id=com.abhi.expencetracker&pcampaignid=web_share"
        )
    }
    context.startActivity(
        android.content.Intent.createChooser(shareIntent, "Invite your friends via")
    )
}
