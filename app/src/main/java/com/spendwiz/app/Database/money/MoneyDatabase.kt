package com.spendwiz.app.Database.money

import android.content.Context
import android.util.Log
import androidx.room.*
import com.spendwiz.app.BackUp.DatabaseBackup
import com.spendwiz.app.Encryption.DatabaseKeyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [Money::class, Category::class, SubCategory::class],
    version = 9, // Keep your version. If you change entities, you'll need a Room migration.
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MoneyDatabase : RoomDatabase() {

    abstract fun getMoneyDao(): MoneyDao
    abstract fun getCategoryDao(): CategoryDao

    companion object {
        private const val TAG = "MoneyDatabase"

        // *** CHANGE 1: Define the new, encrypted database name ***
        const val NAME = "spendwiz_encrypted.db"

        @Volatile
        private var INSTANCE: MoneyDatabase? = null

        fun getDatabase(context: Context): MoneyDatabase {
            val tmp = INSTANCE
            if (tmp != null) {
                return tmp
            }

            return synchronized(this) {
                Log.d(TAG, "getDatabase(): building encrypted database...")

                val passphrase = DatabaseKeyManager.getPassphrase(context.applicationContext)
                val factory = SupportFactory(passphrase.toByteArray())

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoneyDatabase::class.java,
                    NAME
                )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                Log.d(TAG, "getDatabase(): INSTANCE assigned")

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        Log.d(TAG, "Prepopulate check: querying categories...")
                        val dao = instance.getCategoryDao()
                        val expenses = dao.getCategoriesByTypeOnce("Expense")
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

        // NO CHANGES NEEDED for this function
        suspend fun insertDefaultData(dao: CategoryDao) {
            // ... your existing default data insertion logic remains here ...
        }
    }

    // NO CHANGES NEEDED for this function
    suspend fun replaceAllData(backup: DatabaseBackup) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            runInTransaction {
                getCategoryDao().clearSubCategoriesTableSync()
                getCategoryDao().clearCategoriesTableSync()
                getMoneyDao().clearMoneyTableSync()
                getCategoryDao().insertAllCategoriesSync(backup.categories)
                getCategoryDao().insertAllSubCategoriesSync(backup.subCategories)
                getMoneyDao().insertAllMoneySync(backup.money)
            }
        }
    }
}
