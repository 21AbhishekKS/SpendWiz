package com.abhi.expencetracker.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

//-------------------------------------Bar Chart-----------------------------------------------------------//
data class BarChartData(val value: Int, val color: Color, val message : String, val type :String)

@Composable
fun Bar(height: Float, color: Color, message : String, type :String , context : Context) {

    Column(
        Modifier.background(Color.White),
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

        Text(text = type , color = Color.Black)
    }

}

@Composable
fun BarChart(data: List<BarChartData> ,
             context: Context,
             MonthlySpent: Int,
             MonthlyReceived: Int,
             MonthlyTransaction: Int) {

    Row(horizontalArrangement = Arrangement.SpaceBetween) {




        Row(
            modifier = Modifier
                .fillMaxWidth(.69f)
                .height(250.dp)
                .background(Color.White),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val maxValue = data.map { it.value }.maxOf { it }
            data.forEach { barData ->
                Bar(
                    height = barData.value.toFloat() / maxValue * 200,
                    color = barData.color,
                    message = barData.message,
                    type = barData.type,
                    context = context
                )
            }
        }


        Column(modifier = Modifier.fillMaxWidth()
            .background(Color.White),
            horizontalAlignment = Alignment.End
        ) {


            Column(
                modifier = Modifier.fillMaxWidth().background(Color.White),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    Modifier.background(Color.White)
                    ,verticalAlignment = Alignment.CenterVertically){
                    Box(
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .size(15.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                    Text(text = "₹$MonthlySpent" , color = Color.Black)
                }
                Row(
                    Modifier.background(Color.White) ,
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .size(15.dp)
                            .clip(CircleShape)
                            .background(Color(76, 175, 80, 255))

                    )
                    Text(text = "₹$MonthlyReceived", color = Color.Black)
                }
                Row(
                    Modifier.background(Color.White),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .size(15.dp)
                            .clip(CircleShape)
                            .background(Color.Blue)

                    )
                    Text(text = "₹$MonthlyTransaction", color = Color.Black)
                }

            }
        }


    }

}


@Composable
fun MonthlyBarChart(
    spent: Int,
    received: Int,
    transaction: Int,
    context: Context,
) {
    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        BarChart(
            data = listOf(
                BarChartData(spent,
                    Color.Red,
                    "You've spent ₹$spent this May! Let's track your progress",
                    "Spent"
                ),

                BarChartData(received,
                    Color(76, 175, 80, 255),
                    "₹$received Boost to your May income! Keep it up",
                    "Earned"
                ),

                BarChartData(transaction,
                    Color.Blue,
                    "You've transacted ₹$transaction this May!",
                    "Transact")
            ) ,
            context = context,
            MonthlySpent = spent,
            MonthlyTransaction = transaction,
            MonthlyReceived = received
        )
    }
}





//----------------------------------------Bar Chart code ended---------------------------------------------------------//