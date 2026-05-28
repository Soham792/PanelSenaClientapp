package com.panelsena.client.core.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientIdManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val KEY = stringPreferencesKey("client_id")

    suspend fun getOrCreateClientId(): String {
        val stored = dataStore.data.first()[KEY]
        if (stored != null) return stored
        // Format: XXXX-XXXX-XXXX (no ambiguous chars: no 0/O, no 1/I/L)
        val chars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
        val raw = (1..12).map { chars.random() }.joinToString("")
        val formatted = raw.chunked(4).joinToString("-")
        dataStore.edit { it[KEY] = formatted }
        return formatted
    }
}
