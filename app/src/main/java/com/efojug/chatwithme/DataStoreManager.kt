package com.efojug.chatwithme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {
    companion object {
        val USERNAME_KEY = stringPreferencesKey("username")
        val PASSWORD_KEY = stringPreferencesKey("password")
        val SERVER_ADDRESS_KEY = stringPreferencesKey("server_address")
        val SAVE_CREDENTIALS_KEY = booleanPreferencesKey("save_credentials")
        val AUTO_LOGIN_KEY = booleanPreferencesKey("auto_login")
    }

    val usernameFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USERNAME_KEY]
    }

    val passwordFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PASSWORD_KEY]
    }

    val serverAddressFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SERVER_ADDRESS_KEY]
    }

    val saveCredentialsFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SAVE_CREDENTIALS_KEY] ?: false
    }

    val autoLoginFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_LOGIN_KEY] ?: false
    }

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
        }
    }

    suspend fun savePassword(password: String) {
        context.dataStore.edit { preferences ->
            preferences[PASSWORD_KEY] = password
        }
    }

    suspend fun clearPassword() {
        context.dataStore.edit { preferences ->
            preferences.remove(PASSWORD_KEY)
        }
    }

    suspend fun saveServerAddress(address: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVER_ADDRESS_KEY] = address
        }
    }

    suspend fun saveSaveCredentials(saveCredentials: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SAVE_CREDENTIALS_KEY] = saveCredentials
        }
    }

    suspend fun saveAutoLogin(autoLogin: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_LOGIN_KEY] = autoLogin
        }
    }
}