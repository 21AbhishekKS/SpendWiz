package com.abhi.expencetracker.Database.money

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface MoneyDao {

    @Query("select * from MONEY order by date asc")
    fun getAllMoney() : LiveData<List<Money>>

    @Delete
    suspend fun deleteMoney(money: Money)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMoney(money: Money): Long

    //function to get today's transaction
    @Query("SELECT * from money where date = :date")
    fun getTodayTransactionByDate(date: String) : LiveData<List<Money>>

    //get today's transaction without live data
    @Query("SELECT * FROM money WHERE date = :date")
    suspend fun getTodayTransactionsRaw(date: String): List<Money>


    @Query("SELECT * FROM money WHERE substr(date, 4, 2) = :month AND substr(date, 7, 4) = :year")
    fun getTransactionsByMonthAndYear(month: String, year: String): LiveData<List<Money>>


    @Query("SELECT * FROM money WHERE substr(date, 4, 7) LIKE :date")
    fun getTransactionsForMonth(date: String): LiveData<List<Money>>

    @Query("SELECT COUNT(*) FROM money WHERE upiRefNo = :upiRefNo")
    suspend fun existsByUpiRefNo(upiRefNo: String): Int

    @Update
    suspend fun updateMoney(money: Money)

    @Query("SELECT * FROM money WHERE id = :id LIMIT 1")
    suspend fun getMoneyById(id: Int): Money?

    //for category donut chart
    @Query("""
    SELECT category, SUM(amount) as total 
    FROM money 
    WHERE type = :transactionType 
      AND substr(date, 4, 2) = :month 
      AND substr(date, 7, 4) = :year
    GROUP BY category
""")
    fun getCategoryExpensesByMonthAndYear(
        month: String,
        year: String,
        transactionType: TransactionType = TransactionType.EXPENSE
    ): LiveData<List<CategoryExpense>>

    //for subcategory donut chart
    @Query("""
    SELECT subCategory, SUM(amount) as total 
    FROM money 
    WHERE type = :transactionType 
      AND category = :category
      AND substr(date, 4, 2) = :month 
      AND substr(date, 7, 4) = :year
    GROUP BY subCategory
""")
    fun getSubCategoryExpensesByMonthAndYear(
        category: String,
        month: String,
        year: String,
        transactionType: TransactionType = TransactionType.EXPENSE
    ): LiveData<List<SubCategoryExpense>>

    //To update sub category from notification
    @Query("UPDATE money SET subCategory = :subCategory WHERE id = :id")
    suspend fun updateSubCategory(id: Int, subCategory: String)


    // for list of money items from insight screen
    @Query("UPDATE money SET type = :newType WHERE id IN (:ids)")
    suspend fun updateTransactionType(ids: List<Int>, newType: TransactionType)

    @Query("UPDATE money SET category = :newCategory WHERE id IN (:ids)")
    suspend fun updateCategory(ids: List<Int>, newCategory: String)

    @Query("UPDATE money SET subCategory = :newSubCategory WHERE id IN (:ids)")
    suspend fun updateSubCategory(ids: List<Int>, newSubCategory: String)

    @Query("DELETE FROM money WHERE id IN (:ids)")
    suspend fun deleteTransactions(ids: List<Int>)

    @Query("UPDATE money SET category = :category WHERE id = :id")
    suspend fun updateCategory(id: Int, category: String)

    @Query("SELECT * FROM money WHERE id = :id LIMIT 1")
    fun getTransactionById(id: Int): Money?

}



data class SubCategoryExpense(
    val subCategory: String?,
    val total: Double
)



