package com.abhi.expencetracker.Database.money.CategoryData

// Main transaction types
val transactionTypes = listOf("Income", "Expense", "Transfer")

// Categories for each type
val incomeCategories = listOf("Salary", "Business", "Investments", "Others")
val expenseCategories = listOf("Food", "Transport", "Shopping", "Bills", "Misc", "Others")
val transferCategories = listOf("Bank Transfer", "UPI", "Others")

// Subcategories for each category
val incomeSubCategoryMap = mapOf(
    "Salary" to listOf("Monthly", "Bonus", "Overtime"),
    "Business" to listOf("Sales", "Services"),
    "Investments" to listOf("Stocks", "Crypto", "Bonds")
)

val expenseSubCategoryMap = mapOf(
    "Food" to listOf("Breakfast", "Lunch", "Dinner", "Snacks", "Groceries"),
    "Transport" to listOf("Bus", "Train", "Taxi", "Fuel", "Flight"),
    "Shopping" to listOf("Clothes", "Electronics", "Accessories", "Gifts"),
    "Bills" to listOf("Electricity", "Internet", "Water", "Mobile"),
    "Misc" to listOf("Donation", "Entertainment")
)

val transferSubCategoryMap = mapOf(
    "Bank Transfer" to listOf("Same Bank", "Other Bank"),
    "UPI" to listOf("Google Pay", "PhonePe", "Paytm")
)
