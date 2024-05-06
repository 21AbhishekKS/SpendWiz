package com.abhi.expencetracker.helper

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.abhi.expencetracker.Database.money.Money
import com.abhi.expencetracker.R

@Composable
fun TransactionList(moneyList: List<Money>?) {

    val isListEmpty = moneyList?.isEmpty() ?: true

    Box( // Use Box for full-screen coverage
        modifier = Modifier.fillMaxSize()
    ) {
        if (isListEmpty) {
            Column(
                modifier = Modifier.fillMaxSize(), // Ensure full-screen coverage when empty
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "No items !")
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
                        MoneyItem1(item = moneyList[index])
                    }
                }
            )
        }
    }



}