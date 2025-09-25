package com.spendwiz.app.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendwiz.app.Database.money.Money
import com.spendwiz.app.Database.money.TransactionType
import com.spendwiz.app.R

val IncomeColor @Composable get() = MaterialTheme.colorScheme.primary // Or a custom green
val ExpenseColor @Composable get() = MaterialTheme.colorScheme.error // Standard for negative/errors

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
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                Text(item.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Row {
                    Text(item.date, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = item.bankName?.let { "(${it})" } ?: "(Cash Payment)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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
                        TransactionType.INCOME -> IncomeColor
                        TransactionType.EXPENSE -> ExpenseColor
                        else -> MaterialTheme.colorScheme.tertiary
                    },
                    fontSize = 14.sp,
                    textAlign = TextAlign.End
                )

                Text(
                    text = formatTimeTo12Hr(item.time),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}