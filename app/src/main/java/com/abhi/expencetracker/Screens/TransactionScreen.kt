package com.abhi.expencetracker.Screens
import android.widget.Toast
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abhi.expencetracker.Database.money.ViewModels.AddScreenViewModel
import com.abhi.expencetracker.helper.MoneyItem1


@Composable
fun TransactionScreen(viewModel: AddScreenViewModel ){

    var context = LocalContext.current
    val moneyList1 by viewModel.moneyDao.getAllMoney().observeAsState()
    var currentDate = ""

    val scrollableState = rememberScrollState()

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
