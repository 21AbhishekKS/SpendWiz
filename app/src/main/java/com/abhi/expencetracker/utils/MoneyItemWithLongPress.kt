package com.abhi.expencetracker.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.expencetracker.Database.money.Money
import com.abhi.expencetracker.Database.money.TransactionType
import com.abhi.expencetracker.R
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MoneyItemWithLongPress(
    item: Money,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFE3F2FD) else Color.White
        )
    ) {
        Row(
            Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🔹 Left side icon
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

            // 🔹 Middle details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(item.description, fontSize = 14.sp, color = Color.Black)
                Row {
                    Text(item.date, fontSize = 12.sp, color = Color.DarkGray)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = item.bankName?.let { "(${it})" } ?: "(Cash Payment)",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }
            }

            // 🔹 Right side amount + time stacked
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
                        TransactionType.INCOME -> Color(0xFF4CAF50)
                        TransactionType.EXPENSE -> Color(0xFFF44336)
                        else -> Color(0xFF3F51B5)
                    },
                    fontSize = 14.sp,
                    textAlign = TextAlign.End
                )

                Text(
                    text = formatTimeTo12Hr(item.time), // ✅ shows in 12hr format with AM/PM
                    color = Color.Black,
                    fontSize = 12.sp,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
