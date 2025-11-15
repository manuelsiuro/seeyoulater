package com.msa.seeyoulater.data.repository

import com.msa.seeyoulater.data.local.entity.Link
import com.msa.seeyoulater.data.local.entity.Tag
import com.msa.seeyoulater.data.local.entity.Collection
import kotlinx.coroutines.flow.Flow

interface LinkRepository {
    // ==================== Link Operations ====================
    fun getAllLinks(): Flow<List<Link>>
    fun getStarredLinks(): Flow<List<Link>>
    fun getAllLinksSortedByLastOpened(): Flow<List<Link>>
    fun getStarredLinksSortedByLastOpened(): Flow<List<Link>>
    suspend fun getLinkById(id: Long): Link?
    suspend fun getLinkByUrl(url: String): Link?
    suspend fun isUrlSaved(url: String): Boolean
    suspend fun saveLink(url: String, title: String?): Long
    suspend fun updateLink(link: Link)
    suspend fun toggleStarStatus(linkId: Long)
    suspend fun markLinkAsOpened(linkId: Long)
    suspend fun deleteLink(linkId: Long)
    suspend fun deleteAllLinks()
    suspend fun fetchAndUpdateLinkPreview(linkId: Long)

    // ==================== Tag Operations ====================
    fun getAllTags(): Flow<List<Tag>>
    fun getTagsByUsage(): Flow<List<Tag>>
    fun searchTags(query: String): Flow<List<Tag>>
    fun getTagsForLink(linkId: Long): Flow<List<Tag>>
    suspend fun getTagById(tagId: Long): Tag?
    suspend fun createTag(name: String, color: String? = null): Long
    suspend fun updateTag(tag: Tag)
    suspend fun deleteTag(tagId: Long)
    suspend fun addTagToLink(linkId: Long, tagName: String, tagColor: String? = null)
    suspend fun removeTagFromLink(linkId: Long, tagId: Long)
    suspend fun setTagsForLink(linkId: Long, tagNames: List<String>)
    fun getLinkIdsForTag(tagId: Long): Flow<List<Long>>

    // ==================== Collection Operations ====================
    fun getAllCollections(): Flow<List<Collection>>
    fun searchCollections(query: String): Flow<List<Collection>>
    fun getCollectionsForLink(linkId: Long): Flow<List<Collection>>
    fun getLinksInCollection(collectionId: Long): Flow<List<Link>>
    suspend fun getCollectionById(collectionId: Long): Collection?
    suspend fun createCollection(name: String, description: String? = null, icon: String? = null, color: String? = null): Long
    suspend fun updateCollection(collection: Collection)
    suspend fun deleteCollection(collectionId: Long)
    suspend fun addLinkToCollection(linkId: Long, collectionId: Long)
    suspend fun removeLinkFromCollection(linkId: Long, collectionId: Long)
    suspend fun updateLinkSortOrderInCollection(linkId: Long, collectionId: Long, sortOrder: Int)
    suspend fun isLinkInCollection(linkId: Long, collectionId: Long): Boolean

    // ==================== Reader Mode Operations ====================
    suspend fun saveArticleContent(linkId: Long, content: String, estimatedReadingTime: Int)
    suspend fun updateReadingProgress(linkId: Long, progress: Float)
    suspend fun hasArticleContent(linkId: Long): Boolean
}
