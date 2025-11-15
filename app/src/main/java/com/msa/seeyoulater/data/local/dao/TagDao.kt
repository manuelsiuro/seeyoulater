package com.msa.seeyoulater.data.local.dao

import androidx.room.*
import com.msa.seeyoulater.data.local.entity.Tag
import com.msa.seeyoulater.data.local.entity.LinkTag
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Tag and LinkTag operations
 */
@Dao
interface TagDao {

    // ==================== Tag CRUD Operations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag): Long

    @Update
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteTagById(tagId: Long)

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getTagById(tagId: Long): Tag?

    @Query("SELECT * FROM tags WHERE name = :name")
    suspend fun getTagByName(name: String): Tag?

    /**
     * Search tags by name (for autocomplete functionality)
     * Returns tags that contain the search query
     */
    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query || '%' ORDER BY usageCount DESC, name ASC")
    fun searchTags(query: String): Flow<List<Tag>>

    /**
     * Get all tags sorted by usage count (most used first)
     */
    @Query("SELECT * FROM tags ORDER BY usageCount DESC, name ASC")
    fun getTagsByUsage(): Flow<List<Tag>>

    /**
     * Update tag usage count
     */
    @Query("UPDATE tags SET usageCount = :count WHERE id = :tagId")
    suspend fun updateTagUsageCount(tagId: Long, count: Int)

    // ==================== LinkTag Operations ====================

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLinkTag(linkTag: LinkTag)

    @Delete
    suspend fun deleteLinkTag(linkTag: LinkTag)

    @Query("DELETE FROM link_tags WHERE linkId = :linkId AND tagId = :tagId")
    suspend fun deleteLinkTagByIds(linkId: Long, tagId: Long)

    @Query("DELETE FROM link_tags WHERE linkId = :linkId")
    suspend fun deleteAllTagsForLink(linkId: Long)

    @Query("SELECT * FROM link_tags")
    suspend fun getAllLinkTags(): List<LinkTag>

    /**
     * Get all tags for a specific link
     */
    @Query("""
        SELECT tags.* FROM tags
        INNER JOIN link_tags ON tags.id = link_tags.tagId
        WHERE link_tags.linkId = :linkId
        ORDER BY tags.name ASC
    """)
    fun getTagsForLink(linkId: Long): Flow<List<Tag>>

    /**
     * Get tag IDs for a specific link (useful for quick checks)
     */
    @Query("SELECT tagId FROM link_tags WHERE linkId = :linkId")
    suspend fun getTagIdsForLink(linkId: Long): List<Long>

    /**
     * Get links that have a specific tag
     */
    @Query("SELECT linkId FROM link_tags WHERE tagId = :tagId ORDER BY addedTimestamp DESC")
    fun getLinkIdsForTag(tagId: Long): Flow<List<Long>>

    /**
     * Get count of links for a specific tag
     */
    @Query("SELECT COUNT(*) FROM link_tags WHERE tagId = :tagId")
    suspend fun getLinkCountForTag(tagId: Long): Int

    /**
     * Check if a link has a specific tag
     */
    @Query("SELECT EXISTS(SELECT 1 FROM link_tags WHERE linkId = :linkId AND tagId = :tagId)")
    suspend fun isLinkTagged(linkId: Long, tagId: Long): Boolean

    /**
     * Get or create a tag by name
     * This is a helper for tag autocomplete functionality
     */
    @Transaction
    suspend fun getOrCreateTag(name: String, color: String? = null): Tag {
        val existingTag = getTagByName(name)
        if (existingTag != null) {
            return existingTag
        }

        val newTag = Tag(name = name, color = color)
        val id = insertTag(newTag)
        return newTag.copy(id = id)
    }

    /**
     * Add a tag to a link (creates tag if it doesn't exist)
     */
    @Transaction
    suspend fun addTagToLink(linkId: Long, tagName: String, tagColor: String? = null) {
        val tag = getOrCreateTag(tagName, tagColor)
        insertLinkTag(LinkTag(linkId = linkId, tagId = tag.id))

        // Update usage count
        val currentCount = getLinkCountForTag(tag.id)
        updateTagUsageCount(tag.id, currentCount)
    }

    /**
     * Remove a tag from a link
     */
    @Transaction
    suspend fun removeTagFromLink(linkId: Long, tagId: Long) {
        deleteLinkTagByIds(linkId, tagId)

        // Update usage count
        val currentCount = getLinkCountForTag(tagId)
        updateTagUsageCount(tagId, currentCount)
    }

    // ==================== Statistics Queries ====================

    @Query("SELECT COUNT(*) FROM tags")
    suspend fun getTotalTagsCount(): Int
}
