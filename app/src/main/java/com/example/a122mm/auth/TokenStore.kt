package com.example.a122mm.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Create DataStore instance (extension property)
private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenStore(private val context: Context) {

    // Use stringPreferencesKey (not preferencesKey)
    private val KEY_ACCESS = stringPreferencesKey("access_token")
    private val KEY_REFRESH = stringPreferencesKey("refresh_token")

    suspend fun save(access: String, refresh: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS] = access
            prefs[KEY_REFRESH] = refresh
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun access(): String? =
        context.dataStore.data.map { it[KEY_ACCESS] }.first()

    suspend fun refresh(): String? =
        context.dataStore.data.map { it[KEY_REFRESH] }.first()
}
