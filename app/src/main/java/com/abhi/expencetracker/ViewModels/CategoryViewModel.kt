package com.abhi.expencetracker.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhi.expencetracker.Database.money.Category
import com.abhi.expencetracker.Database.money.CategoryDao
import com.abhi.expencetracker.Database.money.SubCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class CategoryViewModel(private val dao: CategoryDao) : ViewModel() {
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    fun loadCategories(type: String) {
        viewModelScope.launch {
            _categories.value = dao.getCategoriesByType(type)
        }
    }

    fun getSubCategories(categoryId: Int): Flow<List<SubCategory>> = flow {
        emit(dao.getSubCategories(categoryId))
    }

    fun addCategory(type: String, name: String) {
        viewModelScope.launch {
            dao.insertCategory(Category(type = type, name = name, isDefault = false))
            loadCategories(type)
        }
    }

    fun addSubCategory(categoryId: Int, name: String) {
        viewModelScope.launch {
            dao.insertSubCategory(SubCategory(categoryId = categoryId, name = name, isDefault = false))
            loadCategories(_categories.value.firstOrNull()?.type ?: "Expense")
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            dao.deleteCategory(category)
            loadCategories(category.type)
        }
    }

    fun deleteSubCategory(sub: SubCategory) {
        viewModelScope.launch {
            dao.deleteSubCategory(sub)
            loadCategories(_categories.value.firstOrNull()?.type ?: "Expense")
        }
    }
}
