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
    var lastOpenedTimestamp: Long? = null
)
