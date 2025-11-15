package com.msa.seeyoulater.data.export

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.msa.seeyoulater.data.local.dao.CollectionDao
import com.msa.seeyoulater.data.local.dao.LinkDao
import com.msa.seeyoulater.data.local.dao.TagDao
import com.msa.seeyoulater.data.local.entity.LinkCollection
import com.msa.seeyoulater.data.local.entity.LinkTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages export and import of all app data
 */
class ExportManager(
    private val context: Context,
    private val linkDao: LinkDao,
    private val tagDao: TagDao,
    private val collectionDao: CollectionDao
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val TAG = "ExportManager"

    /**
     * Export all data to JSON file
     * Returns the file path if successful, null otherwise
     */
    suspend fun exportToFile(): String? = withContext(Dispatchers.IO) {
        try {
            // Collect all data
            val links = linkDao.getAllLinks().first()
            val tags = tagDao.getAllTags().first()
            val collections = collectionDao.getAllCollections().first()
            val linkTags = tagDao.getAllLinkTags()
            val linkCollections = collectionDao.getAllLinkCollections()

            // Convert to export models
            val exportData = ExportData(
                version = 1,
                exportedAt = System.currentTimeMillis(),
                links = links.map { it.toExport() },
                tags = tags.map { it.toExport() },
                collections = collections.map { it.toExport() },
                linkTags = linkTags.map {
                    LinkTagRelation(it.linkId, it.tagId, it.addedTimestamp)
                },
                linkCollections = linkCollections.map {
                    LinkCollectionRelation(it.linkId, it.collectionId, it.addedTimestamp, it.sortOrder)
                }
            )

            // Convert to JSON
            val json = gson.toJson(exportData)

            // Save to file
            val fileName = "seeyoulater_backup_${getTimestampString()}.json"
            val file = File(context.getExternalFilesDir(null), fileName)
            file.writeText(json)

            Log.i(TAG, "Export successful: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Export failed", e)
            null
        }
    }

    /**
     * Import data from JSON file
     * Returns true if successful, false otherwise
     */
    suspend fun importFromFile(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Read file
            val file = File(filePath)
            if (!file.exists()) {
                Log.e(TAG, "Import file does not exist: $filePath")
                return@withContext false
            }

            val json = file.readText()
            val exportData = gson.fromJson(json, ExportData::class.java)

            // Clear existing data (optional - could also merge)
            // For now, we'll clear to avoid duplicates
            linkDao.deleteAllLinks()

            // Import links
            exportData.links.forEach { linkExport ->
                val link = linkExport.toEntity()
                linkDao.insertLink(link)
            }

            // Import tags
            exportData.tags.forEach { tagExport ->
                val tag = tagExport.toEntity()
                tagDao.insertTag(tag)
            }

            // Import collections
            exportData.collections.forEach { collectionExport ->
                val collection = collectionExport.toEntity()
                collectionDao.insertCollection(collection)
            }

            // Import link-tag relationships
            exportData.linkTags.forEach { relation ->
                val linkTag = LinkTag(
                    linkId = relation.linkId,
                    tagId = relation.tagId,
                    addedTimestamp = relation.addedTimestamp
                )
                tagDao.insertLinkTag(linkTag)
            }

            // Import link-collection relationships
            exportData.linkCollections.forEach { relation ->
                val linkCollection = LinkCollection(
                    linkId = relation.linkId,
                    collectionId = relation.collectionId,
                    addedTimestamp = relation.addedTimestamp,
                    sortOrder = relation.sortOrder
                )
                collectionDao.insertLinkCollection(linkCollection)
            }

            Log.i(TAG, "Import successful from: $filePath")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Import failed", e)
            false
        }
    }

    /**
     * Get formatted timestamp string for file naming
     */
    private fun getTimestampString(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Get export file directory
     */
    fun getExportDirectory(): File? {
        return context.getExternalFilesDir(null)
    }

    /**
     * Get list of available backup files
     */
    fun getAvailableBackups(): List<File> {
        val directory = getExportDirectory() ?: return emptyList()
        return directory.listFiles { file ->
            file.name.startsWith("seeyoulater_backup_") && file.name.endsWith(".json")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    /**
     * Export all bookmarks to HTML format (Netscape Bookmark File Format)
     * Returns the file path if successful, null otherwise
     */
    suspend fun exportToHtml(): String? = withContext(Dispatchers.IO) {
        try {
            // Collect all data
            val links = linkDao.getAllLinks().first()
            val tags = tagDao.getAllTags().first()
            val collections = collectionDao.getAllCollections().first()
            val linkTags = tagDao.getAllLinkTags()
            val linkCollections = collectionDao.getAllLinkCollections()

            // Build maps for quick lookup
            val linkCollectionsMap = linkCollections.groupBy { it.linkId }.mapValues { entry ->
                collections.filter { collection -> entry.value.any { it.collectionId == collection.id } }
            }
            val linkTagsMap = linkTags.groupBy { it.linkId }.mapValues { entry ->
                tags.filter { tag -> entry.value.any { it.tagId == tag.id } }
            }

            // Generate HTML
            val html = HtmlExporter.generateHtml(
                links = links,
                collections = collections,
                tags = tags,
                linkCollections = linkCollectionsMap,
                linkTags = linkTagsMap
            )

            // Save to file
            val fileName = "seeyoulater_bookmarks_${getTimestampString()}.html"
            val file = File(context.getExternalFilesDir(null), fileName)
            file.writeText(html)

            Log.i(TAG, "HTML export successful: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "HTML export failed", e)
            null
        }
    }
}
