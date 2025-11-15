package com.msa.seeyoulater.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a Collection of links.
 * Collections allow users to group related links together (e.g., "Work Projects", "Recipes").
 */
@Entity(tableName = "collections")
data class Collection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * The name of the collection (e.g., "Work", "Personal Projects")
     */
    val name: String,

    /**
     * Optional description of what this collection is for
     */
    val description: String? = null,

    /**
     * Optional icon identifier for visual representation
     * Could be Material Icon name or emoji
     */
    val icon: String? = null,

    /**
     * Optional color for visual distinction in UI
     * Stored as hex color string (e.g., "#FF5722")
     */
    val color: String? = null,

    /**
     * Timestamp when the collection was created
     */
    val createdTimestamp: Long = System.currentTimeMillis(),

    /**
     * Timestamp when the collection was last modified
     */
    var lastModifiedTimestamp: Long = System.currentTimeMillis(),

    /**
     * Number of links in this collection (denormalized for performance)
     * Updated when links are added/removed
     */
    var linkCount: Int = 0,

    /**
     * Sort order for displaying collections
     * Lower values appear first
     */
    var sortOrder: Int = 0
)
