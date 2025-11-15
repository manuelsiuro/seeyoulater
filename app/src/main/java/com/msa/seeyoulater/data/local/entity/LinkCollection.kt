package com.msa.seeyoulater.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Junction table for many-to-many relationship between Links and Collections.
 * A link can belong to multiple collections, and a collection can contain multiple links.
 */
@Entity(
    tableName = "link_collections",
    primaryKeys = ["linkId", "collectionId"],
    foreignKeys = [
        ForeignKey(
            entity = Link::class,
            parentColumns = ["id"],
            childColumns = ["linkId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Collection::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["linkId"]),
        Index(value = ["collectionId"])
    ]
)
data class LinkCollection(
    val linkId: Long,
    val collectionId: Long,

    /**
     * Timestamp when this link was added to the collection
     */
    val addedTimestamp: Long = System.currentTimeMillis(),

    /**
     * Sort order for this link within the collection
     * Allows manual reordering of links in a collection
     */
    var sortOrder: Int = 0
)
