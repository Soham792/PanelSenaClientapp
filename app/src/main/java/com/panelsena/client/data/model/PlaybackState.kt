package com.panelsena.client.data.model

/** Whether this device has been linked to a PanelSena account from the dashboard. */
sealed interface LinkState {
    data object Loading : LinkState
    data class Unlinked(val deviceId: String, val deviceKey: String) : LinkState
    data class Linked(
        val userId: String,
        val displayId: String,
        val displayName: String,
        val deviceId: String
    ) : LinkState
}

/** Live playback the UI player renders and the heartbeat reports. */
data class PlaybackState(
    val queue: List<MediaItem> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val scheduleName: String? = null,
    val volume: Int = 80,
    val brightness: Int = 100
) {
    val currentItem: MediaItem? get() = queue.getOrNull(currentIndex)
}
