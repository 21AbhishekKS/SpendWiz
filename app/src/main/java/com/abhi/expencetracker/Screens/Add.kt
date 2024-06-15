package com.abhi.expencetracker.Screens

import android.graphics.Color.rgb
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abhi.expencetracker.Database.money.ViewModels.AddScreenViewModel
import com.abhi.expencetracker.R
import com.abhi.expencetracker.navigation.Routes
import com.abhi.expencetracker.utils.TransparentTextField


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddScreen(
    viewModel: AddScreenViewModel,
    navController: NavController,
  //  passedDescription: String,
  //  passedAmount: String,
  //  id: Int,
  //  type: String
) {

    var isNumberValid by rememberSaveable { mutableStateOf(true) }


    val moneyList by viewModel.moneyList.observeAsState()

    val context = LocalContext.current

    val listOfTransactionType = listOf("Spent", "Received", "Transaction")

    var isExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    var ipTransactionType by rememberSaveable {
        mutableStateOf("Transaction")
    }

    var ipMoney by rememberSaveable {
        mutableStateOf("")
    }
    var ipDescription by rememberSaveable {
        mutableStateOf("")
    }
    var ipDate by rememberSaveable {
        mutableStateOf("")
    }

    val backgroundAnimatable = remember {
        Animatable(
            Color(if (ipTransactionType == "Spent") {
                Color(255, 87, 51).toArgb()
            } else if (ipTransactionType == "Received") {
                Color(android.graphics.Color.rgb(125, 218, 88)).toArgb()
            } else {
                Color(android.graphics.Color.rgb(93, 226, 231)).toArgb()
            })
        )
    }

    fun saveTransaction(){
        if (ipMoney == "" || ipDescription == "") {
            Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT)
                .show()
        } else {
            viewModel.addMoney(0, ipMoney, ipDescription, ipTransactionType)
            ipMoney = ""
            ipDescription = ""
            ipDate = ""
            //   Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }


    LaunchedEffect(key1 = ipTransactionType) {
        backgroundAnimatable.animateTo(
            targetValue = Color(if (ipTransactionType == "Spent") {
                Color(235, 0, 0, 255).toArgb()
            } else if (ipTransactionType == "Received") {
                Color(16, 138, 0, 255).toArgb()
            } else {
                Color(0, 0, 255, 255).toArgb()
            }),
            animationSpec = tween(
                durationMillis = 500
            )
        )
    }



    Box(contentAlignment = Alignment.BottomStart){


        Surface(color = backgroundAnimatable.value,
            modifier = Modifier.fillMaxSize()) {
            Column(
                Modifier.padding(top = 25.dp),
                horizontalAlignment = Alignment.CenterHorizontally                 ) {

                Text(text = if(ipTransactionType  == "Received" ){"Received"}
                else if (ipTransactionType  == "Spent"){"Spent"}
                else{"Transaction"}
                    , fontWeight = FontWeight.Bold  ,
                    color = Color.White ,
                    fontSize = 25.sp ,
                    )

            }

        }

        Surface(color = Color.White ,
            modifier = Modifier
                .fillMaxHeight(.5f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(60.dp).copy(bottomEnd = ZeroCornerSize , bottomStart = ZeroCornerSize)
        ) {

            Column(
                Modifier
                    .padding(top = 60.dp)
                    .padding(horizontal = 10.dp)) {


                ExposedDropdownMenuBox(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .border(
                            1.dp, color = Color.Black,
                            RoundedCornerShape(4.dp)
                        )
                        .fillMaxWidth(),
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = !isExpanded }
                ) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        value = ipTransactionType,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent ,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            unfocusedTextColor = Color.Black,
                            focusedTextColor = Color.Black,
                            focusedTrailingIconColor = Color.Black,
                            unfocusedTrailingIconColor = Color.Black
                        )
                    )




                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false },
                        modifier = Modifier
                            .background(Color.White)
                            .fillMaxWidth(),

                    ) {
                        listOfTransactionType.forEachIndexed { index, text ->
                            DropdownMenuItem(
                                text = { Text(text = text , color = Color.Black) },
                                onClick = {
                                    ipTransactionType = listOfTransactionType[index]
                                    isExpanded = false
                                   },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }










                val numberPattern = Regex("^[0-9]*$")



                OutlinedTextField(
                    value = ipMoney,
                    onValueChange = {
                        if (numberPattern.matches(it)) {
                            ipMoney = it
                        }
                        },
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .fillMaxWidth()
                    , colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        disabledBorderColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black// Optional for disabled state
                    ),
                    label = { Text(text = "Amount" , color = Color.Black) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,

                    ),
                    singleLine = true,

                //    visualTransformation = NumberTransformation(),
                 //   keyboardActions = KeyboardActions(onNext = { keyboardController?.hide() })

                )


                OutlinedTextField(
                    value = ipDescription ,
                    onValueChange ={
                        ipDescription = it },
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 10.dp)
                        .fillMaxWidth() ,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        disabledBorderColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black// Optional for disabled state
                    ),
                    label = { Text(text = "Description" , color = Color.Black)},
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions {
                            saveTransaction()
                        },
                        singleLine = true,

                    )







                Button(
                    onClick = { saveTransaction() },
                    Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(text = "Save")
                }


            }


        }
    }
}

