package com.abhi.expencetracker.Screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.abhi.expencetracker.Database.money.ViewModels.AddScreenViewModel
import com.abhi.expencetracker.utils.MoneyItem1

import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InsightsScreen(viewModel: AddScreenViewModel){

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





    LaunchedEffect(moneyList1)
    {
        MonthlySpent =0
        MonthlyReceived =0
        MonthlyTransaction =0
        moneyList1?.forEach(){
            if(it.type == "Spent"){
                MonthlySpent +=  it.amount.toInt()
            }
            else if(it.type == "Received"){
                MonthlyReceived += it.amount.toInt()
            }
            else{
                MonthlyTransaction += it.amount.toInt()

            }
        }

    }


//-------------------------------------Bar Chart-----------------------------------------------------------//
    data class BarChartData(val value: Int, val color: Color, val message : String , val type :String)

    @Composable
    fun Bar(height: Float, color: Color , message : String,type :String) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(height.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color)
                    .clickable {
                        Toast
                            .makeText(context, message, Toast.LENGTH_SHORT)
                            .show()
                    }
            )
            
            Text(text = type)
        }

    }

    @Composable
    fun BarChart(data: List<BarChartData>) {
        
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            
            
            
            
            Row(
                modifier = Modifier
                    .fillMaxWidth(.69f)
                    .height(250.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val maxValue = data.map { it.value }.maxOf { it }
                data.forEach { barData ->
                    Bar(
                        height = barData.value.toFloat() / maxValue * 200,
                        color = barData.color,
                        message = barData.message,
                        type = barData.type
                    )
                }
            }
            
            
            Column(modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End) {


                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically){
                        Box(
                            modifier = Modifier
                                .padding(end = 5.dp)
                                .size(15.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                        Text(text = "₹$MonthlySpent")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .padding(end = 5.dp)
                                .size(15.dp)
                                .clip(CircleShape)
                                .background(Color(76, 175, 80, 255))

                        )
                        Text(text = "₹$MonthlyReceived")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .padding(end = 5.dp)
                                .size(15.dp)
                                .clip(CircleShape)
                                .background(Color.Blue)

                        )
                        Text(text = "₹$MonthlyTransaction")
                    }

                }
            }
            
            
        }
       
    }


    @Composable
            fun MonthlyBarChart(
                spent: Int,
                received: Int,
                transaction: Int
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    BarChart(
                        data = listOf(
                            BarChartData(spent,
                                Color.Red,
                                "You've spent ₹$MonthlySpent this May! Let's track your progress",
                                "Spent"
                                ),

                            BarChartData(received,
                                Color(76, 175, 80, 255),
                                "₹$MonthlyReceived Boost to your May income! Keep it up",
                                "Earned"
                                ),

                            BarChartData(transaction,
                                Color.Blue,
                                "You've transacted ₹$MonthlyTransaction this May!",
                            "Transact")
                        )
                    )
                }
            }





//----------------------------------------Bar Chart code ended---------------------------------------------------------//










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





    Column(horizontalAlignment = Alignment.CenterHorizontally) {


    Column(Modifier.padding(horizontal = 10.dp) ,
        horizontalAlignment = Alignment.CenterHorizontally)
    {

        Row(
            Modifier
                .padding(vertical = 20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ExposedDropdownMenuBox(
                modifier = Modifier
                    .fillMaxWidth(.5f)
                    .border(
                        1.dp, color = Color.Gray,
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
                        focusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
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
                            text = { Text(text = text.first) },
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
                        1.dp, color = Color.Gray,
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
                        focusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
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
                            text = { Text(text = text) },
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


if(moneyList1?.isEmpty() == false) {
   // Text(text = MonthlyTransaction.toString())
   // Text(text = MonthlyReceived.toString())
   // Text(text = MonthlySpent.toString())

    MonthlyBarChart(MonthlySpent , MonthlyReceived , MonthlyTransaction)

}

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
        .verticalScroll(scrollableState),
    ) {
        if (moneyList1?.reversed().isNullOrEmpty()) {
            Text("No transactions found" ,
                Modifier
                    .background(Color.DarkGray)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp) ,
                color = Color.White ,)

        } else {
            moneyList1?.reversed()?.forEachIndexed() { index, item ->
                if (currentDate != item.date ){
                    Text(
                        text = item.date,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                    currentDate = item.date
                    MoneyItem1(item = item , { Toast.makeText(context , item.type , Toast.LENGTH_SHORT).show()})
                }else{
                    MoneyItem1(item = item , {Toast.makeText(context , item.type , Toast.LENGTH_SHORT).show()})
                }

                //TransactionList(moneyList1!!.reversed())
            }
        }
    }




}
}

