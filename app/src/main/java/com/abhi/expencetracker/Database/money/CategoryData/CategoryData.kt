package com.abhi.expencetracker.Database.money.CategoryData

// Main transaction types
val transactionTypes = listOf("Income", "Expense", "Transfer")

// Categories for each type
val incomeCategories = listOf(
    "Salary", "Business", "Investments", "Gifts", "Refunds",
    "Rental Income", "Dividends", "Interest", "Tax Refund", "Others"
)

val expenseCategories = listOf(
    "Food", "Transport", "Shopping", "Bills", "Housing",
    "Health", "Education", "Travel", "Misc", "Entertainment",
    "Debt Payments", "Insurance", "Taxes", "Personal Care", "Pets", "Others"
)

val transferCategories = listOf(
    "Bank Transfer", "UPI", "Cash Withdrawal", "Cash Deposit", "Others"
)

// Subcategories for each category
val incomeSubCategoryMap = mapOf(
    "Salary" to listOf("Monthly", "Bonus", "Overtime", "Commission"),
    "Business" to listOf("Sales", "Services", "Freelance", "Consulting"),
    "Investments" to listOf("Stocks", "Crypto", "Bonds", "Mutual Funds"),
    "Gifts" to listOf("Cash Gift", "Other Gifts"),
    "Refunds" to listOf("Online Purchase", "Store Refund"),
    "Rental Income" to listOf("Apartment", "Property"),
    "Dividends" to listOf("Stocks", "Mutual Funds"),
    "Interest" to listOf("Savings Account", "Fixed Deposit")
)

val expenseSubCategoryMap = mapOf(
    "Food" to listOf("Breakfast", "Lunch", "Dinner", "Snacks", "Groceries", "Dining Out"),
    "Transport" to listOf("Bus", "Train", "Taxi", "Fuel", "Flight", "Ride-Sharing", "Tolls", "Parking"),
    "Shopping" to listOf("Clothes", "Electronics", "Accessories", "Gifts", "Hobbies", "Books"),
    "Bills" to listOf("Electricity", "Internet", "Water", "Mobile", "Subscriptions", "Cable TV"),
    "Housing" to listOf("Rent", "Mortgage", "Maintenance", "Renovations", "Property Tax"),
    "Health" to listOf("Doctor's Visit", "Medicine", "Gym", "Insurance Premium", "Dental Care", "Eye Care"),
    "Education" to listOf("Tuition Fees", "Books", "Courses", "School Supplies"),
    "Travel" to listOf("Accommodation", "Sightseeing", "Tickets", "Foreign Currency"),
    "Misc" to listOf("Donation", "Child Care", "Professional Fees"),
    "Entertainment" to listOf("Movies", "Concerts", "Gaming", "Streaming Services"),
    "Debt Payments" to listOf("Credit Card", "Loan", "Mortgage"),
    "Insurance" to listOf("Life", "Health", "Vehicle", "Home"),
    "Taxes" to listOf("Income Tax", "Property Tax", "Sales Tax"),
    "Personal Care" to listOf("Haircut", "Salon Services", "Cosmetics"),
    "Pets" to listOf("Food", "Veterinary", "Grooming", "Toys", "Accessories")
)

val transferSubCategoryMap = mapOf(
    "Bank Transfer" to listOf("Same Bank", "Other Bank", "Loan Payment"),
    "UPI" to listOf("Google Pay", "PhonePe", "Paytm", "BHIM"),
    "Cash Withdrawal" to listOf("ATM", "Bank Counter"),
    "Cash Deposit" to listOf("ATM", "Bank Counter")
)