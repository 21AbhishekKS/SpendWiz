package com.abhi.expencetracker.Database.money

data class TransactionWithDate(
    val dateString: String,
    val moneyList: List<Money>
)
