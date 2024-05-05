package com.abhi.expencetracker.Screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.abhi.expencetracker.ViewModels.AddScreenViewModel
import com.abhi.expencetracker.Database.money.Money
import com.abhi.expencetracker.R
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
//@Preview(showBackground = true)
@Composable
fun AddScreen(viewModel : AddScreenViewModel){
    //Text(text = "AddScreen")


    val moneyList by viewModel.moneyList.observeAsState()

    val context = LocalContext.current

    val listOfTransactionType = listOf<String>("Spent" , "Received" ,"Transaction")

    var isExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    var ipTransactionType by rememberSaveable {
        mutableStateOf(listOfTransactionType[0])
    }

    var ipMoney by rememberSaveable{
        mutableStateOf("")
    }
    var ipDescription by rememberSaveable {
        mutableStateOf("")
    }
    var ipDate by rememberSaveable {
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



            ExposedDropdownMenuBox(expanded = isExpanded
                , onExpandedChange =  {isExpanded = !isExpanded}) {

                TextField(
                    modifier = Modifier.menuAnchor(),
                    value = ipTransactionType,
                    onValueChange ={},
                    readOnly = true,
                    trailingIcon = {ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)}
                )

                ExposedDropdownMenu(expanded = isExpanded,
                    onDismissRequest = { isExpanded =false}) {

                    listOfTransactionType.forEachIndexed { index , text ->
                        DropdownMenuItem(text = { Text(text = text) },
                            onClick = {
                                ipTransactionType = listOfTransactionType[index]
                                isExpanded = false
                            } , contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )

                    }
                }
            }







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
                    viewModel.addMoney(ipMoney,ipDescription,ipTransactionType)
                    ipMoney=""
                    ipDescription=""
                    ipDate=""
                }


            }) {
                Text(text = "Add")
            }
        }

        TransactionList(moneyList)
    } }


@Composable
fun TransactionList(moneyList: List<Money>?) {

    moneyList?.let {
        LazyColumn(
            content = {
                itemsIndexed(moneyList!!){ index, item ->
                    MoneyItem1(item = item)

                }
            }

        )}?: Text(text = "no Items")

}
//@Preview(showBackground = true)
@Composable
fun MoneyItem1(item : Money){

        Card(
            Modifier
                .fillMaxWidth()
                .padding(5.dp)
            ) {
            Row(
                Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically){


                Row(verticalAlignment = Alignment.CenterVertically){
                    Image(painter = painterResource(id = R.drawable.green_tick),
                        contentDescription = "" ,
                        Modifier
                            .padding(10.dp)
                            .size(40.dp)
                    )

                    Column {
                        Text(text = item.discription)
                       // Text(text = SimpleDateFormat("hh-mm:aa , dd/MM", Locale.ENGLISH).format(item.date))
                        Text(text = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(item.date))
                    }
                }


                Text(text = if(item.type  == "Received" ){"+ "+item.amount}
                else if (item.type  == "Spent"){"- "+item.amount}
                else{"  "+item.amount} ,

                color =
                if(item.type  == "Received" ){Color(0xFF5ABB5E) }
                else if (item.type  == "Spent"){Color(0xFFF03B2E)}
                else{Color(0xFF4B62E4)
                })
            }

    }

}