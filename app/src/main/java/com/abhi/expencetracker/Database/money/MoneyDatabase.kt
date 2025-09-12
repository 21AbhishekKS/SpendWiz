package com.abhi.expencetracker.Database.money

import android.content.Context
import android.util.Log
import androidx.room.*
import com.abhi.expencetracker.Database.money.CategoryData.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Money::class, Category::class, SubCategory::class],
    version = 6
)
@TypeConverters(Converters::class)
abstract class MoneyDatabase : RoomDatabase() {

    abstract fun getMoneyDao(): MoneyDao
    abstract fun getCategoryDao(): CategoryDao

    companion object {
        private const val TAG = "MoneyDatabase"
        @Volatile
        private var INSTANCE: MoneyDatabase? = null

        fun getDatabase(context: Context): MoneyDatabase {
            // double-checked locking
            val tmp = INSTANCE
            if (tmp != null) {
                Log.d(TAG, "getDatabase(): returning existing INSTANCE")
                return tmp
            }

            return synchronized(this) {
                Log.d(TAG, "getDatabase(): building database...")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoneyDatabase::class.java,
                    NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                Log.d(TAG, "getDatabase(): INSTANCE assigned")

                // Start prepopulation check on IO coroutine AFTER instance is set
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        Log.d(TAG, "Prepopulate check: querying categories...")
                        val dao = instance.getCategoryDao()
                        val expenses = dao.getCategoriesByType("Expense")
                        if (expenses.isEmpty()) {
                            Log.d(TAG, "Prepopulate check: no categories found -> inserting defaults")
                            insertDefaultData(dao)
                            Log.d(TAG, "Prepopulate: completed inserting defaults")
                        } else {
                            Log.d(TAG, "Prepopulate check: categories already exist (count=${expenses.size})")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Prepopulate: exception while checking/inserting defaults", e)
                    }
                }

                instance
            }
        }

        const val NAME = "Money_DB"

        private suspend fun insertDefaultData(dao: CategoryDao) {
            try {
                Log.d(TAG, "insertDefaultData(): start")

                // Income
                incomeCategories.forEach { cat ->
                    try {
                        val idLong = dao.insertCategory(Category(type = "Income", name = cat, isDefault = true))
                        val id = idLong.toInt()
                        Log.d(TAG, "Inserted Income category: '$cat' id=$id")
                        incomeSubCategoryMap[cat]?.forEach { sub ->
                            dao.insertSubCategory(SubCategory(categoryId = id, name = sub, isDefault = true))
                            Log.d(TAG, "   -> Inserted Income subcategory '$sub' for categoryId=$id")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error inserting Income category '$cat'", e)
                    }
                }

                // Expense
                expenseCategories.forEach { cat ->
                    try {
                        val idLong = dao.insertCategory(Category(type = "Expense", name = cat, isDefault = true))
                        val id = idLong.toInt()
                        Log.d(TAG, "Inserted Expense category: '$cat' id=$id")
                        expenseSubCategoryMap[cat]?.forEach { sub ->
                            dao.insertSubCategory(SubCategory(categoryId = id, name = sub, isDefault = true))
                            Log.d(TAG, "   -> Inserted Expense subcategory '$sub' for categoryId=$id")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error inserting Expense category '$cat'", e)
                    }
                }

                // Transfer
                transferCategories.forEach { cat ->
                    try {
                        val idLong = dao.insertCategory(Category(type = "Transfer", name = cat, isDefault = true))
                        val id = idLong.toInt()
                        Log.d(TAG, "Inserted Transfer category: '$cat' id=$id")
                        transferSubCategoryMap[cat]?.forEach { sub ->
                            dao.insertSubCategory(SubCategory(categoryId = id, name = sub, isDefault = true))
                            Log.d(TAG, "   -> Inserted Transfer subcategory '$sub' for categoryId=$id")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error inserting Transfer category '$cat'", e)
                    }
                }

                Log.d(TAG, "insertDefaultData(): finished")
            } catch (e: Exception) {
                Log.e(TAG, "insertDefaultData(): unexpected error", e)
            }
        }
    }
}
