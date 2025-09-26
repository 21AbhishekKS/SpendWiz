package com.spendwiz.app.Database.money


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubCategory(subCategory: SubCategory)

    // Reactive queries
    @Query("SELECT * FROM categories WHERE type = :type")
    fun getCategoriesByType(type: String): Flow<List<Category>>

    @Query("SELECT * FROM subcategories WHERE categoryId = :categoryId")
    fun getSubCategories(categoryId: Int): Flow<List<SubCategory>>

    // One-time suspend query (for prepopulate only ✅)
    @Query("SELECT * FROM categories WHERE type = :type")
    suspend fun getCategoriesByTypeOnce(type: String): List<Category>

    @Delete
    suspend fun deleteCategory(category: Category)

    @Delete
    suspend fun deleteSubCategory(subCategory: SubCategory)

    @Query("DELETE FROM categories")
    suspend fun clearAllCategories()

    //For backup feature
    @Query("DELETE FROM categories")
    fun clearCategoriesTableSync()

    @Query("DELETE FROM subcategories")
    fun clearSubCategoriesTableSync()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllCategoriesSync(categories: List<Category>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSubCategoriesSync(subCategories: List<SubCategory>)

    // Fetch all categories once
    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesOnce(): List<Category>

    // Fetch all subcategories once
    @Query("SELECT * FROM subcategories")
    suspend fun getAllSubCategoriesOnce(): List<SubCategory>

}

