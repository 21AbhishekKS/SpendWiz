package com.spendwiz.app.BackUp.Drive

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import java.io.ByteArrayOutputStream
import java.util.Collections

class GoogleDriveService(context: Context, private val account: GoogleSignInAccount) {

    private val drive: Drive

    init {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            Collections.singleton(DriveScopes.DRIVE_APPDATA)
        ).setSelectedAccount(account.account)

        drive = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        ).setApplicationName("SpendWiz").build()
    }

    /**
     * Uploads or updates a file in the appDataFolder.
     * It first searches for a file with the given name. If found, it updates it.
     * Otherwise, it creates a new file.
     *
     * @param fileName The name of the file to create or update (e.g., "spendwiz_backup.json").
     * @param content The JSON string content of the file.
     * @return The ID of the created or updated file.
     */
    suspend fun uploadOrUpdateFile(fileName: String, content: String): String? {
        // 1. Search for the file first
        val existingFiles = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = '$fileName'") // Query by file name
            .setFields("files(id, name)")
            .execute()
            .files

        val contentStream = ByteArrayContent.fromString("application/json", content)
        val existingFile = existingFiles?.firstOrNull()

        return if (existingFile != null) {
            // 2. If file exists, UPDATE it
            val updatedFile = drive.files().update(existingFile.id, null, contentStream)
                .setFields("id")
                .execute()
            updatedFile.id
        } else {
            // 3. If file does not exist, CREATE it
            val metadata = File().apply {
                name = fileName
                parents = listOf("appDataFolder")
            }
            val newFile = drive.files().create(metadata, contentStream)
                .setFields("id")
                .execute()
            newFile.id
        }
    }


    // --- Your other functions remain the same ---

    /**
     * Queries for all backup files in the app's private data folder.
     * @return A list of Google Drive File objects.
     */
    suspend fun queryFiles(): List<File> {
        val result = drive.files().list()
            .setSpaces("appDataFolder")
            .setFields("files(id, name, modifiedTime)")
            .execute()
        return result.files ?: emptyList()
    }

    /**
     * Deletes a file from Google Drive by its ID.
     * @param fileId The ID of the file to delete.
     */
    suspend fun deleteFile(fileId: String) {
        drive.files().delete(fileId).execute()
    }

    /**
     * Reads the content of a file from Google Drive.
     * @param fileId The ID of the file to read.
     * @return The content of the file as a String.
     */
    suspend fun readFile(fileId: String): String? {
        return try {
            val outputStream = ByteArrayOutputStream()
            drive.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            outputStream.toString("UTF-8")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}