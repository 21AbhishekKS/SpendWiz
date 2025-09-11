package com.abhi.expencetracker.Screens

import android.app.DatePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abhi.expencetracker.Database.money.TransactionType
import com.abhi.expencetracker.ViewModels.AddScreenViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.abhi.expencetracker.Database.money.CategoryData.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UpdateScreen(
    viewModel: AddScreenViewModel,
    navController: NavController,
    description: String,
    amount: Double,
    id: Int,
    type: String,
    category: String = "",
    subCategory: String = "",
    date: String = ""
) {

    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val transactionTypes = listOf("Income", "Expense", "Transfer")
    val initialTypeEnum = try {
        TransactionType.valueOf(type.uppercase())   // works for "INCOME", "EXPENSE"
    } catch (e: Exception) {
        TransactionType.EXPENSE                     // fallback
    }

    var selectedType by rememberSaveable {
        mutableStateOf(
            when (initialTypeEnum) {
                TransactionType.INCOME -> "Income"
                TransactionType.EXPENSE -> "Expense"
                else -> "Transfer"
            }
        )
    }

    var selectedCategory by rememberSaveable { mutableStateOf(category) }
    var customCategory by rememberSaveable { mutableStateOf("") }
    var selectedSubCategory by rememberSaveable { mutableStateOf(subCategory) }
    var amount by rememberSaveable { mutableStateOf(amount.toString()) }
    var description by rememberSaveable { mutableStateOf(description) }

    // ✅ Date handling
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var currentDate by rememberSaveable { mutableStateOf(date.ifEmpty { dateFormatter.format(Date()) }) }
    val calendar = Calendar.getInstance()

    // Try parsing the incoming date into the calendar
    LaunchedEffect(Unit) {
        try {
            val parsed = dateFormatter.parse(currentDate)
            if (parsed != null) calendar.time = parsed
        } catch (_: Exception) {}
    }

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

    fun saveTransaction() {
        val amountDouble = amount.toDoubleOrNull()
        if (amountDouble == null) {
            Toast.makeText(context, "Invalid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val typeEnum = when (selectedType) {
            "Income" -> TransactionType.INCOME
            "Expense" -> TransactionType.EXPENSE
            else -> TransactionType.TRANSFER
        }


        val finalCategory = when {
            selectedCategory == "Others" && customCategory.isNotBlank() -> customCategory
            selectedCategory.isNotBlank() -> selectedCategory
            else -> "Others"
        }

        viewModel.updateMoney(
            id = id,
            amount = amountDouble,
            description = description.ifBlank { "No description" },
            type = typeEnum,
            category = finalCategory,
            subCategory = selectedSubCategory.ifBlank { "General" },
            date = currentDate
        )

        Toast.makeText(context, "Transaction updated!", Toast.LENGTH_SHORT).show()
        navController.popBackStack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // transaction type buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            transactionTypes.forEach { t ->
                val selectedColor = when (t) {
                    "Income" -> Color(0xFF4CAF50)
                    "Expense" -> Color(0xFFF44336)
                    else -> Color(0xFF2196F3)
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, if (selectedType == t) selectedColor else Color.Gray),
                    onClick = {
                        selectedType = t
                        selectedCategory = ""
                        selectedSubCategory = ""
                        customCategory = ""
                    }
                ) {
                    Text(
                        text = t,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        textAlign = TextAlign.Center,
                        color = if (selectedType == t) selectedColor else Color.DarkGray
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Date:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(90.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        // Parse the currently selected date into calendar
                        val parsedDate = try {
                            dateFormatter.parse(currentDate)
                        } catch (_: Exception) {
                            null
                        }
                        val cal = Calendar.getInstance()
                        if (parsedDate != null) cal.time = parsedDate

                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val pickedCal = Calendar.getInstance()
                                pickedCal.set(year, month, dayOfMonth)
                                currentDate = dateFormatter.format(pickedCal.time)
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
            ) {
                OutlinedTextField(
                    value = currentDate,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        disabledBorderColor = typeColor,
                        disabledTextColor = Color.Black
                    )
                )
            }
        }



        // amount row
        FieldRow(
            label = "Amount",
            value = amount,
            onValueChange = { if (it.isEmpty() || it.matches(Regex("^[0-9]*\\.?[0-9]*$"))) amount = it },
            keyboardType = KeyboardType.Number,
            borderColor = typeColor,
            modifier = Modifier.focusRequester(focusRequester)
        )

        // description
        FieldRow(label = "Note", value = description, onValueChange = { description = it }, borderColor = typeColor)

        // category dropdown
        var expanded by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text("Category:", fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(90.dp))
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
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                selectedCategory = cat
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
            FieldRow(label = "Custom", value = customCategory, onValueChange = { customCategory = it })
        } else if (selectedCategory.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
            Text("Update", color = Color.White, fontWeight = FontWeight.ExtraBold)
        }
    }
}

