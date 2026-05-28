package com.panelsena.client.data.model

import com.google.firebase.Timestamp

data class AssignedDisplay(
    val clientId: String = "",
    val name: String = "",
    val assignedMediaUrls: List<String> = emptyList(),
    val mediaTypes: List<String> = emptyList(),  // "image" | "video" | "pdf"
    val isActive: Boolean = true,
    val updatedAt: Timestamp? = null
) {
    val mediaItems: List<MediaItem>
        get() {
            return assignedMediaUrls.mapIndexed { index, url ->
                val typeStr = mediaTypes.getOrNull(index) ?: "image"
                val type = when (typeStr.lowercase()) {
                    "video" -> MediaType.VIDEO
                    "pdf" -> MediaType.PDF
                    else -> MediaType.IMAGE
                }
                // Parse filename from URL
                val rawName = url.substringBefore("?").substringAfterLast("/")
                val name = if (rawName.isEmpty() || rawName.length > 20) {
                    "Content Display ${index + 1}"
                } else {
                    rawName.replace("%20", " ").replace("-", " ").replace("_", " ").capitalize()
                }
                MediaItem(
                    url = url,
                    type = type,
                    name = name
                )
            }
        }
}

data class MediaItem(
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
    IMAGE("Image"), VIDEO("Video"), PDF("PDF")
}
