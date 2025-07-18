package com.abhi.expencetracker.Screens
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.expencetracker.Database.money.ViewModels.AddScreenViewModel
import com.abhi.expencetracker.utils.MoneyItem1
import java.time.LocalDate


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionScreen(viewModel: AddScreenViewModel ){

    val today = LocalDate.now()
    val formattedMonth = "%02d".format(today.monthValue)
    var showDialog by remember { mutableStateOf(false) }


    var context = LocalContext.current
    val moneyList1 by viewModel.moneyDao.getTransactionsByMonthAndYear(formattedMonth , today.year.toString()).observeAsState()
    var currentDate = ""
    var selectedItemId by remember { mutableStateOf(-1) }


    val scrollableState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollableState),
    ) {
        if (moneyList1?.reversed().isNullOrEmpty()) {
            Text(
                "No transactions found",
                Modifier
                    .background(Color.DarkGray)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                color = Color.White,
            )
            Text(
                text = "No History in ${today.month}",
                color = Color.Black
            )
        } else {
            Row(
                Modifier.fillMaxWidth().wrapContentHeight(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "History of ${today.month} ${today.year}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )
            }

            moneyList1?.reversed()?.forEachIndexed { _, item ->
                if (currentDate != item.date) {
                    Text(
                        text = item.date,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp),
                        color = Color.Black
                    )
                    currentDate = item.date
                }
                MoneyItem1(item = item) {
                    selectedItemId = item.id
                    showDialog = true
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Delete Transaction") },
            text = { Text(text = "Are you sure you want to delete this transaction?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMoney(selectedItemId)
                    showDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}
