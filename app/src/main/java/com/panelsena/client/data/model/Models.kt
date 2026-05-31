package com.panelsena.client.data.model

// ===== UI playback models =====

data class MediaItem(
    val id: String = "",
    val url: String,
    val type: MediaType,
    val name: String = ""
) {
    val meta: String
        get() = when (type) {
            MediaType.IMAGE -> "Duration: 8s"
            MediaType.VIDEO -> "Seamless Video"
            MediaType.PDF -> "6s per page"
        }
}

enum class MediaType(val label: String) {
    IMAGE("Image"), VIDEO("Video"), PDF("PDF");

    companion object {
        /** Maps the dashboard content type ("image" | "video" | "document") to a player type. */
        fun fromContentType(type: String?): MediaType = when (type?.lowercase()) {
            "video" -> VIDEO
            "document", "pdf" -> PDF
            else -> IMAGE
        }
    }
}

/**
 * UI aggregate consumed by Home / Schedule / Info screens. It is rebuilt from the real
 * backend data (linked display name + the content queue currently assigned/playing).
 */
data class AssignedDisplay(
    val name: String = "",
    val mediaItems: List<MediaItem> = emptyList(),
    val scheduleName: String? = null,
    val updatedAtMillis: Long? = null
) {
    val mediaTypes: List<String> get() = mediaItems.map { it.type.label.lowercase() }
    val assignedMediaUrls: List<String> get() = mediaItems.map { it.url }
}

// ===== Backend contract models (match dashboard lib/types.ts) =====

/** Firestore `content/{id}` document. */
data class ContentDoc(
    val name: String = "",
    val type: String = "image",
    val url: String = ""
)

/** Firestore `schedules/{id}` document (only fields the client needs). */
data class ScheduleDoc(
    val name: String = "",
    val contentIds: List<String> = emptyList(),
    val displayIds: List<String> = emptyList(),
    val status: String = "active"
)

/** Firestore `displays/{id}` document (only fields the client needs). */
data class DisplayDoc(
    val name: String = "",
    val location: String = ""
)

/** Realtime DB `device_links/{deviceId}` entry written by the dashboard on link. */
data class DeviceLink(
    val userId: String,
    val displayId: String
)

/** Realtime DB `users/{uid}/displays/{id}/commands/{commandId}` entry. */
data class PlaybackCommand(
    val commandId: String,
    val type: String,
    val contentId: String? = null,
    val scheduleId: String? = null,
    val volume: Int? = null,
    val brightness: Int? = null,
    val status: String = "pending"
)
