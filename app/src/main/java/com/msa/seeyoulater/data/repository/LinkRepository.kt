package com.msa.seeyoulater.data.repository

import com.msa.seeyoulater.data.local.entity.Link
import kotlinx.coroutines.flow.Flow

interface LinkRepository {
    fun getAllLinks(): Flow<List<Link>>
    fun getStarredLinks(): Flow<List<Link>>
    fun getAllLinksSortedByLastOpened(): Flow<List<Link>>
    fun getStarredLinksSortedByLastOpened(): Flow<List<Link>>
    suspend fun getLinkById(id: Long): Link?
    suspend fun saveLink(url: String, title: String?): Long
    suspend fun updateLink(link: Link)
    suspend fun toggleStarStatus(linkId: Long)
    suspend fun markLinkAsOpened(linkId: Long)
    suspend fun deleteLink(linkId: Long)
    suspend fun deleteAllLinks()
    suspend fun fetchAndUpdateLinkPreview(linkId: Long)
}
