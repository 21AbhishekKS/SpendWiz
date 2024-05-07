package com.abhi.expencetracker.Screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.abhi.expencetracker.Database.money.ViewModels.AddScreenViewModel
import com.abhi.expencetracker.Database.money.Money
import com.abhi.expencetracker.helper.MoneyItem1
import com.abhi.expencetracker.helper.TransactionList


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddScreen(viewModel : AddScreenViewModel){


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







            OutlinedTextField(value = ipMoney.toString(),
                onValueChange ={ ipMoney = it },
                modifier = Modifier.padding(10.dp),
                label = { Text(text = "Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

            )


            OutlinedTextField(
                value = ipDescription,
                onValueChange ={
                ipDescription = it },
                modifier = Modifier.padding(10.dp) ,
                label = { Text(text = "Description")
                })


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

        //TransactionList(moneyList?.reversed())
    } }



