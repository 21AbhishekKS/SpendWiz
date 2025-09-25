package com.spendwiz.app.Screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spendwiz.app.Database.money.TransactionType
import com.spendwiz.app.ViewModels.AddScreenViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.spendwiz.app.Database.money.Money
import com.spendwiz.app.ViewModels.CategoryViewModel
import com.spendwiz.app.utils.TimePickerField
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddScreen(
    viewModel: AddScreenViewModel,
    categoryViewModel: CategoryViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val transactionTypes = listOf("Income", "Expense", "Transfer")
    var selectedType by rememberSaveable { mutableStateOf(transactionTypes[0]) }

    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var customCategory by rememberSaveable { mutableStateOf("") }
    var selectedSubCategory by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var selectedTime by rememberSaveable {
        mutableStateOf(Money.getCurrentTime())
    }
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var currentDate by rememberSaveable { mutableStateOf(dateFormatter.format(Date())) }
    val calendar = Calendar.getInstance()

    // collect categories
    val incomeCategories by categoryViewModel.getCategories("Income").collectAsState(initial = emptyList())
    val expenseCategories by categoryViewModel.getCategories("Expense").collectAsState(initial = emptyList())
    val transferCategories by categoryViewModel.getCategories("Transfer").collectAsState(initial = emptyList())

    val categories: List<String> = when (selectedType) {
        "Income" -> incomeCategories.map { it.name }
        "Expense" -> expenseCategories.map { it.name }
        else -> transferCategories.map { it.name }
    }

    val selectedCategoryObj = when (selectedType) {
        "Income" -> incomeCategories.find { it.name == selectedCategory }
        "Expense" -> expenseCategories.find { it.name == selectedCategory }
        else -> transferCategories.find { it.name == selectedCategory }
    }

    val subCategories by remember(selectedCategoryObj?.id) {
        selectedCategoryObj?.id?.let { categoryId ->
            categoryViewModel.getSubCategories(categoryId)
        } ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    val subCategoryNames = subCategories.map { it.name }

    val typeColor = when (selectedType) {
        "Income" -> Color(0xFF4CAF50)
        "Expense" -> Color(0xFFF44336)
        else -> Color(0xFF2196F3)
    }

    val colorScheme = MaterialTheme.colorScheme

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
        if (amountDouble > 99_99_999) {
            Toast.makeText(context, "Amount cannot exceed 1 crore", Toast.LENGTH_SHORT).show()
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

        viewModel.addMoney1(
            id = 0,
            amount = amountDouble,
            description = description.ifBlank { "No description" },
            type = typeEnum,
            date = currentDate,
            time = selectedTime,
            category = finalCategory,
            subCategory = selectedSubCategory.ifBlank { "General" }
        )
        Toast.makeText(context, "Transaction saved!", Toast.LENGTH_SHORT).show()
        navController.popBackStack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
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
                    color = colorScheme.surface,
                    border = BorderStroke(1.dp, if (selectedType == t) selectedColor else colorScheme.outline),
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
                        color = if (selectedType == t) selectedColor else colorScheme.onSurface
                    )
                }
            }
        }

        // Date Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Date:", fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(90.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        val parsedDate = try { dateFormatter.parse(currentDate) } catch (_: Exception) { null }
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
                TextField(
                    value = currentDate,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("Select Date") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        disabledTextColor = colorScheme.onSurface,
                        disabledIndicatorColor = colorScheme.outline,
                        disabledLabelColor = colorScheme.outline,
                        disabledContainerColor = Color.Transparent
                    )
                )
            }
        }

        // Time Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Time:", fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(90.dp))
            Box(modifier = Modifier.weight(1f)) {
                TimePickerField(
                    selectedTime = selectedTime,
                    onTimeSelected = { newTime -> selectedTime = newTime }
                )
            }
        }

        // Amount
        FieldRow(
            label = "Amount",
            value = amount,
            onValueChange = {
                if (it.isEmpty() || it.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
                    val parsed = it.toDoubleOrNull()
                    if (parsed == null || parsed <= 99_99_999) amount = it
                }
            },
            keyboardType = KeyboardType.Number,
            borderColor = typeColor,
            modifier = Modifier.focusRequester(focusRequester)
        )

        // Note
        FieldRow(label = "Note", value = description, onValueChange = { description = it }, borderColor = typeColor)

        // Category
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
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
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
                subCategoryNames.forEach { sub ->
                    FilterChip(
                        selected = selectedSubCategory == sub,
                        onClick = { selectedSubCategory = sub },
                        label = { Text(sub, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = colorScheme.surfaceVariant,
                            selectedContainerColor = typeColor,
                            labelColor = colorScheme.onSurface,
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
        modifier = Modifier.fillMaxWidth(),
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
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = fieldBackgroundColor,
                    unfocusedContainerColor = fieldBackgroundColor,
                    cursorColor = borderColor,
                    focusedIndicatorColor = borderColor,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline

                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = keyboardType
                )
            )
        }
    }
}
