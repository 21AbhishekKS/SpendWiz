package com.abhi.expencetracker.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhi.expencetracker.Database.money.Category
import com.abhi.expencetracker.Database.money.CategoryDao
import com.abhi.expencetracker.Database.money.MoneyDatabase
import com.abhi.expencetracker.Database.money.SubCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
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
