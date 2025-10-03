package com.spendwiz.app.Encryption

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.SecureRandom

object DatabaseKeyManager {

    private const val PREFS_FILE_NAME = "db_secure_prefs"
    private const val KEY_PASSPHRASE = "db_passphrase"

    // Generates a cryptographically secure random passphrase
    private fun generatePassphrase(): String {
        val random = SecureRandom()
        val key = ByteArray(32) // 32 bytes = 256 bits, a strong key length
        random.nextBytes(key)
        // Return as a hex string for easy storage
        return key.joinToString("") { "%02x".format(it) }
    }

    // Retrieves the stored passphrase or creates and stores a new one
    fun getPassphrase(context: Context): String {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val sharedPreferences = EncryptedSharedPreferences.create(
            PREFS_FILE_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        var passphrase = sharedPreferences.getString(KEY_PASSPHRASE, null)
        if (passphrase == null) {
            passphrase = generatePassphrase()
            sharedPreferences.edit().putString(KEY_PASSPHRASE, passphrase).apply()
        }
        return passphrase
    }
}