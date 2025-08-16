package com.abhi.expencetracker.Screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abhi.expencetracker.Database.money.TransactionType
import com.abhi.expencetracker.Database.money.ViewModels.AddScreenViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddScreen(
    viewModel: AddScreenViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val transactionTypes = listOf("Income", "Expense", "Transfer")
    var selectedType by rememberSaveable { mutableStateOf(transactionTypes[0]) }

    val incomeCategories = listOf("Salary", "Business", "Investments", "Others")
    val expenseCategories = listOf("Food", "Transport", "Shopping", "Bills", "Misc", "Others")
    val transferCategories = listOf("Bank Transfer", "UPI", "Others")

    val incomeSubCategoryMap = mapOf(
        "Salary" to listOf("Monthly", "Bonus", "Overtime"),
        "Business" to listOf("Sales", "Services"),
        "Investments" to listOf("Stocks", "Crypto", "Bonds")
    )

    val expenseSubCategoryMap = mapOf(
        "Food" to listOf("Breakfast", "Lunch", "Dinner", "Snacks", "Groceries"),
        "Transport" to listOf("Bus", "Train", "Taxi", "Fuel", "Flight"),
        "Shopping" to listOf("Clothes", "Electronics", "Accessories", "Gifts"),
        "Bills" to listOf("Electricity", "Internet", "Water", "Mobile"),
        "Misc" to listOf("Donation", "Entertainment")
    )

    val transferSubCategoryMap = mapOf(
        "Bank Transfer" to listOf("Same Bank", "Other Bank"),
        "UPI" to listOf("Google Pay", "PhonePe", "Paytm")
    )

    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var customCategory by rememberSaveable { mutableStateOf("") }
    var selectedSubCategory by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy (EEE)   hh:mm a")
    val currentDateTime = LocalDateTime.now().format(dateFormatter)

    val categories = when (selectedType) {
        "Income" -> incomeCategories
        "Expense" -> expenseCategories
        else -> transferCategories
    }

    val subCategoryMap = when (selectedType) {
        "Income" -> incomeSubCategoryMap
        "Expense" -> expenseSubCategoryMap
        else -> transferSubCategoryMap
    }

    val typeColor = when (selectedType) {
        "Income" -> Color(0xFF4CAF50)
        "Expense" -> Color(0xFFF44336)
        else -> Color(0xFF2196F3)
    }

    fun Color.darken(factor: Float): Color {
        return Color(
            red = (red * (1 - factor)).coerceIn(0f, 1f),
            green = (green * (1 - factor)).coerceIn(0f, 1f),
            blue = (blue * (1 - factor)).coerceIn(0f, 1f),
            alpha = alpha
        )
    }

    fun saveTransaction() {
        if (amount.isBlank()) {
            Toast.makeText(context, "Enter amount", Toast.LENGTH_SHORT).show()
            return
        }
        val amountDouble = amount.toDoubleOrNull()
        if (amountDouble == null) {
            Toast.makeText(context, "Invalid amount", Toast.LENGTH_SHORT).show()
            return
        }
        val typeEnum = when (selectedType) {
            "Income" -> TransactionType.INCOME
            "Expense" -> TransactionType.EXPENSE
            else -> TransactionType.INCOME
        }
        val finalCategory = if (selectedCategory == "Others") customCategory else selectedCategory
        viewModel.addMoney1(
            id = 0,
            amount = amountDouble,
            description = description,
            type = typeEnum,
            category = finalCategory,
            subCategory = selectedSubCategory
        )
        navController.popBackStack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            transactionTypes.forEach { type ->
                val selectedColor = when (type) {
                    "Income" -> Color(0xFF4CAF50)
                    "Expense" -> Color(0xFFF44336)
                    else -> Color(0xFF2196F3)
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, if (selectedType == type) selectedColor else Color.Gray),
                    onClick = {
                        selectedType = type
                        selectedCategory = ""
                        selectedSubCategory = ""
                        customCategory = ""
                    }
                ) {
                    Text(
                        text = type,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        textAlign = TextAlign.Center,
                        color = if (selectedType == type) selectedColor else Color.DarkGray
                    )
                }
            }
        }

        FieldRow(label = "Date", readOnlyText = currentDateTime)
        FieldRow(
            label = "Amount",
            value = amount,
            onValueChange = { if (it.matches(Regex("^[0-9]*$"))) amount = it },
            keyboardType = KeyboardType.Number,
            borderColor = typeColor,
            modifier = Modifier.focusRequester(focusRequester)
        )
        FieldRow(
            label = "Note",
            value = description,
            onValueChange = { description = it },
            borderColor = typeColor
        )

        var expanded by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Category:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(90.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.weight(1f)
            ) {
                TextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor(),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = typeColor,
                        unfocusedIndicatorColor = Color.Gray,
                        focusedTrailingIconColor = typeColor
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                selectedSubCategory = ""
                                customCategory = ""
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        if (selectedCategory == "Others") {
            FieldRow(
                label = "Custom",
                value = customCategory,
                onValueChange = { customCategory = it }
            )
        } else if (selectedCategory.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                subCategoryMap[selectedCategory]?.forEach { sub ->
                    FilterChip(
                        selected = selectedSubCategory == sub,
                        onClick = { selectedSubCategory = sub },
                        label = { Text(sub, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFFE0E0E0),
                            selectedContainerColor = typeColor,
                            labelColor = Color.Black,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { saveTransaction() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = typeColor),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Save", color = Color.White, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldRow(
    label: String,
    value: String = "",
    onValueChange: (String) -> Unit = {},
    readOnlyText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    fieldBackgroundColor: Color = Color.Transparent,
    borderColor: Color = Color.Gray,
    cursorColor: Color = Color.Blue,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "$label:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(90.dp)
        )
        if (readOnlyText != null) {
            Text(
                text = readOnlyText,
                fontSize = 16.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp)
            )
        } else {
            TextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                modifier = modifier.weight(1f),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = fieldBackgroundColor,
                    cursorColor = borderColor,
                    focusedIndicatorColor = borderColor,
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = keyboardType
                )
            )
        }
    }
}