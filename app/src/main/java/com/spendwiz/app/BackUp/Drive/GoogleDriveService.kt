package com.spendwiz.app.BackUp.Drive

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import java.io.ByteArrayOutputStream
import com.google.api.client.extensions.android.http.AndroidHttp
import java.util.Collections
import com.google.api.client.http.ByteArrayContent

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
     * Uploads a file with content to the app's private data folder on Google Drive.
     * @param fileName The name of the file to create.
     * @param content The JSON string content of the file.
     * @return The ID of the newly created file.
     */
    suspend fun uploadFile(fileName: String, content: String): String? {
        val metadata = File().apply {
            name = fileName
            parents = listOf("appDataFolder")
        }
        val contentStream = ByteArrayContent.fromString("application/json", content)
        val file = drive.files().create(metadata, contentStream).setFields("id").execute()
        return file.id
    }

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