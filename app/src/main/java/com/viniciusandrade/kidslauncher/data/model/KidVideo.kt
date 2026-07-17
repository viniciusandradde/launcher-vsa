package com.viniciusandrade.kidslauncher.data.model

/**
 * A parent-curated YouTube video shown as a tappable card with its thumbnail.
 *
 * Persisted as "videoId" or "videoId|name" in a DataStore string set, so [name]
 * is optional. Thumbnails come from YouTube's public image host.
 */
data class KidVideo(
    val id: String,
    val name: String = "",
) {
    val watchUrl: String get() = "https://www.youtube.com/watch?v=$id"

    /** hqdefault exists for every video and is a good size for a grid tile. */
    val thumbnailUrl: String get() = "https://img.youtube.com/vi/$id/hqdefault.jpg"

    val displayName: String get() = name.ifBlank { "Vídeo" }

    /** How this entry is stored in the DataStore string set. */
    val storageKey: String get() = if (name.isBlank()) id else "$id|$name"

    companion object {
        fun fromStorage(entry: String): KidVideo {
            val idx = entry.indexOf('|')
            return if (idx < 0) KidVideo(entry)
            else KidVideo(entry.substring(0, idx), entry.substring(idx + 1))
        }
    }
}
