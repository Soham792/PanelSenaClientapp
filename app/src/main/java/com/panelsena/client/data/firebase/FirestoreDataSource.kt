package com.panelsena.client.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.panelsena.client.core.utils.awaitResult
import com.panelsena.client.data.model.ContentDoc
import com.panelsena.client.data.model.DisplayDoc
import com.panelsena.client.data.model.MediaItem
import com.panelsena.client.data.model.MediaType
import com.panelsena.client.data.model.ScheduleDoc
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads the dashboard's Firestore content model. Same resolution path as the Pi player:
 * schedule -> contentIds -> content docs -> playable media URLs.
 */
@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getDisplay(displayId: String): DisplayDoc? =
        firestore.collection("displays").document(displayId).get().awaitResult()
            .takeIf { it.exists() }?.toObject(DisplayDoc::class.java)

    /** Resolve a single content item to a playable [MediaItem]. */
    suspend fun getContent(contentId: String): MediaItem? {
        val doc = firestore.collection("content").document(contentId).get().awaitResult()
        if (!doc.exists()) return null
        val content = doc.toObject(ContentDoc::class.java) ?: return null
        if (content.url.isBlank()) return null
        return MediaItem(
            id = contentId,
            url = content.url,
            type = MediaType.fromContentType(content.type),
            name = content.name.ifBlank { "Content" }
        )
    }

    /** Resolve an ordered list of content ids to playable media, skipping any that fail. */
    suspend fun getContentItems(contentIds: List<String>): List<MediaItem> =
        contentIds.mapNotNull { runCatching { getContent(it) }.getOrNull() }

    suspend fun getSchedule(scheduleId: String): ScheduleDoc? =
        firestore.collection("schedules").document(scheduleId).get().awaitResult()
            .takeIf { it.exists() }?.toObject(ScheduleDoc::class.java)
}
