package com.msa.seeyoulater.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Junction table for many-to-many relationship between Links and Tags.
 * A link can have multiple tags, and a tag can be applied to multiple links.
 */
@Entity(
    tableName = "link_tags",
    primaryKeys = ["linkId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = Link::class,
            parentColumns = ["id"],
            childColumns = ["linkId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["linkId"]),
        Index(value = ["tagId"])
    ]
)
data class LinkTag(
    val linkId: Long,
    val tagId: Long,

    /**
     * Timestamp when this tag was added to the link
     */
    val addedTimestamp: Long = System.currentTimeMillis()
)
