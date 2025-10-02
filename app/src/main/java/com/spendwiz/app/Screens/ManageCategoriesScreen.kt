package com.spendwiz.app.Screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spendwiz.app.Database.money.Category
import com.spendwiz.app.R
import com.spendwiz.app.ViewModels.CategoryViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(viewModel: CategoryViewModel) {
    var selectedType by remember { mutableStateOf("Expense") }
    var newCategoryName by remember { mutableStateOf("") }

    val categories by viewModel.getCategories(selectedType).collectAsState(initial = emptyList())

    // Dialog states
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showAddSubCategoryDialog by remember { mutableStateOf(false) }
    var categoryForNewSub by remember { mutableStateOf<Category?>(null) }
    var deleteAction: (() -> Unit)? by remember { mutableStateOf(null) }

    // Custom Color
    val buttonColor = colorResource(id = R.color.button_color)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with Reset button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Manage Categories",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showResetDialog = true }) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset Categories")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Dropdown for transaction type
        DropdownMenuBox(
            label = "Transaction Type",
            items = listOf("Income", "Expense", "Transfer"),
            selected = selectedType,
            onItemSelected = { selectedType = it }
        )

        Spacer(Modifier.height(16.dp))

        // Categories list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories, key = { it.id }) { category ->
                var expanded by remember { mutableStateOf(false) }

                Card(
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Category header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                                    contentDescription = if (expanded) "Collapse" else "Expand"
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    category.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(
                                onClick = {
                                    deleteAction = { viewModel.deleteCategory(category) }
                                    showDeleteDialog = true
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Category",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        // Expanded view with subcategory chips
                        if (expanded) {
                            val subcategories by viewModel.getSubCategories(category.id).collectAsState(emptyList())

                            Divider(modifier = Modifier.padding(horizontal = 16.dp))

                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                subcategories.forEach { sub ->
                                    SubCategoryChip(
                                        text = sub.name,
                                        onDelete = {
                                            deleteAction = { viewModel.deleteSubCategory(sub) }
                                            showDeleteDialog = true
                                        }
                                    )
                                }

                                // Add new subcategory button
                                IconButton(
                                    onClick = {
                                        categoryForNewSub = category
                                        showAddSubCategoryDialog = true
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add Subcategory"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add category field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newCategoryName,
                onValueChange = { newCategoryName = it },
                label = { Text("New Category Name") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (newCategoryName.isNotBlank()) {
                        viewModel.addCategory(selectedType, newCategoryName)
                        newCategoryName = ""
                    }
                },
                modifier = Modifier.height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    }

    // --- Dialogs ---

    // Delete confirmation dialog
    if (showDeleteDialog) {
        ConfirmationDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                deleteAction?.invoke()
                showDeleteDialog = false
            },
            title = "Confirm Deletion",
            text = "Are you sure you want to delete this item? This action cannot be undone.",
            confirmButtonText = "Delete"
        )
    }

    // Reset categories confirmation dialog
    if (showResetDialog) {
        ConfirmationDialog(
            onDismiss = { showResetDialog = false },
            onConfirm = {
                viewModel.resetDatabase()
                showResetDialog = false
            },
            title = "Reset All Categories",
            text = "This will erase all your custom categories and subcategories, restoring the defaults. Are you sure?",
            confirmButtonText = "Reset"
        )
    }

    // Add new subcategory dialog
    if (showAddSubCategoryDialog) {
        var newSubName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddSubCategoryDialog = false },
            title = { Text("Add Subcategory") },
            text = {
                OutlinedTextField(
                    value = newSubName,
                    onValueChange = { newSubName = it },
                    label = { Text("Subcategory Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSubName.isNotBlank()) {
                            categoryForNewSub?.let {
                                viewModel.addSubCategory(it.id, newSubName)
                            }
                            showAddSubCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubCategoryDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubCategoryChip(text: String, onDelete: () -> Unit) {
    InputChip(
        selected = false,
        onClick = { /* Chips are for display/delete only */ },
        label = { Text(text) },
        shape = CircleShape,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete Subcategory",
                modifier = Modifier
                    .size(InputChipDefaults.IconSize)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null, // No ripple effect
                        onClick = onDelete
                    ),
            )
        },
        colors = InputChipDefaults.inputChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    )
}

@Composable
fun ConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    text: String,
    confirmButtonText: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}