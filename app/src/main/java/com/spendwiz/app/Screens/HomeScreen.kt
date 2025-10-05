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



