package com.abhi.expencetracker.Screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhi.expencetracker.Database.money.MoneyDatabase
import com.abhi.expencetracker.ViewModels.CategoryViewModel

@Composable
fun ManageCategoriesScreen(viewModel : CategoryViewModel) {
    var selectedType by remember { mutableStateOf("Expense") }
    var newCategoryName by remember { mutableStateOf("") }

  //  val context = LocalContext.current
 //   val dao = remember { MoneyDatabase.getDatabase(context).getCategoryDao() }

//    val viewModel: CategoryViewModel = viewModel(
//        factory = CategoryViewModelFactory(dao)
//    )

    // ✅ Load categories whenever selectedType changes
    LaunchedEffect(selectedType) {
        viewModel.loadCategories(selectedType)
    }

    Column(Modifier.padding(16.dp)) {
        // Dropdown for type
        DropdownMenuBox(
            label = "Transaction Type",
            items = listOf("Income", "Expense", "Transfer"),
            selected = selectedType,
            onItemSelected = { selectedType = it } // triggers LaunchedEffect
        )

        Spacer(Modifier.height(8.dp))

        // Categories list
        val categories by viewModel.categories.collectAsState(emptyList())
        LazyColumn {
            items(categories) { category ->
                var expanded by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    // Category row (acts like table header)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row {
                            Icon(
                                if (expanded) Icons.Default.ArrowDropDown else Icons.Default.KeyboardArrowRight,
                                contentDescription = "Expand"
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(category.name, style = MaterialTheme.typography.bodyMedium)
                        }

                        // ❌ Only allow delete if not default
                        if (!category.isDefault) {
                            IconButton(onClick = { viewModel.deleteCategory(category) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }

                    if (expanded) {
                        val subcategories by viewModel.getSubCategories(category.id).collectAsState(emptyList())
                        subcategories.forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 32.dp, top = 2.dp, bottom = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = sub.name, style = MaterialTheme.typography.bodySmall)
                                if (!sub.isDefault) {
                                    IconButton(onClick = { viewModel.deleteSubCategory(sub) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                                    }
                                }
                            }
                        }

                        // Add new subcategory inline
                        var newSub by remember { mutableStateOf("") }
                        Row(Modifier.padding(start = 32.dp, top = 4.dp)) {
                            OutlinedTextField(
                                value = newSub,
                                onValueChange = { newSub = it },
                                label = { Text("Add Subcategory") },
                                modifier = Modifier.weight(1f)
                            )
                            Button(onClick = {
                                if (newSub.isNotBlank()) {
                                    viewModel.addSubCategory(category.id, newSub)
                                    newSub = ""
                                }
                            }) { Text("+") }
                        }
                    }
                }
            }
        }

        // Add category field
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = newCategoryName,
            onValueChange = { newCategoryName = it },
            label = { Text("New Category") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                if (newCategoryName.isNotBlank()) {
                    viewModel.addCategory(selectedType, newCategoryName)
                    newCategoryName = ""
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Add Category")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuBox(
    label: String,
    items: List<String>,
    selected: String,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
