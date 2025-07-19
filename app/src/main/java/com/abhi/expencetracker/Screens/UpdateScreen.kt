package com.abhi.expencetracker.Screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abhi.expencetracker.Database.money.TransactionType
import com.abhi.expencetracker.Database.money.ViewModels.AddScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UpdateScreen(
    viewModel: AddScreenViewModel,
    navController: NavController,
    passedDescription: String,
    passedAmount: String,
    id: Int,
    type: String
) {
    val context = LocalContext.current

    // Categories and subcategories
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

    // Transaction type mapping
    val transactionTypeMap = mapOf(
        "Spent" to TransactionType.EXPENSE,
        "Received" to TransactionType.INCOME,
    )
    val displayTypeMap = transactionTypeMap.entries.associate { (k, v) -> v to k }
    val listOfTransactionType = transactionTypeMap.keys.toList()

    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var ipTransactionType by rememberSaveable {
        mutableStateOf(transactionTypeMap[type] ?: TransactionType.EXPENSE)
    }

    var ipMoney by rememberSaveable { mutableStateOf(passedAmount) }
    var ipDescription by rememberSaveable { mutableStateOf(passedDescription) }

    val backgroundAnimatable = remember {
        Animatable(
            when (ipTransactionType) {
                TransactionType.EXPENSE -> Color(255, 87, 51)
                TransactionType.INCOME -> Color(125, 218, 88)
                else -> Color(93, 226, 231)
            }
        )
    }

    // Animate background color based on type
    LaunchedEffect(ipTransactionType) {
        val targetColor = when (ipTransactionType) {
            TransactionType.EXPENSE -> Color(240, 59, 46)
            TransactionType.INCOME -> Color(90, 187, 94)
            else -> Color(75, 98, 228)
        }

        backgroundAnimatable.animateTo(
            targetColor,
            animationSpec = tween(durationMillis = 500)
        )
    }

    fun saveTransaction() {
        if (ipMoney.isBlank() || ipDescription.isBlank()) {
            Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
        } else {
            val amount = ipMoney.toDoubleOrNull()
            if (amount != null) {
                viewModel.addMoney1(
                    id = id,
                    amount = amount,
                    description = ipDescription,
                    type = ipTransactionType,
                    category = selectedCategory,
                    subCategory = selectedSubCategory
                )
                ipMoney = ""
                ipDescription = ""
                selectedSubCategory = ""
                navController.popBackStack()
            } else {
                Toast.makeText(context, "Amount must be a number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(contentAlignment = Alignment.BottomStart) {
        Surface(color = backgroundAnimatable.value, modifier = Modifier.fillMaxSize()) {
            Column(
                Modifier.padding(top = 25.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = displayTypeMap[ipTransactionType] ?: "Transaction",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 25.sp,
                )
            }
        }

        Surface(
            color = Color.White,
            modifier = Modifier
                .fillMaxHeight(0.8f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(60.dp).copy(
                bottomEnd = ZeroCornerSize,
                bottomStart = ZeroCornerSize
            )
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
                        value = displayTypeMap[ipTransactionType] ?: "Transaction",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
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
                            .fillMaxWidth()
                    ) {
                        listOfTransactionType.forEach { label ->
                            DropdownMenuItem(
                                text = { Text(text = label, color = Color.Black) },
                                onClick = {
                                    ipTransactionType = transactionTypeMap[label] ?: TransactionType.EXPENSE
                                    isExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
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
                val numberPattern = Regex("^[0-9]*\\.?[0-9]*$")
                OutlinedTextField(
                    value = ipMoney,
                    onValueChange = {
                        if (numberPattern.matches(it)) ipMoney = it
                    },
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    label = { Text("Amount", color = Color.Black) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
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
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    label = { Text("Description", color = Color.Black) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions { saveTransaction() },
                    singleLine = true
                )

                // Save Button
                Button(
                    onClick = { saveTransaction() },
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(71, 63, 85),
                        contentColor = Color.White
                    )
                ) {
                    Text("Save", color = Color.White)
                }
            }
        }
    }
}