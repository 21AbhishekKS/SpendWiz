package com.abhi.expencetracker.Screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aay.compose.barChart.BarChart
import com.aay.compose.barChart.model.BarParameters
import com.abhi.expencetracker.ViewModels.AddScreenViewModel
import java.time.LocalDate
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Annual(viewModel: AddScreenViewModel) {
    var selectedYear by remember { mutableStateOf(LocalDate.now().year.toString()) }
    val yearlyData by viewModel.getYearlyData(selectedYear).observeAsState(emptyList())
    val context = LocalContext.current

    val months = listOf(
        "Jan","Feb","Mar","Apr","May","Jun",
        "Jul","Aug","Sep","Oct","Nov","Dec"
    )

    // Prepare data
    val incomeList = MutableList(12) { 0.0 }
    val expenseList = MutableList(12) { 0.0 }
    yearlyData.forEach { summary ->
        val monthIndex = summary.month.toInt() - 1
        incomeList[monthIndex] = summary.totalIncome
        expenseList[monthIndex] = summary.totalExpense
    }

    // Totals for summary
    val totalIncome = incomeList.sum()
    val totalExpense = expenseList.sum()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Year selector with DatePickerDialog
        Text(
            text = "Year: $selectedYear",
            modifier = Modifier
                .clickable {
                    val currentYear = LocalDate.now().year
                    val picker = android.app.DatePickerDialog(
                        context,
                        { _, year, _, _ -> selectedYear = year.toString() },
                        selectedYear.toInt(),
                        0,
                        1
                    )
                    // hide month/day, show only year
                    picker.datePicker.findViewById<View>(
                        context.resources.getIdentifier("android:id/day", null, null)
                    )?.visibility = View.GONE
                    picker.datePicker.findViewById<View>(
                        context.resources.getIdentifier("android:id/month", null, null)
                    )?.visibility = View.GONE
                    picker.datePicker.minDate = LocalDate.of(2015, 1, 1)
                        .atStartOfDay(java.time.ZoneId.systemDefault())
                        .toInstant().toEpochMilli()
                    picker.datePicker.maxDate = LocalDate.of(currentYear, 12, 31)
                        .atStartOfDay(java.time.ZoneId.systemDefault())
                        .toInstant().toEpochMilli()
                    picker.show()
                }
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bar chart fixed at 350.dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
        ) {
            BarChart(
                chartParameters = listOf(
                    BarParameters(
                        dataName = "Income",
                        data = incomeList,
                        barColor = Color(0xFF4CAF50) // green
                    ),
                    BarParameters(
                        dataName = "Expense",
                        data = expenseList,
                        barColor = Color(0xFFF44336) // red
                    )
                ),
                gridColor = Color.DarkGray,
                xAxisData = months,
                isShowGrid = true,
                animateChart = true,
                yAxisRange = 10,
                barWidth = 14.dp,
                xAxisStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.W400),
                yAxisStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.W400),
                spaceBetweenBars = 0.dp,
                spaceBetweenGroups = 8.dp,
            )
        }

        // Below the summary row

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Fixed left column
            Column(
                modifier = Modifier
                    .background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .width(90.dp), // fixed width
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Month",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Income",
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Expense",
                    color = Color(0xFFF44336),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Scrollable months section
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
            ) {
                months.forEachIndexed { index, month ->
                    Column(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .width(80.dp), // fixed width for each month column
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Month header
                        Text(
                            text = month,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        // Income
                        Text(
                            text = "₹${"%.0f".format(incomeList[index])}",
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        // Expense
                        Text(
                            text = "₹${"%.0f".format(expenseList[index])}",
                            color = Color(0xFFF44336),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }

    }}



