package com.abhi.expencetracker.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.expencetracker.Database.money.Money
import com.abhi.expencetracker.Database.money.TransactionType
import com.abhi.expencetracker.R

@Composable
fun MoneyItem1(
    item: Money,
    onClick: () -> Unit
) {
    Card(
        Modifier
            .background(Color.White)
            .fillMaxWidth()
            .padding(5.dp)
            .clickable { onClick() }
    ) {
        Row(
            Modifier
                .background(Color.White)
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side (icon + text details)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
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
                        .padding(end = 10.dp)
                        .size(40.dp)
                )

                Column {
                    Text(
                        text = item.description,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Black
                    )
                    Text(text = "Date: ${item.date}", color = Color.Black, fontSize = 12.sp)

                    // UPI Ref in one line only
                    item.upiRefNo?.let {
                        Text(
                            text = "UPI Ref: $it",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.Black,
                            fontSize = 12.sp
                        )
                    }

                    Text(text = item.bankName ?: "Cash Payment", color = Color.Black, fontSize = 12.sp)
                }
            }

            // Right side (aligned fixed box for amount)
            Box(
                modifier = Modifier.width(100.dp), // fixed width ensures alignment
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = when (item.type) {
                        TransactionType.INCOME -> "+ ${item.amount}"
                        TransactionType.EXPENSE -> "- ${item.amount}"
                        else -> "${item.amount}"
                    },
                    color = when (item.type) {
                        TransactionType.INCOME -> Color(0xFF5ABB5E)
                        TransactionType.EXPENSE -> Color(0xFFF03B2E)
                        else -> Color(0xFF4B62E4)
                    },
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
