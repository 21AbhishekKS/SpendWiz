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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        if (uncategorized.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No uncategorized transactions found.",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Selected transactions will be updated to $category / ${subCategory ?: "General"}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(16.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp)
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
                containerColor = Color(0xFF2196F3),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                Icon(Icons.Default.Done, contentDescription = "Update", tint = Color.White)
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
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .background(Color.White)
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
                contentDescription = "",
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
                    color = Color.Black
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.date,
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = item.bankName?.let { "($it)" } ?: "(Cash Payment)",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
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
                        color = Color.Gray
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
                val amountColor = when (item.type) {
                    TransactionType.INCOME -> Color(0xFF4CAF50)
                    TransactionType.EXPENSE -> Color(0xFFF44336)
                    else -> Color(0xFF3F51B5)
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
                    color = Color.Black,
                    fontSize = 12.sp,
                    textAlign = TextAlign.End
                )
            }

            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle(it) },
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2196F3))
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
