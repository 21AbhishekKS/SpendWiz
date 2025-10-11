package com.spendwiz.app.Database.money

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
    fun getAllMoney(): LiveData<List<Money>>

    @Delete
    suspend fun deleteMoney(money: Money)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMoney(money: Money): Long

    //function to get today's transaction
    @Query("SELECT * from money where date = :date")
    fun getTodayTransactionByDate(date: String): LiveData<List<Money>>

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
    @Query(
        """
    SELECT category, SUM(amount) as total 
    FROM money 
    WHERE type = :transactionType 
      AND substr(date, 4, 2) = :month 
      AND substr(date, 7, 4) = :year
    GROUP BY category
"""
    )
    fun getCategoryExpensesByMonthAndYear(
        month: String,
        year: String,
        transactionType: TransactionType = TransactionType.EXPENSE
    ): LiveData<List<CategoryExpense>>

    //for subcategory donut chart
    @Query(
        """
    SELECT subCategory, SUM(amount) as total 
    FROM money 
    WHERE type = :transactionType 
      AND category = :category
      AND substr(date, 4, 2) = :month 
      AND substr(date, 7, 4) = :year
    GROUP BY subCategory
"""
    )
    fun getSubCategoryExpensesByMonthAndYear(
        category: String,
        month: String,
        year: String,
        transactionType: TransactionType = TransactionType.EXPENSE
    ): LiveData<List<SubCategoryExpense>>

    //To update sub category from notification
    @Query("UPDATE money SET subCategory = :subCategory WHERE id = :id")
    suspend fun updateSubCategory(id: Int, subCategory: String)

    @Query("SELECT * FROM money WHERE substr(date, 7, 4) = :year")
    fun getTransactionsForYearReport(year: String): LiveData<List<Money>>

    //To get years in database for year picker
    @Query("SELECT DISTINCT CAST(SUBSTR(date, 7, 4) AS INTEGER) FROM money ORDER BY SUBSTR(date, 7, 4) DESC")
    fun getDistinctYears(): LiveData<List<Int>>

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

    //To check weather the transaction with same amount and description already present
    @Query(
        """
    SELECT * FROM money
    WHERE description = :name 
      AND amount = :amount 
      AND type = :type
    ORDER BY id DESC 
    LIMIT :limit
"""
    )
    suspend fun findRecentByNameAmountAndType(
        name: String,
        amount: Double,
        type: String,
        limit: Int
    ): List<Money>

    //For yearly graph
    @Query(
        """
    SELECT substr(date, 4, 2) as month,
           SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as totalIncome,
           SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as totalExpense
    FROM money
    WHERE substr(date, 7, 4) = :year
    GROUP BY month
    ORDER BY month
"""
    )
    fun getMonthlyIncomeExpense(year: String): LiveData<List<MonthlySummary>>

    data class MonthlySummary(
        val month: String,        // "01".."12"
        val totalIncome: Double,
        val totalExpense: Double
    )

    @Query("SELECT id, date, category, subCategory FROM money WHERE date LIKE :yearPrefix")
    suspend fun getTransactionsForYear(yearPrefix: String): List<MoneyMinimal>

    // Get transactions with same name but uncategorized for bulk update feature
    @Query("SELECT * FROM money WHERE description = :name AND category = 'Others'")
    suspend fun getUncategorizedByNameOnce(name: String): List<Money>

    @Query("SELECT * FROM money WHERE description = :name AND category = 'Others' AND id != :excludeId")
    suspend fun getUncategorizedByNameOnceExceptCurrent(name: String, excludeId: Int): List<Money>

    // Bulk update category + subCategory for multiple transactions
    @Query("UPDATE money SET category = :newCategory, subCategory = :newSubCategory WHERE id IN (:ids)")
    suspend fun bulkUpdateCategory(ids: List<Int>, newCategory: String, newSubCategory: String?)

    //for backup feature
    @Query("DELETE FROM money")
    fun clearMoneyTableSync()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllMoneySync(moneyList: List<Money>)

    // Fetch all money records once (suspend)
    @Query("SELECT * FROM money")
    suspend fun getAllMoneyOnce(): List<Money>

    // For voice assistant

    @Query("SELECT * FROM money ORDER BY id DESC LIMIT 1")
    suspend fun getLastTransaction(): Money?

    @Query("SELECT SUM(amount) FROM money WHERE date = :date AND type = :type")
    suspend fun getTotalForDate(date: String, type: String): Double?

    // For updating the last transaction
    @Query("UPDATE money SET amount = :newAmount WHERE id = (SELECT id FROM money ORDER BY id DESC LIMIT 1)")
    suspend fun updateLastTransactionAmount(newAmount: Double)

    @Query("UPDATE money SET category = :newCategory WHERE id = (SELECT id FROM money ORDER BY id DESC LIMIT 1)")
    suspend fun updateLastTransactionCategory(newCategory: String)

    // For deleting all of today's transactions
    @Query("DELETE FROM money WHERE date = :date")
    suspend fun deleteTransactionsByDate(date: String)

    // For getting a monthly summary (non-live data version for the service)
    @Query("""
    SELECT substr(date, 4, 2) as month,
           SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as totalIncome,
           SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as totalExpense
    FROM money
    WHERE substr(date, 4, 2) = :month AND substr(date, 7, 4) = :year
    GROUP BY month
""")
    suspend fun getMonthlySummary(month: String, year: String): MonthlySummary?

    // For finding the biggest expense in a month
    @Query("""
    SELECT * FROM money
    WHERE type = 'EXPENSE' AND substr(date, 4, 2) = :month AND substr(date, 7, 4) = :year
    ORDER BY amount DESC
    LIMIT 1
""")
    suspend fun getBiggestExpenseForMonth(month: String, year: String): Money?
}

data class MoneyMinimal(
    val id: Int,
    val date: String,       // stored as "dd/MM/yyyy"
    val category: String?,
    val subCategory: String?
)

data class SubCategoryExpense(
    val subCategory: String?,
    val total: Double
)



