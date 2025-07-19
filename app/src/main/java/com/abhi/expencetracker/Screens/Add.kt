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
import androidx.compose.foundation.layout.Row
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
import com.abhi.expencetracker.Database.money.TransactionType
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
) {
    // State variables
    val context = LocalContext.current
    val categories = listOf("Food", "Transport", "Shopping", "Bills", "Others")
    var selectedCategory by rememberSaveable { mutableStateOf("Others") }
    var categoryDropdownExpanded by rememberSaveable { mutableStateOf(false) }

    val subCategorySuggestions = mapOf(
        "Food" to listOf("Breakfast", "Lunch", "Dinner", "Snacks", "Groceries"),
        "Transport" to listOf("Bus", "Train", "Taxi", "Fuel", "Flight"),
        "Shopping" to listOf("Clothes", "Electronics", "Accessories", "Gifts"),
        "Bills" to listOf("Electricity", "Internet", "Water", "Mobile"),
        "Others" to listOf("Misc", "Donation", "Others")
    )
    var selectedSubCategory by rememberSaveable { mutableStateOf("") }

    val listOfTransactionType = listOf("Spent", "Received")
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var ipTransactionType by rememberSaveable { mutableStateOf("Transaction") }

    var ipMoney by rememberSaveable { mutableStateOf("") }
    var ipDescription by rememberSaveable { mutableStateOf("") }
    var ipDate by rememberSaveable { mutableStateOf("") }

    // Background animation
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

    // Save transaction function
    fun saveTransaction() {
        if (ipMoney == "" || ipDescription == "") {
            Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
        } else {
            val amountDouble = ipMoney.toDoubleOrNull()
            if (amountDouble == null) {
                Toast.makeText(context, "Invalid amount", Toast.LENGTH_SHORT).show()
                return
            }

            val transactionType = when (ipTransactionType) {
                "Spent" -> TransactionType.EXPENSE
                "Received" -> TransactionType.INCOME
                else -> {
                    Toast.makeText(context, "Select valid transaction type", Toast.LENGTH_SHORT).show()
                    return
                }
            }

            viewModel.addMoney1(
                id = 0,
                amount = amountDouble,
                description = ipDescription,
                type = transactionType,
                category = selectedCategory,
                subCategory = selectedSubCategory
            )

            ipMoney = ""
            ipDescription = ""
            selectedSubCategory = ""
            ipDate = ""
            navController.popBackStack()
        }
    }

    LaunchedEffect(key1 = ipTransactionType) {
        backgroundAnimatable.animateTo(
            targetValue = Color(if (ipTransactionType == "Spent") {
                Color(240, 59, 46, 255).toArgb()
            } else if (ipTransactionType == "Received") {
                Color(90, 187, 94, 255).toArgb()
            } else {
                Color(75, 98, 228, 255).toArgb()
            }),
            animationSpec = tween(durationMillis = 500)
        )
    }

    Box(contentAlignment = Alignment.BottomStart) {
        Surface(color = backgroundAnimatable.value, modifier = Modifier.fillMaxSize()) {
            Column(
                Modifier.padding(top = 25.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (ipTransactionType == "Received") "Received"
                    else if (ipTransactionType == "Spent") "Spent"
                    else "Transaction",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 25.sp,
                )
            }
        }

        Surface(
            color = Color.White,
            modifier = Modifier
                .fillMaxHeight(.8f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(60.dp).copy(bottomEnd = ZeroCornerSize, bottomStart = ZeroCornerSize)
        ) {
            Column(
                Modifier
                    .padding(top = 60.dp)
                    .padding(horizontal = 10.dp)
            ) {
                // Transaction Type Dropdown
                ExposedDropdownMenuBox(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .border(1.dp, color = Color.Black, RoundedCornerShape(4.dp))
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
                            focusedContainerColor = Color.Transparent,
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
                        modifier = Modifier.background(Color.White)
                    ) {
                        listOfTransactionType.forEachIndexed { index, text ->
                            DropdownMenuItem(
                                text = { Text(text = text, color = Color.Black) },
                                onClick = {
                                    ipTransactionType = listOfTransactionType[index]
                                    isExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }

                // Category Dropdown
                ExposedDropdownMenuBox(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                        .fillMaxWidth(),
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    TextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        label = { Text("Category", color = Color.Black) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedTextColor = Color.Black,
                            focusedTextColor = Color.Black,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(text = category, color = Color.Black) },
                                onClick = {
                                    selectedCategory = category
                                    selectedSubCategory = ""
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // SubCategory Suggestions
                val currentSubSuggestions = subCategorySuggestions[selectedCategory] ?: listOf()
                if (currentSubSuggestions.isNotEmpty()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, top = 4.dp, bottom = 4.dp)
                    ) {
                        currentSubSuggestions.forEach { suggestion ->
                            Button(
                                onClick = { selectedSubCategory = suggestion },
                                modifier = Modifier.padding(end = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(220, 220, 220),
                                    contentColor = Color.Black
                                )
                            ) {
                                Text(text = suggestion, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // SubCategory Input
                OutlinedTextField(
                    value = selectedSubCategory,
                    onValueChange = { selectedSubCategory = it },
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
                    label = { Text("Subcategory", color = Color.Black) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        disabledBorderColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    singleLine = true
                )

                // Amount Input
                OutlinedTextField(
                    value = ipMoney,
                    onValueChange = {
                        if (it.matches(Regex("^[0-9]*$"))) {
                            ipMoney = it
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        disabledBorderColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    label = { Text(text = "Amount", color = Color.Black) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                    singleLine = true,
                )

                // Description Input
                OutlinedTextField(
                    value = ipDescription,
                    onValueChange = { ipDescription = it },
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 10.dp)
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        disabledBorderColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    label = { Text(text = "Description", color = Color.Black) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions {
                        saveTransaction()
                    },
                    singleLine = true,
                )

                // Save Button
                Button(
                    onClick = { saveTransaction() },
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(71, 63, 85, 255),
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Save", color = Color.White)
                }
            }
        }
    }
}
