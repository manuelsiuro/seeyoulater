package com.msa.seeyoulater.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "links")
data class Link(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    var title: String? = null,
    var description: String? = null,
    var faviconUrl: String? = null,
    var previewImageUrl: String? = null,
    val addedTimestamp: Long = System.currentTimeMillis(),
    var isStarred: Boolean = false,
    var isOpened: Boolean = false,
    var lastOpenedTimestamp: Long? = null,

    // Reader Mode fields
    var savedContent: String? = null, // Full article content in HTML
    var contentSavedTimestamp: Long? = null, // When content was saved
    var readingProgress: Float = 0f, // Reading progress (0.0 to 1.0)
    var estimatedReadingTime: Int? = null, // Estimated reading time in minutes

    // Notes & Annotations
    var notes: String? = null, // User's personal notes about the link
    var notesLastModified: Long? = null, // When notes were last modified

    // Archive status
    var isArchived: Boolean = false, // Whether the link is archived
    var archivedTimestamp: Long? = null, // When the link was archived

    // Health check status
    var healthStatus: String? = null, // HEALTHY, REDIRECT, CLIENT_ERROR, SERVER_ERROR, UNREACHABLE, UNKNOWN
    var lastHealthCheck: Long? = null, // When the link was last checked
    var healthStatusCode: Int? = null // HTTP status code from last check
)
