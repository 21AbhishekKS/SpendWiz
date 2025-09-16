package com.abhi.expencetracker.Database.money

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(
    tableName = "money",
    indices = [
        Index(value = ["date"]),
        Index(value = ["category"]),
        Index(value = ["subCategory"]),
        Index(value = ["upiRefNo"], unique = true)
    ]
)data class Money(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val amount: Double,
    val description: String,
    val type: TransactionType,
    val date: String,
    val time: String = getCurrentTime(),   // stores only time (default current)
    val upiRefNo: String? = null,
    val bankName: String? = null,
    val category: String = "Others",
    val subCategory: String? = null
){
    companion object {
        fun getCurrentTime(): String {
            return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        }
    }
}
