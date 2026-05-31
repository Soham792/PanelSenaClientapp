package com.panelsena.client.data.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.panelsena.client.core.utils.awaitResult
import com.panelsena.client.data.model.DeviceLink
import com.panelsena.client.data.model.PlaybackCommand
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implements the PanelSena device protocol over Realtime Database, mirroring the
 * Raspberry Pi player (`raspberry-pi/player.py`) and the dashboard (`lib/realtime-db.ts`):
 *  - register in `device_registry/{deviceId}` so the dashboard can verify the key on link
 *  - observe `device_links/{deviceId}` to discover {userId, displayId}
 *  - heartbeat `users/{userId}/displays/{displayId}/status`
 *  - listen + ack `users/{userId}/displays/{displayId}/commands`
 */
@Singleton
class RealtimeDataSource @Inject constructor(
    private val database: FirebaseDatabase
) {
    private fun registryRef(deviceId: String) = database.getReference("device_registry/$deviceId")
    private fun linkRef(deviceId: String) = database.getReference("device_links/$deviceId")
    private fun statusRef(userId: String, displayId: String) =
        database.getReference("users/$userId/displays/$displayId/status")
    private fun commandsRef(userId: String, displayId: String) =
        database.getReference("users/$userId/displays/$displayId/commands")

    /** Register (or refresh) this device so the dashboard can link it. Mirrors the Pi's authenticate_device(). */
    suspend fun registerDevice(deviceId: String, deviceKey: String, displayName: String) {
        val ref = registryRef(deviceId)
        val existing = ref.get().awaitResult()
        if (existing.exists()) {
            ref.child("lastSeen").setValue(System.currentTimeMillis()).awaitResult()
        } else {
            ref.setValue(
                mapOf(
                    "deviceId" to deviceId,
                    "deviceKey" to deviceKey,
                    "displayName" to displayName,
                    "registeredAt" to System.currentTimeMillis(),
                    "lastSeen" to System.currentTimeMillis(),
                    "linkedToUser" to null,
                    "status" to "registered",
                    "platform" to "android"
                )
            ).awaitResult()
        }
    }

    /** Emits the device link once the dashboard links this device, then on every change. */
    fun observeDeviceLink(deviceId: String): Flow<DeviceLink?> = callbackFlow {
        val ref = linkRef(deviceId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userId = snapshot.child("userId").getValue(String::class.java)
                val displayId = snapshot.child("displayId").getValue(String::class.java)
                trySend(if (userId != null && displayId != null) DeviceLink(userId, displayId) else null)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(null)
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    /** Write live status / heartbeat. Mirrors the Pi's update_status(). */
    suspend fun updateStatus(
        userId: String,
        displayId: String,
        displayName: String,
        status: String,
        volume: Int,
        brightness: Int,
        currentContent: Map<String, Any?>?,
        schedule: Map<String, Any?>?,
        errorMessage: String? = null
    ) {
        val data = mutableMapOf<String, Any?>(
            "displayId" to displayId,
            "displayName" to displayName,
            "status" to status,
            "lastHeartbeat" to System.currentTimeMillis(),
            "volume" to volume,
            "brightness" to brightness,
            "currentContent" to currentContent,
            "schedule" to schedule
        )
        if (errorMessage != null) data["errorMessage"] = errorMessage
        statusRef(userId, displayId).setValue(data).awaitResult()
    }

    /** Set status to offline (best-effort, e.g. on sign-out). */
    suspend fun markOffline(userId: String, displayId: String) {
        statusRef(userId, displayId).child("status").setValue("offline").awaitResult()
    }

    /** Emits the full set of commands whenever they change. */
    fun observeCommands(userId: String, displayId: String): Flow<List<PlaybackCommand>> = callbackFlow {
        val ref = commandsRef(userId, displayId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val commands = snapshot.children.mapNotNull { child ->
                    val id = child.child("commandId").getValue(String::class.java) ?: child.key ?: return@mapNotNull null
                    PlaybackCommand(
                        commandId = id,
                        type = child.child("type").getValue(String::class.java) ?: return@mapNotNull null,
                        contentId = child.child("payload/contentId").getValue(String::class.java),
                        scheduleId = child.child("payload/scheduleId").getValue(String::class.java),
                        volume = child.child("payload/volume").getValue(Int::class.java),
                        brightness = child.child("payload/brightness").getValue(Int::class.java),
                        status = child.child("status").getValue(String::class.java) ?: "pending"
                    )
                }
                trySend(commands)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    /** Acknowledge a command. Mirrors the Pi marking commands executed/failed. */
    suspend fun ackCommand(
        userId: String,
        displayId: String,
        commandId: String,
        success: Boolean,
        result: String
    ) {
        commandsRef(userId, displayId).child(commandId)
            .updateChildren(
                mapOf(
                    "status" to if (success) "executed" else "failed",
                    "result" to result
                )
            ).awaitResult()
    }
}
