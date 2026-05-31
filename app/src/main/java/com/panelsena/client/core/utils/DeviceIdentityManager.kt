package com.panelsena.client.core.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns this device's stable identity used to link it to a PanelSena account.
 * Mirrors the Raspberry Pi setup (`setup_device.py`): a human-shareable Device ID
 * (`DEVICE_<timestamp>_<rand>`) plus a 32-char secret Device Key. The user enters both
 * in the dashboard's "Link Device" dialog to bind this device to their account.
 */
@Singleton
class DeviceIdentityManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val deviceIdKey = stringPreferencesKey("device_id")
    private val deviceKeyKey = stringPreferencesKey("device_key")

    suspend fun getDeviceId(): String {
        dataStore.data.first()[deviceIdKey]?.let { return it }
        return ensureIdentity().first
    }

    suspend fun getDeviceKey(): String {
        dataStore.data.first()[deviceKeyKey]?.let { return it }
        return ensureIdentity().second
    }

    /** Returns the persisted (deviceId, deviceKey), generating and storing them on first run. */
    suspend fun ensureIdentity(): Pair<String, String> {
        val prefs = dataStore.data.first()
        val existingId = prefs[deviceIdKey]
        val existingKey = prefs[deviceKeyKey]
        if (existingId != null && existingKey != null) return existingId to existingKey

        val id = existingId ?: generateDeviceId()
        val key = existingKey ?: generateDeviceKey()
        dataStore.edit {
            it[deviceIdKey] = id
            it[deviceKeyKey] = key
        }
        return id to key
    }

    private fun generateDeviceId(): String {
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val suffix = (1..6).map { chars.random() }.joinToString("")
        return "DEVICE_${timestamp}_$suffix"
    }

    private fun generateDeviceKey(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..32).map { chars.random() }.joinToString("")
    }
}
