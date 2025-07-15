package com.abhi.expencetracker.Database.money

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "money")
data class Money(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val amount: Double,
    val description: String,
    val type: TransactionType,
    val date: String,
    val upiRefNo: String? = null
)
