package com.spendwiz.app.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spendwiz.app.Ads.BannerAdView
import com.spendwiz.app.Database.money.Money
import com.spendwiz.app.Database.money.TransactionType
import com.spendwiz.app.ViewModels.AddScreenViewModel
import com.spendwiz.app.R
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkUpdateScreen(
    viewModel: AddScreenViewModel,
    navController: NavController,
    description: String,
    category: String,
    subCategory: String?
) {
    var uncategorized by remember { mutableStateOf<List<Money>>(emptyList()) }
    val selectedIds = remember { mutableStateListOf<Int>() }

    LaunchedEffect(description) {
        viewModel.getUncategorizedByNameOnce(description) { list ->
            uncategorized = list
            selectedIds.clear()
            selectedIds.addAll(list.map { it.id })
        }
    }

    Scaffold(
        bottomBar = {
            BannerAdView(
                adUnitId = stringResource(id = R.string.ad_unit_id_bulkUpdate_screen),
            )
        },
        floatingActionButton = {
            if (uncategorized.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        if (selectedIds.isNotEmpty()) {
                            viewModel.bulkUpdateCategory(
                                ids = selectedIds,
                                newCategory = category,
                                newSubCategory = subCategory ?: "General"
                            )
                        }
                        navController.popBackStack()
                        navController.popBackStack()
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Done, contentDescription = "Update")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = innerPadding.calculateBottomPadding()
                )
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (uncategorized.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No uncategorized transactions found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Selected transactions will be updated to $category / ${subCategory ?: "General"}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        items(uncategorized) { transaction ->
                            TransactionCard(
                                item = transaction,
                                isSelected = selectedIds.contains(transaction.id),
                                onToggle = { checked ->
                                    if (checked) selectedIds.add(transaction.id)
                                    else selectedIds.remove(transaction.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCard(
    item: Money,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onToggle(!isSelected) },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: icon
            val iconRes = when (item.type) {
                TransactionType.INCOME -> R.drawable.received_icon
                TransactionType.EXPENSE -> R.drawable.spent_icon
                else -> R.drawable.transaction_icon
            }
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = item.type.name,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(32.dp)
            )

            // Middle: details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.description,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.date,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = item.bankName?.let { "($it)" } ?: "(Cash Payment)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                item.upiRefNo?.let {
                    Text(
                        text = "UPI Ref: $it",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right: amount + time
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.widthIn(min = 70.dp)
            ) {
                val amountText = when (item.type) {
                    TransactionType.INCOME -> "+ ${item.amount}"
                    TransactionType.EXPENSE -> "- ${item.amount}"
                    else -> "${item.amount}"
                }
                // Semantic colors are kept for clarity (green for income, red for expense)
                val amountColor = when (item.type) {
                    TransactionType.INCOME -> Color(0xFF4CAF50)
                    TransactionType.EXPENSE -> Color(0xFFF44336)
                    else -> MaterialTheme.colorScheme.primary
                }
                Text(
                    text = amountText,
                    color = amountColor,
                    fontSize = 14.sp,
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = formatTimeTo12Hr(item.time),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    textAlign = TextAlign.End
                )
            }

            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle(it) },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

private fun formatTimeTo12Hr(time: String): String {
    return try {
        val input = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val output = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = input.parse(time)
        if (date != null) output.format(date) else time
    } catch (_: Exception) {
        time
    }
}