package com.abhi.expencetracker.utils

import android.view.View.OnClickListener
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abhi.expencetracker.Database.money.Money
import com.abhi.expencetracker.Database.money.TransactionType
import com.abhi.expencetracker.R
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MoneyItem1(item : Money ,
               onClick: () -> Unit
       ){


    Card(
        Modifier
            .background(Color.White)
            .fillMaxWidth()
            .padding(5.dp)
            .clickable {

                onClick()


            }
    ) {
        Row(
            Modifier
                .background(Color.White)
                .padding(end = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically){


            Row(verticalAlignment = Alignment.CenterVertically){
                Image(painter = painterResource(id =
                if(item.type  == TransactionType.INCOME ){R.drawable.received_icon}
                else if (item.type  == TransactionType.EXPENSE){R.drawable.spent_icon}
                else{R.drawable.transaction_icon}
                ),
                    contentDescription = "" ,
                    Modifier
                        .padding(10.dp)
                        .size(40.dp)
                )

                Column {
                    Text(text = item.description, maxLines = 1, overflow = TextOverflow.Ellipsis
                        , color = Color.Black)
                    // Text(text = SimpleDateFormat("hh-mm:aa , dd/MM", Locale.ENGLISH).format(item.date))
                   // Text(text = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(item.date))
                    Text(text = item.date , color = Color.Black)
                    item.upiRefNo?.let { Text(text = it, color = Color.Black) }
                }
            }


            Text(text = if(item.type  == TransactionType.INCOME ){"+ "+item.amount}
            else if (item.type  == TransactionType.EXPENSE){"- "+item.amount}
            else{"  "+item.amount} ,

                color =
                if(item.type  == TransactionType.INCOME ){
                    Color(0xFF5ABB5E) }
                else if (item.type  == TransactionType.EXPENSE){
                    Color(0xFFF03B2E)
                }
                else{
                    Color(0xFF4B62E4)
                })
        }

    }

}
