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
import com.abhi.expencetracker.utils.MoneyItemWithLongPress
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

    val selectedItems = remember { mutableStateListOf<String>() }

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

    val moneyList = moneyListLive.value.asReversed()

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
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
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
                if (moneyList.isNotEmpty() && selectedItems.size > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedItems.size == moneyList.size && selectedItems.isNotEmpty(),
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    selectedItems.clear()
                                    selectedItems.addAll(moneyList.map { it.id.toString() })
                                } else {
                                    selectedItems.clear()
                                }
                            }
                        )
                    }
                }

                var showChangeTypeDialog by remember { mutableStateOf(false) }
                var showCategoryDialog by remember { mutableStateOf(false) }
                var showSubCategoryDialog by remember { mutableStateOf(false) }
                var showDeleteDialog by remember { mutableStateOf(false) }

                val selectedTransactions = remember(selectedItems, moneyList) {
                    moneyList.filter { selectedItems.contains(it.id.toString()) }
                }

                var expanded by remember { mutableStateOf(false) }

                IconButton(onClick = { expanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More",
                        modifier = Modifier.size(22.dp)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    val enabled = selectedItems.isNotEmpty()

                    DropdownMenuItem(
                        text = { Text("Change Type") },
                        onClick = {
                            expanded = false
                            showChangeTypeDialog = true
                        },
                        enabled = enabled
                    )
                    DropdownMenuItem(
                        text = { Text("Add Category") },
                        onClick = {
                            expanded = false
                            showCategoryDialog = true
                        },
                        enabled = enabled
                    )
                    DropdownMenuItem(
                        text = { Text("Add Sub Category") },
                        onClick = {
                            expanded = false
                            showSubCategoryDialog = true
                        },
                        // enabled only if all selected items share the same category
                        enabled = enabled && selectedTransactions.map { it.category }.distinct().size == 1
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            expanded = false
                            showDeleteDialog = true
                        },
                        enabled = enabled
                    )
                }

// --- Change Type Dialog ---
                if (showChangeTypeDialog) {
                    AlertDialog(
                        onDismissRequest = { showChangeTypeDialog = false },
                        title = { Text("Change Type") },
                        text = {
                            Column {
                                listOf(
                                    "Income" to TransactionType.INCOME,
                                    "Expense" to TransactionType.EXPENSE,
                                    "Transfer" to TransactionType.TRANSFER
                                ).forEach { (label, typeEnum) ->
                                    Text(
                                        text = label,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedTransactions.forEach { tx ->
                                                    viewModel.updateMoney(
                                                        id = tx.id,
                                                        amount = parseAmount(tx.amount),
                                                        description = tx.description ?: "",
                                                        type = typeEnum,
                                                        category = tx.category ?: "Others",
                                                        subCategory = tx.subCategory ?: "General",
                                                        date = tx.date ?: ""
                                                    )
                                                }
                                                showChangeTypeDialog = false
                                                selectedItems.clear()
                                            }
                                            .padding(12.dp)
                                    )
                                }
                            }
                        },
                        confirmButton = {}
                    )
                }
// --- Add Category Dialog ---
                if (showCategoryDialog) {
                    val incomeCategories = listOf("Salary", "Business", "Investments", "Others")
                    val expenseCategories = listOf("Food", "Transport", "Shopping", "Bills", "Misc", "Others")
                    val transferCategories = listOf("Bank Transfer", "UPI", "Others")

                    // detect type of first selected transaction
                    val type = selectedTransactions.firstOrNull()?.type ?: TransactionType.EXPENSE
                    val categories = when (type) {
                        TransactionType.INCOME -> incomeCategories
                        TransactionType.EXPENSE -> expenseCategories
                        else -> transferCategories
                    }

                    AlertDialog(
                        onDismissRequest = { showCategoryDialog = false },
                        title = { Text("Select Category") },
                        text = {
                            Column {
                                categories.forEach { cat ->
                                    Text(
                                        text = cat,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedTransactions.forEach { tx ->
                                                    viewModel.updateMoney(
                                                        id = tx.id,
                                                        amount = parseAmount(tx.amount),
                                                        description = tx.description ?: "",
                                                        type = tx.type,
                                                        category = cat,
                                                        subCategory = tx.subCategory ?: "General",
                                                        date = tx.date ?: ""
                                                    )
                                                }
                                                showCategoryDialog = false
                                                selectedItems.clear()
                                            }
                                            .padding(12.dp)
                                    )
                                }
                            }
                        },
                        confirmButton = {}
                    )
                }

// --- Add Sub Category Dialog ---
                if (showSubCategoryDialog) {
                    val expenseSubCategoryMap = mapOf(
                        "Food" to listOf("Breakfast", "Lunch", "Dinner", "Snacks", "Groceries"),
                        "Transport" to listOf("Bus", "Train", "Taxi", "Fuel", "Flight"),
                        "Shopping" to listOf("Clothes", "Electronics", "Accessories", "Gifts"),
                        "Bills" to listOf("Electricity", "Internet", "Water", "Mobile"),
                        "Misc" to listOf("Donation", "Entertainment")
                    )
                    val incomeSubCategoryMap = mapOf(
                        "Salary" to listOf("Monthly", "Bonus", "Overtime"),
                        "Business" to listOf("Sales", "Services"),
                        "Investments" to listOf("Stocks", "Crypto", "Bonds")
                    )
                    val transferSubCategoryMap = mapOf(
                        "Bank Transfer" to listOf("Same Bank", "Other Bank"),
                        "UPI" to listOf("Google Pay", "PhonePe", "Paytm")
                    )

                    val category = selectedTransactions.firstOrNull()?.category ?: ""
                    val type = selectedTransactions.firstOrNull()?.type ?: TransactionType.EXPENSE
                    val subcategories = when (type) {
                        TransactionType.INCOME -> incomeSubCategoryMap[category].orEmpty()
                        TransactionType.EXPENSE -> expenseSubCategoryMap[category].orEmpty()
                        else -> transferSubCategoryMap[category].orEmpty()
                    }

                    AlertDialog(
                        onDismissRequest = { showSubCategoryDialog = false },
                        title = { Text("Select Sub Category") },
                        text = {
                            Column {
                                subcategories.forEach { sub ->
                                    Text(
                                        text = sub,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedTransactions.forEach { tx ->
                                                    viewModel.updateMoney(
                                                        id = tx.id,
                                                        amount = parseAmount(tx.amount),
                                                        description = tx.description ?: "",
                                                        type = tx.type,
                                                        category = tx.category ?: "Others",
                                                        subCategory = sub,
                                                        date = tx.date ?: ""
                                                    )
                                                }
                                                showSubCategoryDialog = false
                                                selectedItems.clear()
                                            }
                                            .padding(12.dp)
                                    )
                                }
                            }
                        },
                        confirmButton = {}
                    )
                }


                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete Transactions") },
                        text = { Text("Are you sure you want to delete ${selectedItems.size} transactions?") },
                        confirmButton = {
                            TextButton(onClick = {
                                selectedTransactions.forEach { tx ->
                                    viewModel.deleteMoney(tx.id)
                                }
                                showDeleteDialog = false
                                selectedItems.clear()
                            }) {
                                Text("Delete")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Cancel")
                            }
                        }
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
        }
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
                        item(key = "header_$date") {
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
                            key = { it.id }
                        ) { item ->
                            val itemId: String = (item.id.toString())

                            MoneyItemWithLongPress(
                                item = item,
                                selected = selectedItems.contains(itemId),
                                onClick = {
                                    if (selectedItems.isEmpty()) {
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
                                    } else {
                                        if (selectedItems.contains(itemId)) {
                                            selectedItems.remove(itemId)
                                        } else {
                                            selectedItems.add(itemId)
                                        }
                                    }
                                },
                                onLongClick = {
                                    if (selectedItems.contains(itemId)) {
                                        selectedItems.remove(itemId)
                                    } else {
                                        selectedItems.add(itemId)
                                    }
                                }
                            )
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
