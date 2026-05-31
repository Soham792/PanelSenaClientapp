package com.panelsena.client.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.panelsena.client.data.model.AssignedDisplay
import com.panelsena.client.data.model.LinkState
import com.panelsena.client.data.model.PlaybackState
import com.panelsena.client.data.repository.DisplayContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DisplayViewModel @Inject constructor(
    private val repository: DisplayContentRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = repository.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val linkState: StateFlow<LinkState> = repository.linkState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LinkState.Loading)

    val playback: StateFlow<PlaybackState> = repository.playback
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlaybackState())

    /** Adapts the live link + playback queue into the UI's display model. */
    val displayContent: StateFlow<AssignedDisplay?> =
        combine(repository.linkState, repository.playback) { link, state ->
            val linked = link as? LinkState.Linked
            if (state.queue.isEmpty() && linked == null) null
            else AssignedDisplay(
                name = linked?.displayName ?: "PanelSena Display",
                mediaItems = state.queue,
                scheduleName = state.scheduleName,
                updatedAtMillis = System.currentTimeMillis()
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    var deviceId: String = ""
        private set
    var deviceKey: String = ""
        private set

    private var heartbeatJob: Job? = null
    private var commandJob: Job? = null
    private var linkJob: Job? = null

    init {
        startLinking()
    }

    /** Register the device and watch for the dashboard linking it to an account. */
    fun startLinking() {
        linkJob?.cancel()
        linkJob = viewModelScope.launch {
            val (id, key) = repository.let { it.getDeviceId() to it.getDeviceKey() }
            deviceId = id
            deviceKey = key
            repository.setLinkState(LinkState.Unlinked(id, key))
            repository.registerDevice()

            repository.observeDeviceLink(id).collect { link ->
                if (link == null) {
                    repository.setLinkState(LinkState.Unlinked(id, key))
                    stopRuntime()
                } else {
                    val name = repository.resolveDisplayName(link.displayId, fallbackName())
                    repository.setLinkState(
                        LinkState.Linked(link.userId, link.displayId, name, id)
                    )
                    startRuntime(LinkState.Linked(link.userId, link.displayId, name, id))
                }
            }
        }
    }

    /** Begin heartbeating and listening for commands once linked. */
    private fun startRuntime(link: LinkState.Linked) {
        if (heartbeatJob?.isActive == true) return

        heartbeatJob = viewModelScope.launch {
            while (isActive) {
                repository.sendHeartbeat(link)
                delay(10_000) // 10s, matching the Pi heartbeat cadence
            }
        }

        commandJob = viewModelScope.launch {
            repository.observeCommands(link.userId, link.displayId).collect { commands ->
                commands.filter { it.status == "pending" }.forEach { command ->
                    val (success, message) = repository.executeCommand(command)
                    repository.ackCommand(link.userId, link.displayId, command.commandId, success, message)
                    repository.sendHeartbeat(link)
                }
            }
        }
    }

    private fun stopRuntime() {
        heartbeatJob?.cancel(); heartbeatJob = null
        commandJob?.cancel(); commandJob = null
    }

    fun onMediaCompleted() {
        repository.onMediaCompleted()
        (linkState.value as? LinkState.Linked)?.let { link ->
            viewModelScope.launch { repository.sendHeartbeat(link) }
        }
    }

    fun playLocalQueue(startIndex: Int) = repository.playLocalQueue(startIndex)

    fun signOut() {
        (linkState.value as? LinkState.Linked)?.let { link ->
            viewModelScope.launch { repository.markOffline(link) }
        }
        stopRuntime()
        auth.signOut()
    }

    private fun fallbackName(): String =
        auth.currentUser?.displayName?.let { "$it's Display" } ?: "PanelSena Display"

    val todayDate: String
        get() = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())

    val weekDays: List<WeekDay>
        get() {
            val days = mutableListOf<WeekDay>()
            val calendar = Calendar.getInstance()
            val todayDay = calendar.get(Calendar.DAY_OF_MONTH)
            val sdfDay = SimpleDateFormat("EEE", Locale.getDefault())
            val sdfNum = SimpleDateFormat("d", Locale.getDefault())
            for (i in 0 until 7) {
                val dayName = sdfDay.format(calendar.time)
                val dayNum = sdfNum.format(calendar.time)
                val isToday = calendar.get(Calendar.DAY_OF_MONTH) == todayDay
                days.add(WeekDay(dayName, dayNum, isToday))
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            return days
        }
}

data class WeekDay(
    val shortName: String,
    val number: String,
    val isToday: Boolean
)
