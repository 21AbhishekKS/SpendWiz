package com.abhi.expencetracker.Screens
import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.abhi.expencetracker.Database.money.ViewModels.AddScreenViewModel
import com.abhi.expencetracker.utils.AnimatedIconCard
import com.abhi.expencetracker.utils.TransactionList

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    viewModel: AddScreenViewModel,
    navController1: NavHostController
){
    val context = LocalContext.current



    val todayMoneyList by viewModel.todayMoneyList.observeAsState()


    var totalMoneySpent by remember {
        mutableIntStateOf(0)
    }
    var totalMoneyEarned by remember {
        mutableStateOf(0)
    }

    LaunchedEffect(todayMoneyList)
    {
        totalMoneySpent =0
        totalMoneyEarned =0
    todayMoneyList?.forEach(){
            if(it.type == "Spent"){
                totalMoneySpent +=  it.amount.toInt()
            }
             else if(it.type == "Received"){
                totalMoneyEarned += it.amount.toInt()
            }

    }
    }






    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White),
          //  .verticalScroll(scrollableState),
        horizontalAlignment = Alignment.CenterHorizontally,


    ) {



        CardItemHome(
           // totalBalance = "$3,257.00",
            income = totalMoneyEarned.toString(),
            expenses = totalMoneySpent.toString()
        )

        AnimatedIconCard()

        Text(
            text = "Today's Transactions",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
        Text(
            text = "Need to update a transaction? Just click on it!",
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray
        )



       TransactionList(todayMoneyList?.reversed() , navController = navController1 , viewModel)



    }




}

@Composable
fun CardItemHome(
                 income: String,
                 expenses: String,
                 modifier: Modifier = Modifier){
    Column(
        Modifier
            .padding(15.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(


                    0.0f to Color(0xFFF35979),
                    1.0f to Color(0xFF6A82FB),


                    )
            )
            .padding(15.dp),

                verticalArrangement = Arrangement.SpaceEvenly

    ) {
        Column(modifier = Modifier,
            ) {
            Text(
                text = "Today's Financial Snapshot ",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                          text = "Income",
                          style = MaterialTheme.typography.bodyMedium,
                          color = Color.White
                           )
                           Text(
                           text = income,
                           style = MaterialTheme.typography.bodyMedium,
                           color = Color.White
                           )
                }
                Column {
                    Text(
                        text = "Expenses",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = expenses,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}


