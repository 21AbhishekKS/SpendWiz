package com.spendwiz.app.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.aay.compose.donutChart.DonutChart
import com.spendwiz.app.ViewModels.AddScreenViewModel

@Composable
fun ExpenseDonutChartBySubCategory(
    viewModel: AddScreenViewModel,
    month: String,
    year: String,
    modifier: Modifier
) {
    val categoryList by viewModel.getCategoryExpensesForMonth(month, year)
        .observeAsState(emptyList())
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // Text acts as a dropdown trigger
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(top = 8.dp)
            ) {
                val displayText: AnnotatedString = if (selectedCategory == null) {
                    buildAnnotatedString {
                        append("Select Category")
                        addStyle(
                            style = SpanStyle(textDecoration = TextDecoration.Underline),
                            start = 0,
                            end = length
                        )
                    }
                } else {
                    buildAnnotatedString {
                        append("Detailed Breakdown of ")
                        val start = length
                        append(selectedCategory!!)
                        addStyle(
                            style = SpanStyle(textDecoration = TextDecoration.Underline),
                            start = start,
                            end = length
                        )
                    }
                }

                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown Arrow",
                    // Use theme color for the icon tint
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categoryList.forEach { item ->
                    val categoryName = item.partName.split(":")[0]
                    DropdownMenuItem(
                        text = { Text(categoryName) },
                        onClick = {
                            selectedCategory = categoryName
                            expanded = false
                        }
                    )
                }
            }
        }

        // Show subcategory chart only if a category is chosen
        selectedCategory?.let { category ->
            val subCategoryData by viewModel
                .getSubCategoryExpensesForMonth(category, month, year)
                .observeAsState(emptyList())

            if (subCategoryData.isNotEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    DonutChart(
                        modifier = modifier,
                        pieChartData = subCategoryData,
                        centerTitle = category,
                        centerTitleStyle = TextStyle(color = MaterialTheme.colorScheme.primary),
                        outerCircularColor = MaterialTheme.colorScheme.surfaceVariant,
                        innerCircularColor = MaterialTheme.colorScheme.outline,
                        ratioLineColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            } else {
                Text(
                    text = "No subcategory data for $category",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}