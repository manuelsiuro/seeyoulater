package com.msa.seeyoulater.data.local.dao

import androidx.room.*
import com.msa.seeyoulater.data.local.entity.Collection
import com.msa.seeyoulater.data.local.entity.LinkCollection
import com.msa.seeyoulater.data.local.entity.Link
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Collection and LinkCollection operations
 */
@Dao
interface CollectionDao {

    // ==================== Collection CRUD Operations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: Collection): Long

    @Update
    suspend fun updateCollection(collection: Collection)

    @Delete
    suspend fun deleteCollection(collection: Collection)

    @Query("DELETE FROM collections WHERE id = :collectionId")
    suspend fun deleteCollectionById(collectionId: Long)

    @Query("SELECT * FROM collections ORDER BY sortOrder ASC, name ASC")
    fun getAllCollections(): Flow<List<Collection>>

    @Query("SELECT * FROM collections WHERE id = :collectionId")
    suspend fun getCollectionById(collectionId: Long): Collection?

    @Query("SELECT * FROM collections WHERE name = :name")
    suspend fun getCollectionByName(name: String): Collection?

    /**
     * Search collections by name or description
     */
    @Query("""
        SELECT * FROM collections
        WHERE name LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
        ORDER BY linkCount DESC, name ASC
    """)
    fun searchCollections(query: String): Flow<List<Collection>>

    /**
     * Update collection link count
     */
    @Query("UPDATE collections SET linkCount = :count, lastModifiedTimestamp = :timestamp WHERE id = :collectionId")
    suspend fun updateCollectionLinkCount(collectionId: Long, count: Int, timestamp: Long = System.currentTimeMillis())

    /**
     * Update collection sort order
     */
    @Query("UPDATE collections SET sortOrder = :sortOrder WHERE id = :collectionId")
    suspend fun updateCollectionSortOrder(collectionId: Long, sortOrder: Int)

    // ==================== LinkCollection Operations ====================

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLinkCollection(linkCollection: LinkCollection)

    @Delete
    suspend fun deleteLinkCollection(linkCollection: LinkCollection)

    @Query("DELETE FROM link_collections WHERE linkId = :linkId AND collectionId = :collectionId")
    suspend fun deleteLinkCollectionByIds(linkId: Long, collectionId: Long)

    @Query("DELETE FROM link_collections WHERE linkId = :linkId")
    suspend fun deleteAllCollectionsForLink(linkId: Long)

    @Query("DELETE FROM link_collections WHERE collectionId = :collectionId")
    suspend fun deleteAllLinksInCollection(collectionId: Long)

    @Query("SELECT * FROM link_collections")
    suspend fun getAllLinkCollections(): List<LinkCollection>

    /**
     * Get all collections for a specific link
     */
    @Query("""
        SELECT collections.* FROM collections
        INNER JOIN link_collections ON collections.id = link_collections.collectionId
        WHERE link_collections.linkId = :linkId
        ORDER BY collections.name ASC
    """)
    fun getCollectionsForLink(linkId: Long): Flow<List<Collection>>

    /**
     * Get collection IDs for a specific link
     */
    @Query("SELECT collectionId FROM link_collections WHERE linkId = :linkId")
    suspend fun getCollectionIdsForLink(linkId: Long): List<Long>

    /**
     * Get all links in a specific collection
     */
    @Query("""
        SELECT links.* FROM links
        INNER JOIN link_collections ON links.id = link_collections.linkId
        WHERE link_collections.collectionId = :collectionId
        ORDER BY link_collections.sortOrder ASC, link_collections.addedTimestamp DESC
    """)
    fun getLinksInCollection(collectionId: Long): Flow<List<Link>>

    /**
     * Get link IDs in a specific collection
     */
    @Query("SELECT linkId FROM link_collections WHERE collectionId = :collectionId ORDER BY sortOrder ASC")
    fun getLinkIdsInCollection(collectionId: Long): Flow<List<Long>>

    /**
     * Get count of links in a specific collection
     */
    @Query("SELECT COUNT(*) FROM link_collections WHERE collectionId = :collectionId")
    suspend fun getLinkCountInCollection(collectionId: Long): Int

    /**
     * Check if a link is in a specific collection
     */
    @Query("SELECT EXISTS(SELECT 1 FROM link_collections WHERE linkId = :linkId AND collectionId = :collectionId)")
    suspend fun isLinkInCollection(linkId: Long, collectionId: Long): Boolean

    /**
     * Update sort order for a link in a collection
     */
    @Query("UPDATE link_collections SET sortOrder = :sortOrder WHERE linkId = :linkId AND collectionId = :collectionId")
    suspend fun updateLinkSortOrder(linkId: Long, collectionId: Long, sortOrder: Int)

    /**
     * Get the maximum sort order in a collection (for adding new links at the end)
     */
    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM link_collections WHERE collectionId = :collectionId")
    suspend fun getMaxSortOrderInCollection(collectionId: Long): Int

    /**
     * Add a link to a collection
     */
    @Transaction
    suspend fun addLinkToCollection(linkId: Long, collectionId: Long) {
        val maxSortOrder = getMaxSortOrderInCollection(collectionId)
        val linkCollection = LinkCollection(
            linkId = linkId,
            collectionId = collectionId,
            sortOrder = maxSortOrder + 1
        )
        insertLinkCollection(linkCollection)

        // Update collection link count
        val currentCount = getLinkCountInCollection(collectionId)
        updateCollectionLinkCount(collectionId, currentCount)
    }

    /**
     * Remove a link from a collection
     */
    @Transaction
    suspend fun removeLinkFromCollection(linkId: Long, collectionId: Long) {
        deleteLinkCollectionByIds(linkId, collectionId)

        // Update collection link count
        val currentCount = getLinkCountInCollection(collectionId)
        updateCollectionLinkCount(collectionId, currentCount)
    }

    /**
     * Move a link to a new position within a collection
     * This requires updating sort orders for other items
     */
    @Transaction
    suspend fun moveLinkInCollection(linkId: Long, collectionId: Long, newSortOrder: Int) {
        updateLinkSortOrder(linkId, collectionId, newSortOrder)
    }
}
