package com.abhi.expencetracker.helper

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.abhi.expencetracker.Database.money.Money
import com.abhi.expencetracker.Database.money.ViewModels.AddScreenViewModel
import com.abhi.expencetracker.R
import com.abhi.expencetracker.navigation.Routes

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun TransactionList(moneyList: List<Money>? , navController : NavController , viewModel : AddScreenViewModel) {

    val isListEmpty = moneyList?.isEmpty() ?: true

    var  context = LocalContext.current

    Box( // Use Box for full-screen coverage
        modifier = Modifier.fillMaxSize()
    ) {
        if (isListEmpty) {
            Column(
                modifier = Modifier.fillMaxSize(), // Ensure full-screen coverage when empty
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "No Transactions today !")
               // Image(
                //    painter = painterResource(id = R.drawable.no_transaction),
                //    contentDescription = "No transactions found",
                //    Modifier
                //        .height(10.dp)
                 //       .width(10.dp)
              //  )
            }
        } else {
            LazyColumn(
                content = {
                    itemsIndexed(moneyList!!) { index, item ->
                        var passingDescription = item.discription
                        var passingamount = item.amount
                        var id = item.id
                        var passingtype = item.type
                        MoneyItem1(item = moneyList[index] , onClick = {



                          //  navController.navigate(route = Routes.AddScreen.route+"/$passingDescription/$passingamount/$id")
                            navController.navigate(route = "${Routes.AddScreen.route}?description=$passingDescription&amount=$passingamount&id=$id&type=$passingtype")


                             viewModel.deleteMoney(id)
                        }
                        )
                    }
                }
            )
        }
    }



}