package com.abhi.expencetracker.utils

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.abhi.expencetracker.Database.money.Money
import com.abhi.expencetracker.ViewModels.AddScreenViewModel
import com.abhi.expencetracker.navigation.Routes
import com.abhi.expencetracker.utils.MoneyItem1

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun TransactionList(
    moneyList: List<Money>?,
    navController: NavController,
    viewModel: AddScreenViewModel
) {
    val isListEmpty = moneyList?.isEmpty() ?: true

    Box(modifier = Modifier.fillMaxSize()) {
        if (isListEmpty) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "No Transactions today !", color = Color.Black)
            }
        } else {
            LazyColumn {
                itemsIndexed(moneyList!!) { index, item ->
                    MoneyItem1(item = item, onClick = {
                        val description = Uri.encode(item.description ?: "")
                        val amount = item.amount
                        val id = item.id
                        val type = Uri.encode(item.type.toString())
                        val category = Uri.encode(item.category ?: "")
                        val subCategory = Uri.encode(item.subCategory ?: "")
                        val date = Uri.encode(item.date ?: "")

                        navController.navigate(
                            Routes.UpdateScreen.route +
                                    "?description=$description" +
                                    "&amount=$amount" +
                                    "&id=$id" +
                                    "&type=$type" +
                                    "&category=$category" +
                                    "&subCategory=$subCategory" +
                                    "&date=$date" +
                                    "&time=${item.time }"
                        )
                    })
                }
            }
        }
    }
}
