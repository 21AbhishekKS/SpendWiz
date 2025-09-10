package com.abhi.expencetracker.Notifications


import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val DAILY_NOTIFICATION = booleanPreferencesKey("daily_notification")
        val TRANSACTION_NOTIFICATION = booleanPreferencesKey("transaction_notification")
    }

    val dailyNotificationFlow: Flow<Boolean> =
        context.dataStore.data.map { it[DAILY_NOTIFICATION] ?: true }

    val transactionNotificationFlow: Flow<Boolean> =
        context.dataStore.data.map { it[TRANSACTION_NOTIFICATION] ?: true }

    suspend fun setDailyNotification(enabled: Boolean) {
        context.dataStore.edit { it[DAILY_NOTIFICATION] = enabled }
    }

    suspend fun setTransactionNotification(enabled: Boolean) {
        context.dataStore.edit { it[TRANSACTION_NOTIFICATION] = enabled }
    }
}
