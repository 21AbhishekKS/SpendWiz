package com.abhi.expencetracker.Screens

import android.app.AlertDialog
import android.content.Context
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
import android.widget.FrameLayout
import android.widget.NumberPicker
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import androidx.navigation.NavController
import com.abhi.expencetracker.navigation.Routes
import java.time.DayOfWeek
import java.time.Year
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Annual(viewModel: AddScreenViewModel , navController: NavController) {
    var selectedYear by remember { mutableStateOf(LocalDate.now().year.toString()) }

    // Data
    val yearlyData by viewModel.getYearlyData(selectedYear).observeAsState(emptyList())
    val dayStatuses by viewModel.getDayStatusesForYear(selectedYear.toInt()).observeAsState(emptyMap())

    // Smooth loader for 1 second
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(selectedYear) {
        isLoading = true
        kotlinx.coroutines.delay(1000) // wait exactly 1 sec
        isLoading = false
    }

    val context = LocalContext.current
    val months = remember {
        listOf("Jan","Feb","Mar","Apr","May","Jun",
            "Jul","Aug","Sep","Oct","Nov","Dec")
    }

    // Pre-compute lists only after data loads
    val (incomeList, expenseList) = remember(yearlyData) {
        val inc = DoubleArray(12) { 0.0 }
        val exp = DoubleArray(12) { 0.0 }
        for (summary in yearlyData) {
            val idx = (summary.month.toIntOrNull() ?: 1) - 1
            if (idx in 0..11) {
                inc[idx] = summary.totalIncome
                exp[idx] = summary.totalExpense
            }
        }
        Pair(inc.toList(), exp.toList())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            // Loader Screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else {
            // Actual UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Year selector
                YearSelector(
                    year = selectedYear.toInt(),
                    onMonthChange = { newMonthIndex, newYear ->
                        selectedYear = newYear.toString()
                    }
                )

                // Bar chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    BarChart(
                        chartParameters = listOf(
                            BarParameters("Income", incomeList, Color(0xFF4CAF50)),
                            BarParameters("Expense", expenseList, Color(0xFFF44336))
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

                Spacer(modifier = Modifier.height(16.dp))

                val totalIncome = incomeList.sum()
                val totalExpense = expenseList.sum()

                // Bottom Summary Buttons
                TotalSummaryButtons(
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    onIncomeClick = {
                        navController.navigate(Routes.IncomeDetailsScreen.createRoute(selectedYear.toString()))
                    },
                    onExpenseClick = {
                        navController.navigate(Routes.ExpenseDetailsScreen.createRoute(selectedYear.toString()))
                    }
                )
                // Summary table
                MonthlySummaryTable(months, incomeList, expenseList)

                // Heatmap
                YearlyHeatmapCanvas(
                    year = selectedYear.toInt(),
                    data = dayStatuses,
                    squareSize = 14.dp,
                    spacing = 3.dp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

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

        // Use LazyRow for month-wise summary
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(end = 12.dp)
        ) {
            items(months.size) { index ->
                Column(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(BorderStroke(1.dp, Color(0xFFE0E0E0)), RoundedCornerShape(16.dp))
                        .width(90.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TableCell(months[index], Color(0xFF455A64), isHeader = true)
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

    val weeks: List<List<LocalDate>> = remember(year) {
        val start = LocalDate.of(year, 1, 1)
        val end = LocalDate.of(year, 12, 31)
        generateSequence(start) { it.plusDays(1) }
            .takeWhile { !it.isAfter(end) }
            .toList()
            .groupBy { it.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR) }
            .toSortedMap()
            .values.toList()
    }

    val colorMatrix: List<List<Color>> = remember(data, weeks) {
        val today = LocalDate.now()
        weeks.map { week ->
            DayOfWeek.values().map { dow ->
                val date = week.find { it.dayOfWeek == dow }
                val status = when {
                    date == null -> DayStatus.NoTransaction
                    date.isAfter(today) -> DayStatus.Future
                    else -> data[date] ?: DayStatus.NoTransaction
                }
                when (status) {
                    DayStatus.Categorized -> Color(0xFF298C31)    // Dark Green
                    DayStatus.NotCategorized -> Color(0xFF81C784) // Red
                    DayStatus.NoTransaction -> Color.LightGray
                    DayStatus.Future -> Color.LightGray       // Gray for future
                }
            }
        }
    }

    val columns = weeks.size
    val rows = DayOfWeek.values().size
    val widthPx = columns * squarePx + (columns - 1) * spacingPx
    val heightPx = rows * squarePx + (rows - 1) * spacingPx
    val widthDp = with(density) { widthPx.toDp() }

    val scrollState = rememberScrollState()

    Row(modifier = modifier.fillMaxWidth()) {
        // Fixed Day Labels
        Column(
            modifier = Modifier
                .width(20.dp)
                .padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            DayOfWeek.values().forEach { dow ->
                Text(
                    text = dow.getDisplayName(
                        java.time.format.TextStyle.NARROW, // first letter only
                        Locale.getDefault()
                    ),
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    modifier = Modifier.height(squareSize)
                )
            }
        }

        // Scrollable Heatmap + Legend
        Column(
            modifier = Modifier.horizontalScroll(scrollState)
        ) {
            // Month Labels Row
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(widthDp)
            ) {
                val firstDaysOfMonths = (1..12).map { m -> LocalDate.of(year, m, 1) }
                firstDaysOfMonths.forEach { monthStart ->
                    val weekIndex = weeks.indexOfFirst { week ->
                        week.any { it.month == monthStart.month && it.dayOfMonth == 1 }
                    }.takeIf { it >= 0 } ?: 0

                    val xPx = weekIndex * (squarePx + spacingPx)

                    Text(
                        text = monthStart.month.getDisplayName(
                            java.time.format.TextStyle.SHORT,
                            Locale.getDefault()
                        ),
                        modifier = Modifier
                            .offset(x = with(density) { xPx.toDp() }, y = 0.dp)
                            .padding(horizontal = 2.dp),
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Heatmap
            Box(
                modifier = Modifier
                    .height(with(density) { heightPx.toDp() })
                    .width(widthDp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = 2.dp.toPx()
                    colorMatrix.forEachIndexed { colIndex, rowColors ->
                        rowColors.forEachIndexed { rowIndex, color ->
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

            Spacer(modifier = Modifier.height(12.dp))

            // Legend (also scrollable)
            Row(
                modifier = Modifier.width(widthDp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LegendItem(color = Color(0xFF298C31), label = "Categorized")
                LegendItem(color = Color(0xFF81C784), label = "Not Categorized")
                LegendItem(color = Color.LightGray, label = "No Transaction")
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(color, shape = RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun YearSelector(
    year: Int,
    onMonthChange: (newMonthIndex: Int, newYear: Int) -> Unit
) {
    var selectedYear by remember { mutableStateOf(year) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            Icons.Rounded.KeyboardArrowLeft,
            contentDescription = "Previous Year",
            tint = Color.Black,
            modifier = Modifier
                .size(20.dp)
                .clickable {
                    selectedYear--
                    onMonthChange(0, selectedYear) // always pass monthIndex = 0
                }
        )

        Text(
            text = selectedYear.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Icon(
            Icons.Rounded.KeyboardArrowRight,
            contentDescription = "Next Year",
            tint = Color.Black,
            modifier = Modifier
                .size(20.dp)
                .clickable {
                    selectedYear++
                    onMonthChange(0, selectedYear) // always pass monthIndex = 0
                }
        )
    }
}

@Composable
fun TotalSummaryButtons(
    totalIncome: Double,
    totalExpense: Double,
    onIncomeClick: () -> Unit,
    onExpenseClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Income Button (Green)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .background(Color(0xFF4CAF50), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFF2E7D32), RoundedCornerShape(10.dp))
                .clickable { onIncomeClick() },   // ✅ Navigate on click
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Earned: ₹${"%.0f".format(totalIncome)}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Expense Button (Red)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .background(Color(0xFFF44336), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFD32F2F), RoundedCornerShape(10.dp))
                .clickable { onExpenseClick() },   // ✅ Navigate on click
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Spent: ₹${"%.0f".format(totalExpense)}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}




