package com.spendwiz.app.BackUp

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spendwiz.app.Database.money.MoneyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

sealed class BackupRestoreState {
    object Idle : BackupRestoreState()
    object InProgress : BackupRestoreState()
    data class Success(val message: String) : BackupRestoreState()
    data class Error(val error: String) : BackupRestoreState()
}

class BackupRestoreViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow<BackupRestoreState>(BackupRestoreState.Idle)
    val state: StateFlow<BackupRestoreState> = _state

    private val database = MoneyDatabase.getDatabase(application)
    private val moneyDao = database.getMoneyDao()
    private val categoryDao = database.getCategoryDao()
    private val contentResolver: ContentResolver = application.contentResolver

    private val json = Json { prettyPrint = true; encodeDefaults = true; ignoreUnknownKeys = true }

    /**
     * Backup data to the provided Uri (Storage Access Framework).
     */
    fun backupData(targetUri: Uri) {
        viewModelScope.launch {
            _state.value = BackupRestoreState.InProgress
            try {
                withContext(Dispatchers.IO) {
                    val moneyList = moneyDao.getAllMoneyOnce()
                    val categories = categoryDao.getAllCategoriesOnce()
                    val subCategories = categoryDao.getAllSubCategoriesOnce()

                    val backup = DatabaseBackup(
                        money = moneyList,
                        categories = categories,
                        subCategories = subCategories
                    )

                    val jsonString = json.encodeToString(backup)

                    contentResolver.openOutputStream(targetUri)?.use { outStream ->
                        OutputStreamWriter(outStream, Charsets.UTF_8).use { writer ->
                            writer.write(jsonString)
                            writer.flush()
                        }
                    } ?: throw IllegalStateException("Unable to open output stream for URI")
                }
                _state.value = BackupRestoreState.Success("Backup completed successfully.")
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = BackupRestoreState.Error("Backup failed: ${e.localizedMessage ?: e.message}")
            }
        }
    }

    /**
     * Read JSON from the Uri and replace the database contents atomically.
     */
    fun restoreData(sourceUri: Uri) {
        viewModelScope.launch {
            _state.value = BackupRestoreState.InProgress
            try {
                val backup = withContext(Dispatchers.IO) {
                    val sb = StringBuilder()
                    contentResolver.openInputStream(sourceUri)?.use { input ->
                        BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
                            var line = reader.readLine()
                            while (line != null) {
                                sb.append(line)
                                line = reader.readLine()
                            }
                        }
                    } ?: throw IllegalStateException("Unable to open input stream for URI")

                    json.decodeFromString<DatabaseBackup>(sb.toString())
                }

                // Replace data atomically
                database.replaceAllData(backup)

                _state.value = BackupRestoreState.Success("Restore completed successfully.")
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = BackupRestoreState.Error("Restore failed: ${e.localizedMessage ?: e.message}")
            }
        }
    }

    fun resetState() {
        _state.value = BackupRestoreState.Idle
    }
}
