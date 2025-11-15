package com.msa.seeyoulater.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a Tag that can be applied to links.
 * Tags allow users to categorize and organize their saved links.
 */
@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * The name of the tag (e.g., "Work", "Read Later", "Tutorial")
     * Must be unique per user
     */
    val name: String,

    /**
     * Optional color for visual distinction in UI
     * Stored as hex color string (e.g., "#FF5722")
     */
    val color: String? = null,

    /**
     * Timestamp when the tag was created
     */
    val createdTimestamp: Long = System.currentTimeMillis(),

    /**
     * Number of times this tag has been used (denormalized for performance)
     * Updated when links are tagged/untagged
     */
    var usageCount: Int = 0
)
