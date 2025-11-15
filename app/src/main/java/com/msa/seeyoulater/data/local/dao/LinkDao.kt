package com.msa.seeyoulater.data.local.dao

import androidx.room.*
import com.msa.seeyoulater.data.local.entity.Link
import kotlinx.coroutines.flow.Flow

@Dao
interface LinkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: Link): Long

    @Update
    suspend fun updateLink(link: Link)

    @Query("DELETE FROM links WHERE id = :linkId")
    suspend fun deleteLinkById(linkId: Long)

    @Query("DELETE FROM links WHERE id IN (:linkIds)")
    suspend fun deleteLinksByIds(linkIds: List<Long>) // Optional bulk delete

    @Query("DELETE FROM links")
    suspend fun deleteAllLinks()

    @Query("SELECT * FROM links ORDER BY addedTimestamp DESC")
    fun getAllLinks(): Flow<List<Link>>

    @Query("SELECT * FROM links WHERE isStarred = 1 ORDER BY addedTimestamp DESC")
    fun getStarredLinks(): Flow<List<Link>>

    @Query("SELECT * FROM links ORDER BY lastOpenedTimestamp")
    fun getAllLinksSortedByLastOpened(): Flow<List<Link>>

     @Query("SELECT * FROM links WHERE isStarred = 1 ORDER BY lastOpenedTimestamp")
    fun getStarredLinksSortedByLastOpened(): Flow<List<Link>>

    @Query("SELECT * FROM links WHERE id = :linkId")
    suspend fun getLinkById(linkId: Long): Link?
}
