package com.spendwiz.app.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendwiz.app.Database.money.Money
import com.spendwiz.app.Database.money.TransactionType

val IncomeLineColor @Composable get() = Color(0xFF2ECC71)
val ExpenseLineColor @Composable get() = Color(0xFFE74C3C)
val TransferLineColor @Composable get() = Color(0xFF3498DB)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MoneyItemWithLongPress(
    item: Money,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val lineColor = when (item.type) {
        TransactionType.INCOME -> IncomeLineColor
        TransactionType.EXPENSE -> ExpenseLineColor
        TransactionType.TRANSFER -> TransferLineColor
    }

    val interactionSource = remember { MutableInteractionSource() }
    val cardShape = RoundedCornerShape(12.dp)


    val scale = animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .scale(scale.value)
            .border(
                width = if (selected) 1.dp else 0.5.dp,
                color = if (selected)
                    lineColor
                else
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                shape = cardShape
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        shape = cardShape,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (selected) 8.dp else 4.dp),
        colors = CardDefaults.cardColors(
            containerColor =  MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(visible = selected) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 4.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(lineColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Colored vertical bar on the left (shifts right when checkmark is shown)
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .clip(
                        RoundedCornerShape(
                            topStart = 12.dp,
                            bottomStart = 12.dp
                        )
                    )
                    .background(lineColor)
            )

            // Main content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.description,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.date} • ${item.bankName ?: "Cash"}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    val amountText = when (item.type) {
                        TransactionType.INCOME -> "+ ${item.amount}"
                        TransactionType.EXPENSE -> "- ${item.amount}"
                        TransactionType.TRANSFER -> item.amount
                    }
                    val amountColor = when (item.type) {
                        TransactionType.INCOME -> IncomeLineColor
                        TransactionType.EXPENSE -> ExpenseLineColor
                        TransactionType.TRANSFER -> TransferLineColor
                    }

                    Text(
                        text = amountText.toString(),
                        color = amountColor,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatTimeTo12Hr(item.time),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
