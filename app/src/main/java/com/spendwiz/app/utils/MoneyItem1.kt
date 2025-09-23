package com.spendwiz.app.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendwiz.app.Database.money.Money
import com.spendwiz.app.Database.money.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale
import com.spendwiz.app.R

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

@Composable
fun MoneyItem1(
    item: Money,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side (icon + text details)
            Image(
                painter = painterResource(
                    id = when (item.type) {
                        TransactionType.INCOME -> R.drawable.received_icon
                        TransactionType.EXPENSE -> R.drawable.spent_icon
                        else -> R.drawable.transaction_icon
                    }
                ),
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(32.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Description
                Text(
                    text = item.description,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Bank name + Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.date,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (item.bankName != null) "(${item.bankName})" else "(Cash Payment)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // UPI Ref
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

            // Right side (amount + formatted time)
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.widthIn(min = 70.dp)
            ) {
                Text(
                    text = when (item.type) {
                        TransactionType.INCOME -> "+ ${item.amount}"
                        TransactionType.EXPENSE -> "- ${item.amount}"
                        else -> "${item.amount}"
                    },
                    color = when (item.type) {
                        TransactionType.INCOME -> colorResource(id = R.color.income) // Green
                        TransactionType.EXPENSE -> colorResource(id = R.color.expense) // Red
                        else -> colorResource(id = R.color.transfer) // Blue
                    },
                    fontSize = 14.sp,
                    textAlign = TextAlign.End
                )

                Text(
                    text = formatTimeTo12Hr(item.time),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
