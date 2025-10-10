package com.spendwiz.app.Screens

import android.app.DatePickerDialog
import android.content.res.Configuration
import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.spendwiz.app.Ads.AdmobNativeAdCardForInsightScreen
import com.spendwiz.app.Database.money.Money
import com.spendwiz.app.Database.money.TransactionType
import com.spendwiz.app.R
import com.spendwiz.app.ViewModels.AddScreenViewModel
import com.spendwiz.app.navigation.Routes
import com.spendwiz.app.utils.BlueCircularLoader
import com.spendwiz.app.utils.ChipSelectionDialog
import com.spendwiz.app.utils.ExpenseDonutChartByMonth
import com.spendwiz.app.utils.MoneyItemWithLongPress
import com.spendwiz.app.utils.PieChart
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import org.threeten.bp.LocalDate
import com.spendwiz.app.ViewModels.CategoryViewModel
import com.spendwiz.app.utils.ExpenseDonutChartBySubCategory
import kotlinx.coroutines.delay
import java.util.UUID

// Define a sealed interface for all list item types
sealed interface ListItem {
    val key: String
}

// Represents a date header (e.g., "Today", "Yesterday")
data class HeaderItem(val date: String) : ListItem {
    override val key: String = "header_$date"
}

// Wraps your original transaction data class
data class TransactionDataItem(val transaction: Money) : ListItem {
    override val key: String = transaction.id.toString()
}

// Represents a placeholder for a native ad
data class AdItem(val adId: String = UUID.randomUUID().toString()) : ListItem {
    override val key: String = "ad_$adId"
}

// Define how often you want an ad to appear. E.g., 1 ad every 7 items.
private const val AD_INTERVAL = 12

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InsightsScreen(
    viewModel: AddScreenViewModel = viewModel(),
    categoryViewModel: CategoryViewModel,
    navController1: NavHostController
) {
    var isLoading by remember { mutableStateOf(true) }

    val today = remember { LocalDate.now() }
    var selectedYear by rememberSaveable { mutableStateOf(today.year) }
    var selectedMonthIndex by rememberSaveable { mutableStateOf(today.monthValue - 1) }

    LaunchedEffect(selectedMonthIndex, selectedYear) {
        isLoading = true
        delay(600)
        isLoading = false
    }

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

    val listWithAds = remember(moneyList) {
        val items = mutableListOf<ListItem>()
        var transactionCounter = 0
        val groupedByDate = moneyList.groupBy { it.date ?: "" }
            .toList()
            .sortedByDescending { it.first }

        groupedByDate.forEach { (date, transactionsForDate) ->
            items.add(HeaderItem(date))
            transactionsForDate.forEach { transaction ->
                items.add(TransactionDataItem(transaction))
                transactionCounter++
                if (transactionCounter % AD_INTERVAL == 0) {
                    items.add(AdItem())
                }
            }
        }
        items
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
    val scope = rememberCoroutineScope()
    val orientation = LocalConfiguration.current.orientation

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomTopBar(
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            selectedMonthIndex = selectedMonthIndex,
            selectedItems = selectedItems,
            moneyList = moneyList,
            datePickerDialog = datePickerDialog,
            viewModel = viewModel,
            categoryViewModel = categoryViewModel,
            onMonthChange = { newMonthIndex, newYear ->
                selectedMonthIndex = newMonthIndex
                selectedYear = newYear
            }
        )

        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                BlueCircularLoader(Modifier)
            }
        } else if (moneyList.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(painter = painterResource(R.drawable.no_transaction), "", modifier = Modifier.size(50.dp))
                Spacer(Modifier.height(20.dp))
                Text(text = "No transactions this month!", color = MaterialTheme.colorScheme.onBackground)
            }
        } else {
            // Define UI parts as composable lambdas to reuse them
            val chartsPager = @Composable { modifier: Modifier ->
                if (monthlyReceived != 0.0 || monthlySpent != 0.0) {
                    val pages = remember(monthlyReceived, monthlySpent) {
                        buildList {
                            if (monthlyReceived != 0.0 && monthlySpent != 0.0) add("Pie")
                            add("ByMonth")
                            add("BySubCategory")
                        }
                    }
                    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pages.size })

                    Box(modifier = modifier) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            when (pages[page]) {
                                "Pie" -> PieChart(spent = monthlySpent, earned = monthlyReceived, modifier = Modifier.fillMaxSize())
                                "ByMonth" -> ExpenseDonutChartByMonth(viewModel = viewModel, month = selectedMonthInNum, year = selectedYear.toString(), modifier = Modifier.fillMaxSize())
                                "BySubCategory" -> ExpenseDonutChartBySubCategory(viewModel = viewModel, month = selectedMonthInNum, year = selectedYear.toString(), modifier = Modifier.fillMaxSize())
                            }
                        }

                        if (pagerState.currentPage > 0) {
                            IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }, modifier = Modifier.align(Alignment.CenterStart).padding(4.dp)) {
                                Icon(painter = painterResource(id = R.drawable.right_arrow), contentDescription = "Previous Chart", modifier = Modifier.size(28.dp).rotate(180f))
                            }
                        }

                        if (pagerState.currentPage < pagerState.pageCount - 1) {
                            IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }, modifier = Modifier.align(Alignment.CenterEnd).padding(4.dp)) {
                                Icon(painter = painterResource(id = R.drawable.right_arrow), contentDescription = "Next Chart", modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                }
            }

            val transactionList = @Composable { modifier: Modifier ->
                LazyColumn(
                    modifier = modifier,
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(items = listWithAds, key = { it.key }) { listItem ->
                        when (listItem) {
                            is HeaderItem -> {
                                Text(
                                    text = listItem.date,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                )
                            }
                            is TransactionDataItem -> {
                                val item = listItem.transaction
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
                                                        "&date=${enc(item.date ?: "")}" +
                                                        "&time=${item.time}"
                                            )
                                        } else {
                                            if (selectedItems.contains(itemId)) selectedItems.remove(itemId) else selectedItems.add(itemId)
                                        }
                                    },
                                    onLongClick = {
                                        if (selectedItems.contains(itemId)) selectedItems.remove(itemId) else selectedItems.add(itemId)
                                    }
                                )
                            }
                            is AdItem -> {
                                AdmobNativeAdCardForInsightScreen()
                            }
                        }
                    }
                }
            }

            // Arrange UI based on orientation
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                Column(modifier = Modifier.fillMaxSize()) {
                    chartsPager(
                        // FIX: Removed named parameter
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp, max = 280.dp)
                            .padding(horizontal = 10.dp)
                    )
                    transactionList(
                        // FIX: Removed named parameter
                        Modifier.weight(1f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chartsPager(
                        // FIX: Removed named parameter
                        Modifier
                            .weight(0.45f)
                            .fillMaxHeight()
                            .padding(vertical = 10.dp)
                    )
                    transactionList(
                        // FIX: Removed named parameter
                        Modifier.weight(0.55f)
                    )
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
    categoryViewModel: CategoryViewModel,
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

    // 🔥 Fetch categories dynamically
    val incomeCategories by categoryViewModel.getCategories("Income")
        .collectAsState(initial = emptyList())

    val expenseCategories by categoryViewModel.getCategories("Expense")
        .collectAsState(initial = emptyList())

    val transferCategories by categoryViewModel.getCategories("Transfer")
        .collectAsState(initial = emptyList())

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.KeyboardArrowLeft,
                contentDescription = "Previous",
                tint = MaterialTheme.colorScheme.onSurface,
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
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clickable { datePickerDialog.show() }
                    .padding(horizontal = 4.dp)
            )

            Icon(
                Icons.Rounded.KeyboardArrowRight,
                contentDescription = "Next",
                tint = MaterialTheme.colorScheme.onSurface,
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            // Select All
            Icon(
                painter = painterResource(
                    id = if (selectedItems.size == moneyList.size && moneyList.isNotEmpty())
                        R.drawable.deselect_all   // show deselect icon
                    else
                        R.drawable.select_all     // show select icon
                ),
                contentDescription = if (selectedItems.size == moneyList.size && moneyList.isNotEmpty())
                    "Deselect All"
                else
                    "Select All",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        if (selectedItems.size == moneyList.size) {
                            selectedItems.clear()
                        } else {
                            selectedItems.clear()
                            selectedItems.addAll(moneyList.map { it.id.toString() })
                        }
                    }
            )

            // space between the two icons
            Spacer(modifier = Modifier.width(8.dp))

            // 3 dots for other actions
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "More",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { expanded = true }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                val enabled = selectedItems.isNotEmpty()

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

    // 🔥 Change Type Dialog
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

    // 🔥 Category Dialog (dynamic from DB)
    if (showCategoryDialog) {
        val typeForCats = selectedTransactions.firstOrNull()?.type ?: TransactionType.EXPENSE
        val categories: List<String> = when (typeForCats) {
            TransactionType.INCOME -> incomeCategories.map { it.name }
            TransactionType.EXPENSE -> expenseCategories.map { it.name }
            TransactionType.TRANSFER -> transferCategories.map { it.name }
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

    // 🔥 SubCategory Dialog (dynamic from DB)
    if (showSubCategoryDialog && isSameType && isSameCategory) {
        val typeForSubs = selectedTransactions.firstOrNull()?.type ?: TransactionType.EXPENSE
        val category = selectedTransactions.firstOrNull()?.category ?: ""

        // find category object
        val selectedCategoryObj = when (typeForSubs) {
            TransactionType.INCOME -> incomeCategories.find { it.name == category }
            TransactionType.EXPENSE -> expenseCategories.find { it.name == category }
            TransactionType.TRANSFER -> transferCategories.find { it.name == category }
        }

        // load subcategories dynamically
        val subCategories by remember(selectedCategoryObj?.id) {
            selectedCategoryObj?.id?.let { categoryId ->
                categoryViewModel.getSubCategories(categoryId)
            } ?: kotlinx.coroutines.flow.flowOf(emptyList())
        }.collectAsState(initial = emptyList())

        val subCategoryNames = subCategories.map { it.name }

        ChipSelectionDialog(
            title = "Select Sub Category",
            type = typeForSubs,
            options = subCategoryNames,
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

    // 🔥 Delete Dialog
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