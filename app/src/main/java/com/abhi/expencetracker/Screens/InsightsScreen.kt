package com.abhi.expencetracker.Screens

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.expencetracker.ViewModels.AddScreenViewModel
import com.abhi.expencetracker.utils.MoneyItem1
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.abhi.expencetracker.Database.money.TransactionType
import com.abhi.expencetracker.R
import com.abhi.expencetracker.helper.OnBoarding.LoaderIntro
import com.abhi.expencetracker.navigation.Routes
import com.abhi.expencetracker.utils.PieChart

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InsightsScreen(viewModel: AddScreenViewModel, navController1: NavHostController){

    var context = LocalContext.current

   // val moneyList1 by viewModel.moneyDao.getAllMoney().observeAsState()


    var currentDate = ""

    val scrollableState = rememberScrollState()

    var isMonthExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    var isYearExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    var selectedMonth by rememberSaveable {
        mutableStateOf("January")
    }
    var selectedMonthInNum by rememberSaveable {
        mutableStateOf("01")
    }

    var selectedYear by rememberSaveable {
        mutableStateOf("2024")
    }

    val moneyList1 by viewModel.moneyDao.getTransactionsByMonthAndYear(selectedMonthInNum , selectedYear).observeAsState()

    var MonthlySpent by rememberSaveable {
        mutableStateOf(0)
    }
    var MonthlyReceived by rememberSaveable {
        mutableStateOf(0)
    }
    var MonthlyTransaction by rememberSaveable {
        mutableStateOf(0)
    }





    LaunchedEffect(moneyList1) {
        MonthlySpent = 0
        MonthlyReceived = 0
        MonthlyTransaction = 0

        moneyList1?.forEach {
            when (it.type) {
                TransactionType.EXPENSE -> MonthlySpent += it.amount.toInt()
                TransactionType.INCOME -> MonthlyReceived += it.amount.toInt()
            }
        }
    }

    val listOfMonth = listOf(
        "January" to "01",
        "February" to "02",
        "March" to "03",
        "April" to "04",
        "May" to "05",
        "June" to "06",
        "July" to "07",
        "August" to "08",
        "September" to "09",
        "October" to "10",
        "November" to "11",
        "December" to "12"
    )
    val listOfYear = listOf("2024", "2025", "2026", "2027" )





    Column(Modifier.background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally) {


    Column(Modifier.padding(horizontal = 10.dp).background(Color.White) ,
        horizontalAlignment = Alignment.CenterHorizontally)
    {

        Row(
            Modifier
                .padding(top = 20.dp, bottom = 5.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ExposedDropdownMenuBox(
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxWidth(.5f)
                    .border(
                        1.dp, color = Color.Black,
                        RoundedCornerShape(4.dp)
                    )
                ,
                expanded = isMonthExpanded,
                onExpandedChange = { isMonthExpanded =!isMonthExpanded }
            ) {
                TextField(
                    modifier = Modifier
                        .menuAnchor(),
                    value = selectedMonth,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMonthExpanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent ,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        unfocusedTextColor = Color.Black,
                        focusedTextColor = Color.Black,
                        focusedTrailingIconColor = Color.Black,
                        unfocusedTrailingIconColor = Color.Black
                    )
                )

                ExposedDropdownMenu(
                    expanded = isMonthExpanded,
                    onDismissRequest = { isMonthExpanded = false },
                    modifier = Modifier
                        .background(Color.White)

                ) {
                    listOfMonth.forEachIndexed { index, text ->
                        DropdownMenuItem(
                            text = { Text(text = text.first , color = Color.Black) },
                            onClick = {
                                selectedMonth = text.first
                                selectedMonthInNum = text.second
                                isMonthExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            ExposedDropdownMenuBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp, color = Color.Black,
                        RoundedCornerShape(4.dp)
                    )
                ,
                expanded = isYearExpanded,
                onExpandedChange = { isYearExpanded =!isYearExpanded }
            ) {
                TextField(
                    modifier = Modifier
                        .menuAnchor(),
                    value = selectedYear,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isYearExpanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent ,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        unfocusedTextColor = Color.Black,
                        focusedTextColor = Color.Black,
                        focusedTrailingIconColor = Color.Black,
                        unfocusedTrailingIconColor = Color.Black
                    )
                )

                ExposedDropdownMenu(
                    expanded = isYearExpanded,
                    onDismissRequest = { isYearExpanded = false },
                    modifier = Modifier
                        .background(Color.White)
                ) {
                    listOfYear.forEachIndexed { index, text ->
                        DropdownMenuItem(
                            text = { Text(text = text  , color = Color.Black) },
                            onClick = {
                                selectedYear = listOfYear[index]
                                isYearExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
            }


    }


        if (MonthlyReceived != 0 || MonthlySpent != 0) {
            PieChart(
                spent = MonthlySpent.toDouble(),
                earned = MonthlyReceived.toDouble(),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
            )
        }


        Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
        .verticalScroll(scrollableState),

    ) {
        if (moneyList1?.reversed().isNullOrEmpty()) {

            Column(Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {

                Text("No transactions found" ,
                    Modifier
                        .background(Color.DarkGray)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp) ,
                    color = Color.White ,)

                LoaderIntro(modifier = Modifier
                    .fillMaxSize(.8f)
                    , image = R.raw.a8)

            }




        } else {
            moneyList1?.reversed()?.forEachIndexed() { index, item ->
                if (currentDate != item.date ){
                    Text(
                        text = item.date,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp) ,
                        color = Color.Black
                    )
                    currentDate = item.date
                    MoneyItem1(item = item) {
                        navController1.navigate(
                            Routes.UpdateScreen.route +
                                    "?description=${Uri.encode(item.description)}" +
                                    "&amount=${item.amount}" +
                                    "&id=${item.id}" +
                                    "&type=${Uri.encode(item.type.toString())}" +
                                    "&category=${Uri.encode(item.category ?: "")}" +
                                    "&subCategory=${Uri.encode(item.subCategory ?: "")}" +
                                    "&date=${Uri.encode(item.date ?: "")}"
                        )
                    }

                }else{
                    MoneyItem1(item = item) {
                        navController1.navigate(
                            Routes.UpdateScreen.route +
                                    "?description=${Uri.encode(item.description)}" +
                                    "&amount=${item.amount}" +
                                    "&id=${item.id}" +
                                    "&type=${Uri.encode(item.type.toString())}" +
                                    "&category=${Uri.encode(item.category ?: "")}" +
                                    "&subCategory=${Uri.encode(item.subCategory ?: "")}" +
                                    "&date=${Uri.encode(item.date ?: "")}"
                        )

                    }

                }
            }
        }
    }




}
}

