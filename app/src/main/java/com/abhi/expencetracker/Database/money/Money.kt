package com.abhi.expencetracker.Database.money

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "money",
    indices = [Index(value = ["upiRefNo"], unique = true)]
)data class Money(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val amount: Double,
    val description: String,
    val type: TransactionType,
    val date: String,
    val upiRefNo: String? = null,
    val bankName: String? = null,
    val category: String = "Others",
    val subCategory: String? = null
)
