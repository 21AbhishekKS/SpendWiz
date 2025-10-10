package com.spendwiz.app.utils

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.spendwiz.app.Database.money.Money
import com.spendwiz.app.R
import com.spendwiz.app.ViewModels.AddScreenViewModel
import com.spendwiz.app.navigation.Routes

@Composable
fun TransactionList(
    moneyList: List<Money>?,
    navController: NavController,
    viewModel: AddScreenViewModel
) {
    val isListEmpty = moneyList?.isEmpty() ?: true

    var showDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<Money?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isListEmpty) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(painter = painterResource(R.drawable.no_transaction), "" , modifier = Modifier.size(50.dp))
                Spacer(Modifier.height(20.dp))
                Text(text = "No Transactions today !",color = MaterialTheme.colorScheme.onBackground)
            }
        } else {
            LazyColumn {
                itemsIndexed(moneyList!!) { index, item ->
                    MoneyItemWithLongPress(item = item,
                        false,
                        onClick = {
                        val description = Uri.encode(item.description ?: "")
                        val amount = item.amount
                        val id = item.id
                        val type = Uri.encode(item.type.toString())
                        val category = Uri.encode(item.category ?: "")
                        val subCategory = Uri.encode(item.subCategory ?: "")
                        val date = Uri.encode(item.date ?: "")
                        val time = Uri.encode(item.time ?: "")

                        navController.navigate(
                            Routes.UpdateScreen.route +
                                    "?description=$description" +
                                    "&amount=$amount" +
                                    "&id=$id" +
                                    "&type=$type" +
                                    "&category=$category" +
                                    "&subCategory=$subCategory" +
                                    "&date=$date" +
                                    "&time=$time"
                        )
                    }, onLongClick = {
                            itemToDelete = item
                            showDialog = true
                        })

                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                // Hide the dialog if the user clicks outside of it
                showDialog = false
                itemToDelete = null
            },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this transaction?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // If confirmed, delete the item and hide the dialog
                        itemToDelete?.let {
                            viewModel.deleteMoney(it.id)
                        }
                        showDialog = false
                        itemToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        itemToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
