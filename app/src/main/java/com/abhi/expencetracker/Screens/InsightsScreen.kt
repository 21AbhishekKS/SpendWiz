package com.abhi.expencetracker.Screens

import android.app.DatePickerDialog
import android.os.Build
import android.util.Log
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
import com.abhi.expencetracker.utils.ChipSelectionDialog
import com.abhi.expencetracker.utils.ExpenseDonutChartByMonth
import com.abhi.expencetracker.utils.MoneyItem1
import com.abhi.expencetracker.utils.MoneyItemWithLongPress
import com.abhi.expencetracker.utils.PieChart
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import androidx.compose.ui.unit.LayoutDirection
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
            val daySpinnerId = context.resources.getIdentifier("android:id/day", null, null)
            val datePicker = this.datePicker.findViewById<View>(
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

                CustomTopBar(
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    selectedMonthIndex = selectedMonthIndex,
                    selectedItems = selectedItems,
                    moneyList = moneyList,
                    datePickerDialog = datePickerDialog,
                    viewModel = viewModel,
                    onMonthChange = { newMonthIndex, newYear ->
                        selectedMonthIndex = newMonthIndex
                        selectedYear = newYear
                    }
                )

                Divider(color = Color.LightGray)

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

@Composable
fun CustomTopBar(
    selectedMonth: String,
    selectedYear: Int,
    selectedMonthIndex: Int,
    selectedItems: MutableList<String>,
    moneyList: List<Money>,
    datePickerDialog: DatePickerDialog,
    viewModel: AddScreenViewModel,
    onMonthChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var monthIndex by remember { mutableStateOf(selectedMonthIndex) }
    var year by remember { mutableStateOf(selectedYear) }

    var showChangeTypeDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showSubCategoryDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var selectedType by remember { mutableStateOf<TransactionType?>(null) }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedSubCategory by remember { mutableStateOf("") }

    val selectedTransactions = remember(selectedItems.size, moneyList) {
        moneyList.filter { selectedItems.contains(it.id.toString()) }
    }

    val isSameType = selectedTransactions.map { it.type }.distinct().size == 1
    val isSameCategory = selectedTransactions.map { it.category.ifBlank { "Others" } }.distinct().size == 1

    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.KeyboardArrowLeft,
                contentDescription = "Previous",
                tint = Color.Black,
                modifier = Modifier
                    .size(20.dp)
                    .clickable {
                        if (monthIndex == 0) {
                            monthIndex = 11
                            year--
                        } else {
                            monthIndex--
                        }
                        onMonthChange(monthIndex, year)
                    }
            )

            Text(
                text = "${selectedMonth.take(3)} ${year % 100}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier
                    .clickable { datePickerDialog.show() }
                    .padding(horizontal = 4.dp)
            )

            Icon(
                Icons.Rounded.KeyboardArrowRight,
                contentDescription = "Next",
                tint = Color.Black,
                modifier = Modifier
                    .size(20.dp)
                    .clickable {
                        if (monthIndex == 11) {
                            monthIndex = 0
                            year++
                        } else {
                            monthIndex++
                        }
                        onMonthChange(monthIndex, year)
                    }
            )
        }

        // ⋮ Menu Actions
        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                val enabled = selectedItems.isNotEmpty()

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = selectedItems.size == moneyList.size && moneyList.isNotEmpty(),
                                onCheckedChange = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select All")
                        }
                    },
                    onClick = {
                        if (selectedItems.size == moneyList.size) {
                            selectedItems.clear()
                        } else {
                            selectedItems.clear()
                            selectedItems.addAll(moneyList.map { it.id.toString() })
                        }
                    },
                    enabled = moneyList.isNotEmpty()
                )

                Divider()

                DropdownMenuItem(
                    text = { Text("Change Type") },
                    onClick = {
                        expanded = false
                        selectedType = null
                        showChangeTypeDialog = true
                    },
                    enabled = enabled
                )

                Divider()

                DropdownMenuItem(
                    text = { Text("Add Category") },
                    onClick = {
                        expanded = false
                        selectedCategory = ""
                        showCategoryDialog = true
                    },
                    enabled = enabled && isSameType
                )

                Divider()

                DropdownMenuItem(
                    text = { Text("Add Sub Category") },
                    onClick = {
                        expanded = false
                        selectedSubCategory = ""
                        showSubCategoryDialog = true
                    },
                    enabled = enabled && isSameType && isSameCategory
                )

                Divider()

                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        expanded = false
                        showDeleteDialog = true
                    },
                    enabled = enabled
                )
            }
        }
    }

    // 🔥 Dialogs (same as before, just moved below)
    if (showChangeTypeDialog) {
        ChipSelectionDialog(
            title = "Select Type",
            type = selectedType ?: TransactionType.EXPENSE,
            options = listOf(
                TransactionType.INCOME.name,
                TransactionType.EXPENSE.name,
                TransactionType.TRANSFER.name
            ),
            selectedOption = selectedType?.name ?: "",
            onOptionSelected = { selected -> selectedType = TransactionType.valueOf(selected) },
            onDismiss = { showChangeTypeDialog = false },
            onConfirm = {
                if (selectedType != null) {
                    val ids = selectedItems.map { it.toInt() }
                    viewModel.updateTransactionType(ids, selectedType!!)
                }
                showChangeTypeDialog = false
            }
        )
    }

    if (showCategoryDialog) {
        val typeForCats = selectedTransactions.firstOrNull()?.type ?: TransactionType.EXPENSE
        val categories = when (typeForCats) {
            TransactionType.INCOME -> listOf("Salary", "Business", "Investments", "Others")
            TransactionType.EXPENSE -> listOf("Food", "Transport", "Shopping", "Bills", "Misc", "Others")
            TransactionType.TRANSFER -> listOf("Bank Transfer", "UPI", "Others")
        }

        ChipSelectionDialog(
            title = "Select Category",
            type = typeForCats,
            options = categories,
            selectedOption = selectedCategory,
            onOptionSelected = { selectedCategory = it },
            onDismiss = { showCategoryDialog = false },
            onConfirm = {
                if (selectedCategory.isNotEmpty()) {
                    val ids = selectedItems.map { it.toInt() }
                    viewModel.updateCategory(ids, selectedCategory)
                }
                showCategoryDialog = false
            }
        )
    }

    if (showSubCategoryDialog && isSameType && isSameCategory) {
        val typeForSubs = selectedTransactions.firstOrNull()?.type ?: TransactionType.EXPENSE
        val category = selectedTransactions.firstOrNull()?.category ?: ""

        val expenseMap = mapOf(
            "Food" to listOf("Breakfast", "Lunch", "Dinner", "Snacks", "Groceries"),
            "Transport" to listOf("Bus", "Train", "Taxi", "Fuel", "Flight"),
            "Shopping" to listOf("Clothes", "Electronics", "Accessories", "Gifts"),
            "Bills" to listOf("Electricity", "Internet", "Water", "Mobile"),
            "Misc" to listOf("Donation", "Entertainment")
        )
        val incomeMap = mapOf(
            "Salary" to listOf("Monthly", "Bonus", "Overtime"),
            "Business" to listOf("Sales", "Services"),
            "Investments" to listOf("Stocks", "Crypto", "Bonds")
        )
        val transferMap = mapOf(
            "Bank Transfer" to listOf("Same Bank", "Other Bank"),
            "UPI" to listOf("Google Pay", "PhonePe", "Paytm")
        )

        val subcategories = when (typeForSubs) {
            TransactionType.INCOME -> incomeMap[category].orEmpty()
            TransactionType.EXPENSE -> expenseMap[category].orEmpty()
            TransactionType.TRANSFER -> transferMap[category].orEmpty()
        }

        ChipSelectionDialog(
            title = "Select Sub Category",
            type = typeForSubs,
            options = subcategories,
            selectedOption = selectedSubCategory,
            onOptionSelected = { selectedSubCategory = it },
            onDismiss = { showSubCategoryDialog = false },
            onConfirm = {
                if (selectedSubCategory.isNotEmpty()) {
                    val ids = selectedItems.map { it.toInt() }
                    viewModel.updateSubCategory(ids, selectedSubCategory)
                }
                showSubCategoryDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Transactions") },
            text = { Text("Are you sure you want to delete ${selectedItems.size} transactions?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val ids = selectedItems.map { it.toInt() }
                        viewModel.deleteTransactions(ids)
                        showDeleteDialog = false
                        selectedItems.clear()
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
