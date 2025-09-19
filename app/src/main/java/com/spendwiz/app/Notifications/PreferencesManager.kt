package com.spendwiz.app.Notifications


import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val DAILY_NOTIFICATION = booleanPreferencesKey("daily_notification")
        val TRANSACTION_NOTIFICATION = booleanPreferencesKey("transaction_notification")
        val AUTO_CATEGORIZATION = booleanPreferencesKey("auto_categorization")
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

    suspend fun setDailyNotificationTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[DAILY_HOUR_KEY] = hour
            prefs[DAILY_MINUTE_KEY] = minute
        }
    }

    val dailyHourFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[DAILY_HOUR_KEY] ?: 22 // default 10 PM
    }

    val dailyMinuteFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[DAILY_MINUTE_KEY] ?: 5 // default 10:05
    }

    private val DAILY_HOUR_KEY = intPreferencesKey("daily_hour")
    private val DAILY_MINUTE_KEY = intPreferencesKey("daily_minute")

    val autoCategorizationFlow: Flow<Boolean> =
        context.dataStore.data.map { it[AUTO_CATEGORIZATION] ?: true } // default enabled

    suspend fun setAutoCategorization(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_CATEGORIZATION] = enabled }
    }
}
