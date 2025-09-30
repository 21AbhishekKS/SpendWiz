package com.spendwiz.app.Screens

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.spendwiz.app.ViewModels.AddScreenViewModel
import com.spendwiz.app.utils.TransactionList
import com.spendwiz.app.Database.money.TransactionType
import kotlin.math.roundToInt
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendwiz.app.Database.money.CategoryExpense


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    viewModel: AddScreenViewModel,
    navController1: NavHostController
) {

    val monthlySummary by viewModel.currentMonthSummary.observeAsState()
    val monthlyIncome = monthlySummary?.totalIncome ?: 0.0
    val monthlyExpense = monthlySummary?.totalExpense ?: 0.0
    val monthlySavings = monthlyIncome - monthlyExpense

    val todayMoneyList by viewModel.todayMoneyList.observeAsState()
    var totalMoneySpent by remember { mutableStateOf(0.0) }
    var totalMoneyEarned by remember { mutableStateOf(0.0) }

    val context = LocalContext.current
    LaunchedEffect(true) {
        viewModel.runSmsImportOnce(context)
    }
    LaunchedEffect(todayMoneyList) {
        totalMoneySpent = 0.0
        totalMoneyEarned = 0.0
        todayMoneyList?.forEach { transaction ->
            when (transaction.type) {
                TransactionType.EXPENSE -> totalMoneySpent += transaction.amount
                TransactionType.INCOME -> totalMoneyEarned += transaction.amount
                else -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CardItemHome(
            income = formatCurrency(totalMoneyEarned),
            expenses = formatCurrency(totalMoneySpent)
        )
        Box(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 10.dp)) {
            SavingGoalsCard(
                balance = monthlySavings,
                monthlyExpense =monthlyExpense,
                monthlySavings,
                monthlyIncome = monthlyIncome,
                progressColor = Color.LightGray,
                trackColor = Color.Gray

            )
        }

        Text(
            text = "Today's Transactions",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Need to update a transaction? Just click on it!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        TransactionList(
            todayMoneyList?.reversed(),
            navController = navController1,
            viewModel
        )
    }
}

@Composable
fun CardItemHome(
    income: String,
    expenses: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(15.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    0.0f to Color(0xFFF35979),
                    1.0f to Color(0xFF6A82FB)
                )
            )
            .padding(15.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Column {
            Text(
                text = "Today's Financial Snapshot",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White // stays white because gradient is dark
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = income,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
                Column {
                    Text(
                        text = "Expenses",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = expenses,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Helper function to format currency values
private fun formatCurrency(amount: Double): String {
    return if (amount == amount.roundToInt().toDouble()) {
        "₹${amount.roundToInt()}" // Show as integer if no decimal places
    } else {
        "₹${"%.2f".format(amount)}" // Show with 2 decimal places
    }
}


// Your TicketShape remains the same as it's theme-agnostic.
class TicketShape(private val cornerRadius: Float, private val cutOutRadius: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(cornerRadius, 0f)
            lineTo(size.width - cornerRadius, 0f)
            arcTo(
                rect = Rect(Offset(size.width - 2 * cornerRadius, 0f), Size(2 * cornerRadius, 2 * cornerRadius)),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(size.width, size.height / 2 - cutOutRadius)
            arcTo(
                rect = Rect(
                    center = Offset(size.width, size.height / 2),
                    radius = cutOutRadius
                ),
                startAngleDegrees = -90f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            lineTo(size.width, size.height - cornerRadius)
            arcTo(
                rect = Rect(Offset(size.width - 2 * cornerRadius, size.height - 2 * cornerRadius), Size(2 * cornerRadius, 2 * cornerRadius)),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(cornerRadius, size.height)
            arcTo(
                rect = Rect(Offset(0f, size.height - 2 * cornerRadius), Size(2 * cornerRadius, 2 * cornerRadius)),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(0f, size.height / 2 + cutOutRadius)
            arcTo(
                rect = Rect(
                    center = Offset(0f, size.height / 2),
                    radius = cutOutRadius
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            lineTo(0f, cornerRadius)
            arcTo(
                rect = Rect(Offset(0f, 0f), Size(2 * cornerRadius, 2 * cornerRadius)),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun DashedDivider(
    modifier: Modifier = Modifier,
    color: Color,
    thickness: Dp = 2.dp, // Define thickness as Dp
    intervals: FloatArray = floatArrayOf(20f, 10f)
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness) // Set the canvas height to the desired thickness
    ) {
        // Convert Dp to pixel value
        val strokeWidthInPx = thickness.toPx()

        // Center the line vertically within the Canvas
        val y = size.height / 2

        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            pathEffect = PathEffect.dashPathEffect(intervals, 0f),
            strokeWidth = strokeWidthInPx // Use the pixel value for strokeWidth
        )
    }
}

object SavingCardDefaults {
    @Composable
    fun cardBrush(): Brush {
        // This linear gradient goes from the top-left (light blue/purple)
        // to the bottom-right (darker blue), matching the image.
        return Brush.linearGradient(
            colors = listOf(Color(0xFF4D6DE3), Color(0xFF233E9A)),
            start = Offset.Zero, // Top-left
            end = Offset.Infinite // Bottom-right
        )
    }

    val contentColor: Color = Color.White
}

@Composable
fun SavingGoalsCard(
    balance: Double,
    monthlyExpense: Double,
    currentSavings: Double,
    monthlyIncome: Double,
    modifier: Modifier = Modifier,
    cardBrush: Brush = SavingCardDefaults.cardBrush(),
    contentColor: Color = SavingCardDefaults.contentColor,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    // Derive secondary colors from the main content color for consistency.
    val primaryTextColor = contentColor
    val secondaryTextColor = contentColor.copy(alpha = 0.8f)
    val dividerColor = contentColor.copy(alpha = 0.5f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = TicketShape(cornerRadius = 20f, cutOutRadius = 25f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.99f) // Workaround for brush transparency bug
                .background(cardBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                // Top Section: Account Balance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Account balance this month",
                            color = secondaryTextColor, // Use theme-aware color
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = "${"%,.2f".format(balance)} ₹",
                                color = primaryTextColor, // Use theme-aware color
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${"%,.0f".format(monthlyIncome)} ₹",
                                    color = primaryTextColor, // Use theme-aware color
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Increase",
                                    tint = primaryTextColor, // Use theme-aware color
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${"%,.0f".format(monthlyExpense)} ₹",
                                    color = primaryTextColor, // Use theme-aware color
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Increase",
                                    tint = primaryTextColor, // Use theme-aware color
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            }
                        }
                    }

                }

                DashedDivider(color = Color.White) // Pass theme-aware color

                // Bottom Section: Savings Progress
                Column {
                    val progress = if (monthlyIncome > 0) (currentSavings / monthlyIncome).toFloat() else 0f

                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = progressColor,
                        trackColor = trackColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val progressPercentage = (progress * 100).coerceIn(0f, 100f).toInt()
                    Text(
                        text = "You have saved $progressPercentage% of your monthly income.",
                        color = secondaryTextColor, // Use theme-aware color
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

