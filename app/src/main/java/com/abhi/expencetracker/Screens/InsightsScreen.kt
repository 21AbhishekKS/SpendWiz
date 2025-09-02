package com.abhi.expencetracker.Screens

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.abhi.expencetracker.Database.money.Money
import com.abhi.expencetracker.Database.money.TransactionType
import com.abhi.expencetracker.R
import com.abhi.expencetracker.ViewModels.AddScreenViewModel
import com.abhi.expencetracker.helper.OnBoarding.LoaderIntro
import com.abhi.expencetracker.navigation.Routes
import com.abhi.expencetracker.utils.BlueCircularLoader
import com.abhi.expencetracker.utils.ExpenseDonutChartByMonth
import com.abhi.expencetracker.utils.MoneyItem1
import com.abhi.expencetracker.utils.PieChart
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InsightsScreen(
    viewModel: AddScreenViewModel = viewModel(),
    navController1: NavHostController
) {
    val today = remember { LocalDate.now() }
    var selectedYear by rememberSaveable { mutableStateOf(today.year) }
    var selectedMonthIndex by rememberSaveable { mutableStateOf(today.monthValue - 1) }

    val listOfMonth = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val selectedMonth = listOfMonth[selectedMonthIndex]
    val selectedMonthInNum = String.format("%02d", selectedMonthIndex + 1)

    val context = LocalContext.current
    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, _ ->
                selectedYear = year
                selectedMonthIndex = month
            },
            selectedYear,
            selectedMonthIndex,
            1
        ).apply {
            // Hide the day spinner
            val daySpinnerId =
                context.resources.getIdentifier("android:id/day", null, null)
            val datePicker =
                this.datePicker.findViewById<View>(
                    context.resources.getIdentifier("android:id/datePicker", null, null)
                )
            datePicker?.findViewById<View>(daySpinnerId)?.visibility = View.GONE
        }
    }

    val moneyListLive = viewModel.moneyDao
        .getTransactionsByMonthAndYear(selectedMonthInNum, selectedYear.toString())
        .observeAsState(initial = emptyList())

    val moneyList = remember(moneyListLive.value) { moneyListLive.value.asReversed() }

    val groupedByDate: List<Pair<String, List<Money>>> =
        remember(moneyList) {
            moneyList.groupBy { it.date ?: "" }
                .toList()
                .sortedByDescending { it.first }
        }

    val monthlyTotals by remember(moneyList) {
        derivedStateOf {
            var spent = 0.0
            var received = 0.0
            for (t in moneyList) {
                val amt = parseAmount(t.amount)
                when (t.type) {
                    TransactionType.EXPENSE -> spent += amt
                    TransactionType.INCOME -> received += amt
                    else -> Unit
                }
            }
            spent to received
        }
    }
    val (monthlySpent, monthlyReceived) = monthlyTotals

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            modifier = Modifier.height(40.dp),
            navigationIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    //modifier = Modifier.padding(start = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()          // <- fills the 48.dp so center works
                            .padding(start = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            Icons.Rounded.KeyboardArrowLeft,
                            contentDescription = "Previous",
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    if (selectedMonthIndex == 0) {
                                        selectedMonthIndex = 11
                                        selectedYear--
                                    } else {
                                        selectedMonthIndex--
                                    }
                                }
                        )

                        // 📅 Month / Year
                        Text(
                            text = "${selectedMonth.take(3)} ${selectedYear % 100}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable { datePickerDialog.show() }
                                .padding(horizontal = 4.dp)
                        )

                        // ➡️ Next
                        Icon(
                            Icons.Rounded.KeyboardArrowRight,
                            contentDescription = "Next",
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    if (selectedMonthIndex == 11) {
                                        selectedMonthIndex = 0
                                        selectedYear++
                                    } else {
                                        selectedMonthIndex++
                                    }
                                }
                        )
                    }
                }
            },
            title = { },
            actions = {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        )
        // 🔹 Charts
        if (monthlyReceived != 0.0 || monthlySpent != 0.0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 220.dp, max = 280.dp)
                    .padding(horizontal = 10.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> PieChart(
                            spent = monthlySpent,
                            earned = monthlyReceived,
                            modifier = Modifier.fillMaxSize()
                        )
                        1 -> ExpenseDonutChartByMonth(
                            viewModel = viewModel,
                            month = selectedMonthInNum,
                            year = selectedYear.toString(),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Left arrow for pager
                if (pagerState.currentPage > 0) {
                    IconButton(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                        modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.right_arrow),
                            contentDescription = "Previous Chart",
                            modifier = Modifier.size(28.dp).rotate(180f)
                        )
                    }
                }
                // Right arrow for pager
                if (pagerState.currentPage < 1) {
                    IconButton(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.right_arrow),
                            contentDescription = "Next Chart",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Pager indicators
            Row(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(2) { index ->
                    val selected = pagerState.currentPage == index
                    Surface(
                        modifier = Modifier.padding(4.dp).size(if (selected) 10.dp else 8.dp),
                        shape = CircleShape,
                        color = if (selected) Color(0xFF1565C0) else Color(0xFFBDBDBD)
                    ) {}
                }
            }
        }

        // 🔹 Transactions List / Loader
        when {
            moneyListLive.value.isEmpty() && monthlySpent == 0.0 && monthlyReceived == 0.0 -> {
                BlueCircularLoader(Modifier)
            }
            moneyList.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "No transactions found",
                        modifier = Modifier
                            .background(Color(0xFF424242))
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        color = Color.White
                    )
                    LoaderIntro(
                        modifier = Modifier.fillMaxSize(0.8f),
                        image = R.raw.a8
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().background(Color.White),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    groupedByDate.forEach { (date, itemsForDate) ->
                        stickyHeader(key = "header_$date") {
                            Text(
                                text = date,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                        items(
                            items = itemsForDate,
                            key = { it.id ?: "${it.description}_${it.amount}_${it.date}" }
                        ) { item ->
                            MoneyItem1(item = item) {
                                navController1.navigate(
                                    Routes.UpdateScreen.route +
                                            "?description=${enc(item.description)}" +
                                            "&amount=${item.amount}" +
                                            "&id=${item.id}" +
                                            "&type=${enc(item.type.toString())}" +
                                            "&category=${enc(item.category ?: "")}" +
                                            "&subCategory=${enc(item.subCategory ?: "")}" +
                                            "&date=${enc(item.date ?: "")}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helpers
private fun enc(s: String): String =
    URLEncoder.encode(s, StandardCharsets.UTF_8.toString()).replace("+", "%20")

private fun parseAmount(amountAny: Any?): Double =
    when (amountAny) {
        is Number -> amountAny.toDouble()
        is String -> amountAny.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }
