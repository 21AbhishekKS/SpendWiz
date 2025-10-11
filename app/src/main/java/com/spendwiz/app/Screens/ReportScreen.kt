package com.spendwiz.app.Screens

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spendwiz.app.Ads.CommonNativeAd
import com.spendwiz.app.AppStyle.AppColors.customButtonColors
import com.spendwiz.app.R
import com.spendwiz.app.ViewModels.AddScreenViewModel
import com.spendwiz.app.utils.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.format.TextStyle
import java.io.File
import java.util.Locale

@Composable
fun ReportScreen(
    addScreenViewModel: AddScreenViewModel = viewModel()
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // Landscape layout: Split screen
        Row(modifier = Modifier.fillMaxSize()) {
            // First half: Report generation UI
            ReportGeneratorUI(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(16.dp),
                viewModel = addScreenViewModel
            )

            // Second half: Ad
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CommonNativeAd(
                    Modifier,
                    stringResource(id = R.string.ad_unit_id_more_option_screen)
                )
            }
        }
    } else {
        // Portrait layout: Ad in bottom bar
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                CommonNativeAd(
                    Modifier,
                    stringResource(id = R.string.ad_unit_id_more_option_screen)
                )
            }
        ) { innerPadding ->
            ReportGeneratorUI(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                        end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = innerPadding.calculateBottomPadding()
                    )
                    .padding(horizontal = 16.dp),
                viewModel = addScreenViewModel
            )
        }
    }
}

@Composable
fun ReportGeneratorUI(
    modifier: Modifier = Modifier,
    viewModel: AddScreenViewModel
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Monthly Report", "Yearly Report")

    Column(modifier = modifier) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, color = colorResource(id = R.color.button_color)) }
                )
            }
        }
        when (selectedTabIndex) {
            0 -> MonthlyReportContent(viewModel = viewModel)
            1 -> YearlyReportContent(viewModel = viewModel)
        }
    }
}


@Composable
fun MonthlyReportContent(
    viewModel: AddScreenViewModel,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    // Fetch available years from the database
    val availableYears by viewModel.distinctYears.observeAsState(initial = emptyList())

    val currentDate = LocalDate.now()
    var selectedYear by remember { mutableIntStateOf(currentDate.year) }
    var selectedMonth by remember { mutableStateOf(currentDate.month) }

    // Set the initial year to the latest one from the database once loaded
    LaunchedEffect(availableYears) {
        if (availableYears.isNotEmpty()) {
            selectedYear = availableYears.first()
        }
    }

    val monthString = String.format("%02d", selectedMonth.value)
    val yearString = selectedYear.toString()

    val transactions by viewModel.moneyDao
        .getTransactionsByMonthAndYear(monthString, yearString)
        .observeAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp), // Content padding
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                Text("Generating PDF...", modifier = Modifier.padding(top = 60.dp))
            }
        } else {
            if (availableYears.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transaction data found to generate reports.")
                }
            } else {
                Text("Select Month and Year", style = MaterialTheme.typography.titleMedium)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MonthPicker(
                        modifier = Modifier.weight(1f),
                        selectedMonth = selectedMonth,
                        onMonthSelected = { selectedMonth = it }
                    )
                    YearPicker(
                        modifier = Modifier.weight(1f),
                        selectedYear = selectedYear,
                        onYearSelected = { selectedYear = it },
                        years = availableYears
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                val monthName = selectedMonth.getDisplayName(TextStyle.FULL, Locale.getDefault())
                                val fileName = "Monthly_Report_${monthName}_$yearString.pdf"
                                val reportFile = File(context.cacheDir, fileName)

                                withContext(Dispatchers.IO) {
                                    PdfGenerator.generateEnhancedMonthlyReport(
                                        context = context,
                                        file = reportFile,
                                        monthName = monthName,
                                        year = yearString,
                                        transactions = transactions
                                    )
                                }

                                val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", reportFile)
                                sharePdf(context, fileUri)

                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    e.printStackTrace()
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = transactions.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = customButtonColors()
                ) {
                    Text("Generate Monthly PDF")
                }

                if (transactions.isEmpty() && !isLoading) {
                    Text("No transactions found for the selected period.")
                }
            }
        }
    }
}

@Composable
fun YearlyReportContent(
    viewModel: AddScreenViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    // Fetch available years from the database
    val availableYears by viewModel.distinctYears.observeAsState(initial = emptyList())
    val currentDate = LocalDate.now()
    var selectedYear by remember { mutableStateOf(currentDate.year) }

    // Set the initial year to the latest one from the database once loaded
    LaunchedEffect(availableYears) {
        if (availableYears.isNotEmpty()) {
            selectedYear = availableYears.first()
        }
    }

    val yearString = selectedYear.toString()

    val yearlyTransactions by viewModel.getTransactionsForYear(yearString)
        .observeAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp), // Content padding
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                Text("Generating PDF...", modifier = Modifier.padding(top = 60.dp))
            }
        } else {
            if (availableYears.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transaction data found to generate reports.")
                }
            } else {
                Text("Select Year", style = MaterialTheme.typography.titleMedium)

                // Pass the fetched list of years to the picker
                YearPicker(
                    selectedYear = selectedYear,
                    onYearSelected = { selectedYear = it },
                    years = availableYears
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                val fileName = "Yearly_Report_$yearString.pdf"
                                val reportFile = File(context.cacheDir, fileName)

                                withContext(Dispatchers.IO) {
                                    PdfGenerator.generateEnhancedYearlyReport(
                                        context = context,
                                        file = reportFile,
                                        year = yearString,
                                        transactions = yearlyTransactions
                                    )
                                }

                                val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", reportFile)
                                sharePdf(context, fileUri)

                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Error generating report: ${e.message}", Toast.LENGTH_LONG).show()
                                    e.printStackTrace()
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = yearlyTransactions.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = customButtonColors()
                ) {
                    Text("Generate Yearly PDF")
                }

                if (yearlyTransactions.isEmpty() && !isLoading) {
                    Text("No data found for the selected year.")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearPicker(
    modifier: Modifier = Modifier,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    years: List<Int>
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            value = selectedYear.toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Year") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(),
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            years.forEach { year ->
                DropdownMenuItem(
                    text = { Text(year.toString()) },
                    onClick = {
                        onYearSelected(year)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthPicker(
    modifier: Modifier = Modifier,
    selectedMonth: Month,
    onMonthSelected: (Month) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val months = Month.values()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            value = selectedMonth.getDisplayName(TextStyle.FULL, Locale.getDefault()),
            onValueChange = {},
            readOnly = true,
            label = { Text("Month") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(),
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            matchTextFieldWidth = false
        ) {
            months.forEach { month ->
                DropdownMenuItem(
                    text = { Text(month.getDisplayName(TextStyle.FULL, Locale.getDefault())) },
                    onClick = {
                        onMonthSelected(month)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun sharePdf(context: Context, fileUri: Uri) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, fileUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Export Report"))
}