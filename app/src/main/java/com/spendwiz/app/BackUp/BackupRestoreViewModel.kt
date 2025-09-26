package com.spendwiz.app.BackUp

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.spendwiz.app.BackUp.Drive.GoogleDriveService
import com.spendwiz.app.Database.money.MoneyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Keep your existing DatabaseBackup data class and BackupRestoreState sealed class
// (Assuming DatabaseBackup is a @Serializable data class you have defined)

sealed class BackupRestoreState {
    object Idle : BackupRestoreState()
    object InProgress : BackupRestoreState()
    data class Success(val message: String) : BackupRestoreState()
    data class Error(val error: String) : BackupRestoreState()
}

class BackupRestoreViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow<BackupRestoreState>(BackupRestoreState.Idle)
    val state: StateFlow<BackupRestoreState> = _state.asStateFlow()

    private val _googleUser = MutableStateFlow<GoogleSignInAccount?>(null)
    val googleUser: StateFlow<GoogleSignInAccount?> = _googleUser.asStateFlow()

    private val database = MoneyDatabase.getDatabase(application)
    private val moneyDao = database.getMoneyDao()
    private val categoryDao = database.getCategoryDao()
    private val contentResolver: ContentResolver = application.contentResolver

    private val json = Json { prettyPrint = true; encodeDefaults = true; ignoreUnknownKeys = true }

    init {
        // Check for an existing signed-in user
        _googleUser.value = GoogleSignIn.getLastSignedInAccount(application)
    }

    fun handleSignInResult(intent: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            _googleUser.value = task.getResult(ApiException::class.java)
        } catch (e: ApiException) {
            Log.w("SignIn", "signInResult:failed code=" + e.statusCode)
            _state.value = BackupRestoreState.Error("Sign-in failed. Please try again.")
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("903991082659-uauv6af5179j70ijr2sn42ufop96ibi2.apps.googleusercontent.com")
                .requestEmail()
                .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
                .build()

            val googleSignInClient = GoogleSignIn.getClient(context, gso)

            googleSignInClient.signOut().await()
            _googleUser.value = null
            _state.value = BackupRestoreState.Success("Signed out successfully.")
        }
    }



    // --- Google Drive Backup/Restore ---

    fun backupToDrive() {
        val user = _googleUser.value ?: return
        viewModelScope.launch {
            _state.value = BackupRestoreState.InProgress
            try {
                val driveService = GoogleDriveService(getApplication(), user)
                val jsonString = withContext(Dispatchers.IO) {
                    val backup = DatabaseBackup(
                        money = moneyDao.getAllMoneyOnce(),
                        categories = categoryDao.getAllCategoriesOnce(),
                        subCategories = categoryDao.getAllSubCategoriesOnce()
                    )
                    json.encodeToString(backup)
                }

                val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "spendwiz_backup_$dateStr.json"

                val newFileId = withContext(Dispatchers.IO) {
                    driveService.uploadOrUpdateFile (fileName, jsonString)
                }

                if (newFileId != null) {
                    // Cleanup old backups
                    withContext(Dispatchers.IO) {
                        val allFiles = driveService.queryFiles()
                        for (file in allFiles) {
                            if (file.id != newFileId) {
                                driveService.deleteFile(file.id)
                            }
                        }
                    }
                    _state.value = BackupRestoreState.Success("Backup to Google Drive successful.")
                } else {
                    _state.value = BackupRestoreState.Error("Failed to upload backup to Google Drive.")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = BackupRestoreState.Error("Drive Backup failed: ${e.localizedMessage}")
            }
        }
    }

    fun restoreFromDrive() {
        val user = _googleUser.value ?: return
        viewModelScope.launch {
            _state.value = BackupRestoreState.InProgress
            try {
                val driveService = GoogleDriveService(getApplication(), user)

                val latestFile = withContext(Dispatchers.IO) {
                    driveService.queryFiles().maxByOrNull { it.modifiedTime.value }
                }

                if (latestFile == null) {
                    _state.value = BackupRestoreState.Error("No backup file found in Google Drive.")
                    return@launch
                }

                val jsonContent = withContext(Dispatchers.IO) {
                    driveService.readFile(latestFile.id)
                }

                if (jsonContent != null) {
                    val backup = json.decodeFromString<DatabaseBackup>(jsonContent)
                    database.replaceAllData(backup) // Assuming you have this transaction method
                    _state.value = BackupRestoreState.Success("Restore from Google Drive successful.")
                } else {
                    _state.value = BackupRestoreState.Error("Failed to read backup file from Drive.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = BackupRestoreState.Error("Drive Restore failed: ${e.localizedMessage}")
            }
        }
    }


    // --- Local Backup/Restore --- (Your existing functions)

    fun backupData(targetUri: Uri) {
        viewModelScope.launch {
            _state.value = BackupRestoreState.InProgress
            try {
                withContext(Dispatchers.IO) {
                    val backup = DatabaseBackup(
                        money = moneyDao.getAllMoneyOnce(),
                        categories = categoryDao.getAllCategoriesOnce(),
                        subCategories = categoryDao.getAllSubCategoriesOnce()
                    )
                    val jsonString = json.encodeToString(backup)
                    contentResolver.openOutputStream(targetUri)?.use { outStream ->
                        OutputStreamWriter(outStream, Charsets.UTF_8).use { it.write(jsonString) }
                    } ?: throw IllegalStateException("Unable to open output stream for URI")
                }
                _state.value = BackupRestoreState.Success("Local backup completed successfully.")
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = BackupRestoreState.Error("Local Backup failed: ${e.message}")
            }
        }
    }

    fun restoreData(sourceUri: Uri) {
        viewModelScope.launch {
            _state.value = BackupRestoreState.InProgress
            try {
                val jsonString = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(sourceUri)?.use { input ->
                        BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { it.readText() }
                    } ?: throw IllegalStateException("Unable to open input stream for URI")
                }
                val backup = json.decodeFromString<DatabaseBackup>(jsonString)
                database.replaceAllData(backup)
                _state.value = BackupRestoreState.Success("Local restore completed successfully.")
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = BackupRestoreState.Error("Local Restore failed: ${e.message}")
            }
        }
    }

    fun resetState() {
        _state.value = BackupRestoreState.Idle
    }
}