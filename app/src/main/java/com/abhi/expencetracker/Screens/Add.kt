package com.abhi.expencetracker.Screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.expencetracker.ViewModels.AddScreenViewModel
import com.abhi.expencetracker.Database.money.Money

@RequiresApi(Build.VERSION_CODES.O)
//@Preview(showBackground = true)
@Composable
fun AddScreen(viewModel : AddScreenViewModel){
    //Text(text = "AddScreen")

    val moneyList by viewModel.moneyList.observeAsState()

    val context = LocalContext.current


    var ipMoney by rememberSaveable{
        mutableStateOf("")
    }
    var ipDescription by rememberSaveable {
        mutableStateOf("")
    }

    Column(
        Modifier
            .fillMaxHeight()
            .padding(8.dp)
            ) {

        Column(modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally

        ){

            OutlinedTextField(value = ipMoney.toString(), onValueChange ={
                ipMoney = it
            }, modifier = Modifier.padding(10.dp) , label = { Text(text = "Amount")})
            OutlinedTextField(value = ipDescription, onValueChange ={
                ipDescription = it
            }, modifier = Modifier.padding(10.dp) ,label = { Text(text = "Description")})

            Button(onClick = {
                if(ipMoney== "" || ipDescription == ""){
                    Toast.makeText(context , "All fields are required" , Toast.LENGTH_SHORT).show()
                }
                else{
                    viewModel.addMoney(ipMoney,ipDescription,"received")
                    ipMoney=""
                    ipDescription=""
                }


            }) {
                Text(text = "Add")
            }
        }

        moneyList?.let {
        LazyColumn(
            content = {
                itemsIndexed(moneyList!!){ index, item ->
                    MoneyItem(item = item)

                }
            }

        )}?: Text(text = "no Items")
    } }


@Composable
fun MoneyItem(item : Money){

  Row(
      Modifier
          .fillMaxWidth()
          .padding(8.dp)
          .background(color = MaterialTheme.colorScheme.primary)
          .clip(RoundedCornerShape(16.dp))
          .padding(16.dp) ,
      verticalAlignment = Alignment.CenterVertically
      ) {

      Column( Modifier.weight(1f) ) {

          Text(text = item.amount.toString() ,
              fontSize = 22.sp ,color = Color.White ,

              )

          Text(text = item.discription , fontSize = 12.sp  , color = Color.LightGray)
          Text(text = item.date.toString() , fontSize = 12.sp , color = Color.LightGray)
      }
      Icon(imageVector =
          if(item.type == "Earned"){
                       Icons.Filled.Add      
          }else{
               Icons.Filled.Send
          }
          , contentDescription = "")
  }

}