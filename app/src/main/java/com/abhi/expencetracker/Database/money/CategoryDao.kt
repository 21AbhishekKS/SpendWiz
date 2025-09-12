package com.abhi.expencetracker.Database.money


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete

@Dao
interface CategoryDao {

    // Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubCategory(subCategory: SubCategory)

    // Fetch
    @Query("SELECT * FROM categories WHERE type = :type")
    suspend fun getCategoriesByType(type: String): List<Category>

    @Query("SELECT * FROM subcategories WHERE categoryId = :categoryId")
    suspend fun getSubCategories(categoryId: Int): List<SubCategory>

    // Delete
    @Delete
    suspend fun deleteCategory(category: Category)

    @Delete
    suspend fun deleteSubCategory(subCategory: SubCategory)
}
