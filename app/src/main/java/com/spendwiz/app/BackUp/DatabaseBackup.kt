package com.spendwiz.app.BackUp

import com.spendwiz.app.Database.money.Category
import com.spendwiz.app.Database.money.Money
import com.spendwiz.app.Database.money.SubCategory
import kotlinx.serialization.Serializable

@Serializable
data class DatabaseBackup(
    val money: List<Money> = emptyList(),
    val categories: List<Category> = emptyList(),
    val subCategories: List<SubCategory> = emptyList()
)
