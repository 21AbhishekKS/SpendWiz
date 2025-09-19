package com.spendwiz.app.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendwiz.app.Database.money.Category
import com.spendwiz.app.Database.money.CategoryDao
import com.spendwiz.app.Database.money.MoneyDatabase
import com.spendwiz.app.Database.money.SubCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CategoryViewModel(private val dao: CategoryDao) : ViewModel() {

    fun getCategories(type: String): Flow<List<Category>> {
        return dao.getCategoriesByType(type)
    }

    fun getSubCategories(categoryId: Int): Flow<List<SubCategory>> {
        return dao.getSubCategories(categoryId)
    }

    fun addCategory(type: String, name: String) {
        viewModelScope.launch {
            dao.insertCategory(Category(type = type, name = name, isDefault = false))
        }
    }

    fun addSubCategory(categoryId: Int, name: String) {
        viewModelScope.launch {
            dao.insertSubCategory(SubCategory(categoryId = categoryId, name = name, isDefault = false))
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            dao.deleteCategory(category)
        }
    }

    fun deleteSubCategory(sub: SubCategory) {
        viewModelScope.launch {
            dao.deleteSubCategory(sub)
        }
    }

    fun resetDatabase() {
        viewModelScope.launch {
            // Delete everything first
            dao.clearAllCategories()

            // Reinsert default data
            MoneyDatabase.insertDefaultData(dao)
        }
    }
}
