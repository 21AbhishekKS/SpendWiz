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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.Year

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
        MonthlySummaryTable(
            months = months,
            incomeList = incomeList,
            expenseList = expenseList
        )

        Spacer(modifier = Modifier.height(16.dp))

        val dayStatuses by viewModel.getDayStatusesForYear(selectedYear.toInt())
            .observeAsState(emptyMap())

        YearlyHeatmapCanvas(
            year = selectedYear.toInt(),
            data = dayStatuses,
            squareSize = 14.dp,
            spacing = 3.dp,
            modifier = Modifier.padding(8.dp)
        )

    }}

@Composable
fun MonthlySummaryTable(
    months: List<String>,
    incomeList: List<Double>,
    expenseList: List<Double>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Fixed left column
        Column(
            modifier = Modifier
                .background(Color(0xFFFAFAFA), RoundedCornerShape(16.dp))
                .border(BorderStroke(1.dp, Color(0xFFE0E0E0)), RoundedCornerShape(16.dp))
                .width(100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TableCell("Month", Color.Black, isHeader = true)
            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            TableCell("Income", Color(0xFF388E3C), isHeader = true)
            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            TableCell("Expense", Color(0xFFD32F2F), isHeader = true)
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Scrollable month-wise columns
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
        ) {
            months.forEachIndexed { index, month ->
                Column(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(BorderStroke(1.dp, Color(0xFFE0E0E0)), RoundedCornerShape(16.dp))
                        .width(90.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TableCell(month, Color(0xFF455A64), isHeader = true)
                    Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                    TableCell("₹${"%.0f".format(incomeList[index])}", Color(0xFF388E3C))
                    Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                    TableCell("₹${"%.0f".format(expenseList[index])}", Color(0xFFD32F2F))
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
    }
}

@Composable
fun TableCell(
    text: String,
    color: Color,
    isHeader: Boolean = false
) {
    Text(
        text = text,
        color = color,
        fontSize = if (isHeader) 15.sp else 14.sp,
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

enum class DayStatus {
    Categorized, NotCategorized, NoTransaction, Future
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun YearlyHeatmapCanvas(
    year: Int,
    data: Map<LocalDate, DayStatus>,
    squareSize: Dp = 14.dp,
    spacing: Dp = 3.dp,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val squarePx = with(density) { squareSize.toPx() }
    val spacingPx = with(density) { spacing.toPx() }

    // Generate all days of year grouped into weeks
    val weeks by remember(year) {
        mutableStateOf(run {
            val start = LocalDate.of(year, 1, 1)
            val end = LocalDate.of(year, 12, 31)
            val allDays = generateSequence(start) { it.plusDays(1) }
                .takeWhile { !it.isAfter(end) }
                .toList()

            // group into weeks (columns)
            allDays.groupBy {
                it.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)
            }.toSortedMap().values.toList()
        })
    }

    val columns = weeks.size
    val rows = DayOfWeek.values().size // 7 days

    val widthPx = columns * squarePx + (columns - 1) * spacingPx
    val heightPx = rows * squarePx + (rows - 1) * spacingPx

    Box(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .height(with(density) { heightPx.toDp() })
            .fillMaxWidth()
    ) {
        Canvas(
            modifier = Modifier
                .width(with(density) { widthPx.toDp() })
                .height(with(density) { heightPx.toDp() })
        ) {
            val radius = 2.dp.toPx()
            val today = LocalDate.now()

            weeks.forEachIndexed { colIndex, weekDays ->
                DayOfWeek.values().forEachIndexed { rowIndex, dow ->
                    val date = weekDays.find { it.dayOfWeek == dow }
                    val status = when {
                        date == null -> DayStatus.NoTransaction
                        date.isAfter(today) -> DayStatus.Future
                        else -> data[date] ?: DayStatus.NoTransaction
                    }

                    val color = when (status) {
                        DayStatus.Categorized -> Color(0xFF006400) // Dark Green
                        DayStatus.NotCategorized -> Color(0xFFD32F2F) // Red
                        DayStatus.NoTransaction -> Color.LightGray // Light Green
                        DayStatus.Future -> Color.LightGray
                    }

                    val left = colIndex * (squarePx + spacingPx)
                    val top = rowIndex * (squarePx + spacingPx)
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(left, top),
                        size = Size(squarePx, squarePx),
                        cornerRadius = CornerRadius(radius, radius)
                    )
                }
            }
        }
    }
}


