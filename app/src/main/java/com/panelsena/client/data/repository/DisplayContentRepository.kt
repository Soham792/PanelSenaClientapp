package com.panelsena.client.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.panelsena.client.core.utils.DeviceIdentityManager
import com.panelsena.client.core.utils.NetworkMonitor
import com.panelsena.client.data.firebase.FirestoreDataSource
import com.panelsena.client.data.firebase.RealtimeDataSource
import com.panelsena.client.data.model.LinkState
import com.panelsena.client.data.model.PlaybackCommand
import com.panelsena.client.data.model.PlaybackState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates the PanelSena device protocol for the Android client. It registers this
 * device, discovers its account link, drives playback from dashboard commands, and exposes
 * state to the UI. Heartbeating and command listening are driven by [com.panelsena.client.ui.DisplayViewModel].
 */
@Singleton
class DisplayContentRepository @Inject constructor(
    private val realtime: RealtimeDataSource,
    private val firestore: FirestoreDataSource,
    private val deviceIdentity: DeviceIdentityManager,
    private val networkMonitor: NetworkMonitor,
    private val auth: FirebaseAuth
) {
    val isOnline: Flow<Boolean> = networkMonitor.isOnline

    private val _linkState = MutableStateFlow<LinkState>(LinkState.Loading)
    val linkState = _linkState.asStateFlow()

    private val _playback = MutableStateFlow(PlaybackState())
    val playback = _playback.asStateFlow()

    private val displayName: String
        get() = (auth.currentUser?.displayName?.let { "$it's Display" }) ?: "PanelSena Display"

    suspend fun getDeviceId(): String = deviceIdentity.getDeviceId()
    suspend fun getDeviceKey(): String = deviceIdentity.getDeviceKey()

    /** Register this device in the registry so the dashboard can verify + link it. */
    suspend fun registerDevice() {
        val (id, key) = deviceIdentity.ensureIdentity()
        runCatching { realtime.registerDevice(id, key, displayName) }
    }

    fun observeDeviceLink(deviceId: String) = realtime.observeDeviceLink(deviceId)

    fun observeCommands(userId: String, displayId: String) =
        realtime.observeCommands(userId, displayId)

    suspend fun ackCommand(
        userId: String,
        displayId: String,
        commandId: String,
        success: Boolean,
        result: String
    ) = runCatching { realtime.ackCommand(userId, displayId, commandId, success, result) }

    fun setLinkState(state: LinkState) {
        _linkState.value = state
    }

    suspend fun resolveDisplayName(displayId: String, fallback: String): String =
        runCatching { firestore.getDisplay(displayId)?.name }.getOrNull()?.takeIf { it.isNotBlank() } ?: fallback

    /** Push the current status/heartbeat for the linked display. */
    suspend fun sendHeartbeat(link: LinkState.Linked) {
        val state = _playback.value
        val status = when {
            state.isPaused -> "paused"
            state.isPlaying -> "playing"
            else -> "online"
        }
        val currentContent = state.currentItem?.let {
            mapOf(
                "id" to it.id,
                "name" to it.name,
                "type" to it.type.label.lowercase(),
                "url" to it.url
            )
        }
        val schedule = state.scheduleName?.let {
            mapOf(
                "name" to it,
                "contentQueue" to state.queue.map { item -> item.id },
                "currentIndex" to state.currentIndex
            )
        }
        runCatching {
            realtime.updateStatus(
                userId = link.userId,
                displayId = link.displayId,
                displayName = link.displayName,
                status = status,
                volume = state.volume,
                brightness = state.brightness,
                currentContent = currentContent,
                schedule = schedule
            )
        }
    }

    suspend fun markOffline(link: LinkState.Linked) {
        runCatching { realtime.markOffline(link.userId, link.displayId) }
    }

    /**
     * Execute a pending command from the dashboard. Returns a (success, message) result so the
     * caller can acknowledge it. Mirrors the Pi player's execute_command().
     */
    suspend fun executeCommand(command: PlaybackCommand): Pair<Boolean, String> = runCatching {
        when (command.type) {
            "play" -> {
                when {
                    command.scheduleId != null -> loadAndPlaySchedule(command.scheduleId)
                    command.contentId != null -> playSingleContent(command.contentId)
                    else -> return false to "play command missing contentId/scheduleId"
                }
            }
            "pause" -> _playback.value = _playback.value.copy(isPaused = true)
            "stop" -> _playback.value = PlaybackState(volume = _playback.value.volume, brightness = _playback.value.brightness)
            "skip" -> skip()
            "volume" -> _playback.value = _playback.value.copy(volume = (command.volume ?: 80).coerceIn(0, 100))
            "brightness" -> _playback.value = _playback.value.copy(brightness = (command.brightness ?: 100).coerceIn(0, 100))
            "restart" -> _playback.value = _playback.value.copy(currentIndex = 0)
            else -> return false to "unknown command: ${command.type}"
        }
        true to "ok"
    }.getOrElse { false to (it.message ?: "command failed") }

    private suspend fun loadAndPlaySchedule(scheduleId: String) {
        val schedule = firestore.getSchedule(scheduleId) ?: throw IllegalStateException("Schedule not found")
        val items = firestore.getContentItems(schedule.contentIds)
        if (items.isEmpty()) throw IllegalStateException("Schedule has no playable content")
        _playback.value = _playback.value.copy(
            queue = items,
            currentIndex = 0,
            isPlaying = true,
            isPaused = false,
            scheduleName = schedule.name
        )
    }

    private suspend fun playSingleContent(contentId: String) {
        val item = firestore.getContent(contentId) ?: throw IllegalStateException("Content not found")
        _playback.value = _playback.value.copy(
            queue = listOf(item),
            currentIndex = 0,
            isPlaying = true,
            isPaused = false,
            scheduleName = null
        )
    }

    private fun skip() {
        val state = _playback.value
        if (state.queue.isEmpty()) return
        _playback.value = state.copy(
            currentIndex = (state.currentIndex + 1) % state.queue.size,
            isPaused = false,
            isPlaying = true
        )
    }

    /** Called by the player when a media item finishes, to advance the queue. */
    fun onMediaCompleted() = skip()

    /** Manual playback (from the app's own UI) without a dashboard command. */
    fun playLocalQueue(startIndex: Int) {
        val state = _playback.value
        if (state.queue.isEmpty()) return
        _playback.value = state.copy(currentIndex = startIndex.coerceIn(0, state.queue.size - 1), isPlaying = true, isPaused = false)
    }
}
