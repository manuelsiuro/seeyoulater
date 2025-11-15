package com.msa.seeyoulater.data.export

import com.msa.seeyoulater.data.local.entity.Link
import com.msa.seeyoulater.data.local.entity.Tag
import com.msa.seeyoulater.data.local.entity.Collection

/**
 * Data structure for exporting/importing all user data
 */
data class ExportData(
    val version: Int = 1, // Export format version for future compatibility
    val exportedAt: Long = System.currentTimeMillis(),
    val links: List<LinkExport>,
    val tags: List<TagExport>,
    val collections: List<CollectionExport>,
    val linkTags: List<LinkTagRelation>, // Link-Tag relationships
    val linkCollections: List<LinkCollectionRelation> // Link-Collection relationships
)

/**
 * Exportable link data
 */
data class LinkExport(
    val id: Long,
    val url: String,
    val title: String?,
    val description: String?,
    val faviconUrl: String?,
    val previewImageUrl: String?,
    val addedTimestamp: Long,
    val isStarred: Boolean,
    val isOpened: Boolean,
    val lastOpenedTimestamp: Long?,
    val savedContent: String?,
    val contentSavedTimestamp: Long?,
    val readingProgress: Float,
    val estimatedReadingTime: Int?,
    val notes: String?,
    val notesLastModified: Long?
)

/**
 * Exportable tag data
 */
data class TagExport(
    val id: Long,
    val name: String,
    val color: String?,
    val createdTimestamp: Long,
    val usageCount: Int
)

/**
 * Exportable collection data
 */
data class CollectionExport(
    val id: Long,
    val name: String,
    val description: String?,
    val icon: String?,
    val color: String?,
    val createdTimestamp: Long,
    val lastModifiedTimestamp: Long,
    val linkCount: Int,
    val sortOrder: Int
)

/**
 * Link-Tag relationship for export
 */
data class LinkTagRelation(
    val linkId: Long,
    val tagId: Long,
    val addedTimestamp: Long
)

/**
 * Link-Collection relationship for export
 */
data class LinkCollectionRelation(
    val linkId: Long,
    val collectionId: Long,
    val addedTimestamp: Long,
    val sortOrder: Int
)

/**
 * Extension functions to convert entities to export models
 */
fun Link.toExport() = LinkExport(
    id = id,
    url = url,
    title = title,
    description = description,
    faviconUrl = faviconUrl,
    previewImageUrl = previewImageUrl,
    addedTimestamp = addedTimestamp,
    isStarred = isStarred,
    isOpened = isOpened,
    lastOpenedTimestamp = lastOpenedTimestamp,
    savedContent = savedContent,
    contentSavedTimestamp = contentSavedTimestamp,
    readingProgress = readingProgress,
    estimatedReadingTime = estimatedReadingTime,
    notes = notes,
    notesLastModified = notesLastModified
)

fun Tag.toExport() = TagExport(
    id = id,
    name = name,
    color = color,
    createdTimestamp = createdTimestamp,
    usageCount = usageCount
)

fun Collection.toExport() = CollectionExport(
    id = id,
    name = name,
    description = description,
    icon = icon,
    color = color,
    createdTimestamp = createdTimestamp,
    lastModifiedTimestamp = lastModifiedTimestamp,
    linkCount = linkCount,
    sortOrder = sortOrder
)

/**
 * Extension functions to convert export models back to entities
 */
fun LinkExport.toEntity() = Link(
    id = id,
    url = url,
    title = title,
    description = description,
    faviconUrl = faviconUrl,
    previewImageUrl = previewImageUrl,
    addedTimestamp = addedTimestamp,
    isStarred = isStarred,
    isOpened = isOpened,
    lastOpenedTimestamp = lastOpenedTimestamp,
    savedContent = savedContent,
    contentSavedTimestamp = contentSavedTimestamp,
    readingProgress = readingProgress,
    estimatedReadingTime = estimatedReadingTime,
    notes = notes,
    notesLastModified = notesLastModified
)

fun TagExport.toEntity() = Tag(
    id = id,
    name = name,
    color = color,
    createdTimestamp = createdTimestamp,
    usageCount = usageCount
)

fun CollectionExport.toEntity() = Collection(
    id = id,
    name = name,
    description = description,
    icon = icon,
    color = color,
    createdTimestamp = createdTimestamp,
    lastModifiedTimestamp = lastModifiedTimestamp,
    linkCount = linkCount,
    sortOrder = sortOrder
)
