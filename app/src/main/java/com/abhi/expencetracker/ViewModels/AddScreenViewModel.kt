package com.abhi.expencetracker.ViewModels

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.aay.compose.donutChart.model.PieChartData
import com.abhi.expencetracker.Database.money.CategoryExpense
import com.abhi.expencetracker.MainApplication
import com.abhi.expencetracker.Database.money.Money
import com.abhi.expencetracker.Database.money.TransactionType
import com.abhi.expencetracker.MainActivity
import com.abhi.expencetracker.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class AddScreenViewModel : ViewModel() {

    val moneyDao = MainApplication.moneyDatabase.getMoneyDao()

    @RequiresApi(Build.VERSION_CODES.O)
    private val today = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    private val customFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    @RequiresApi(Build.VERSION_CODES.O)
    private val formattedDateCustom = today.format(customFormatter)

    @RequiresApi(Build.VERSION_CODES.O)
    val todayMoneyList: LiveData<List<Money>> = moneyDao.getTodayTransactionByDate(formattedDateCustom)

    fun updateMoney(
        id: Int,
        amount: Double,
        description: String,
        type: TransactionType,
        category: String,
        subCategory: String,
        date: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        val existing = moneyDao.getMoneyById(id) ?: return@launch

        val updated = existing.copy(
            amount = amount,
            description = description,
            type = type,
            category = category,
            subCategory = subCategory,
            date = date
        )
        moneyDao.updateMoney(updated)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun addMoney1(
        id: Int = 0,
        amount: Double,
        description: String,
        type: TransactionType,
        date: String,
        category: String = "Others",
        subCategory: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val money = Money(
                id = id,
                amount = amount,
                description = description,
                type = type,
                date = date,
                category = category,
                subCategory = subCategory
            )
            moneyDao.addMoney(money)
        }
    }


    fun getCategoryExpensesForMonth(month: String, year: String): LiveData<List<PieChartData>> {
        return moneyDao.getCategoryExpensesByMonthAndYear(month, year).map { categoryList ->
            mapCategoryExpensesToPieChartData(categoryList)
        }
    }

    private fun mapCategoryExpensesToPieChartData(expenses: List<CategoryExpense>): List<PieChartData> {
        val colors = listOf(
            Color(0xFF8B0000), // Very Dark Red (Maroon)
            Color(0xFFC62828), // Dark Red
            Color(0xFF6A1B1A), // Red-Brown
            Color(0xFFFFCDD2), // Light Red
            Color(0xFF4E342E), // Coffee Brown
            Color(0xFFEF9A9A), // Soft Red
            Color(0xFF3E2723), // Dark Coffee
            Color(0xFFE57373), // Medium Red
            Color(0xFF5D4037),  // Rich Brown
            Color(0xFFB71C1C)  // Deep Red
        )

        return expenses.mapIndexed { index, item ->
            PieChartData(
                partName = item.category+": "+item.total,
                data = item.total,
                color = colors[index % colors.size]
            )
        }
    }

    fun deleteMoney(id: Int) {
        Log.v( "delete", "delete method called")

        viewModelScope.launch(Dispatchers.IO) {
            val money = moneyDao.getMoneyById(id)
            if (money != null) {
                moneyDao.deleteMoney(money)
            }
        }
    }

    // for list of money items from insight screen
    fun updateTransactionType(ids: List<Int>, newType: TransactionType) =
        viewModelScope.launch { moneyDao.updateTransactionType(ids, newType) }

    fun updateCategory(ids: List<Int>, newCategory: String) =
        viewModelScope.launch { moneyDao.updateCategory(ids, newCategory) }

    fun updateSubCategory(ids: List<Int>, newSubCategory: String) =
        viewModelScope.launch { moneyDao.updateSubCategory(ids, newSubCategory) }

    fun deleteTransactions(ids: List<Int>) =
        viewModelScope.launch { moneyDao.deleteTransactions(ids) }

    private fun showTransactionNotification(context: Context, money: Money) {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "transaction_channel")
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("New ${money.type.name.lowercase().replaceFirstChar { it.uppercase() }} added")
            .setContentText("${money.bankName}: ₹${money.amount} on ${money.date}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            manager.notify(System.currentTimeMillis().toInt(), notification)
        }
    }

}
