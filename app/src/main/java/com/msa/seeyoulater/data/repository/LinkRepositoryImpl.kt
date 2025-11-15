package com.msa.seeyoulater.data.repository

import android.util.Log
import android.webkit.URLUtil
import com.msa.seeyoulater.data.local.dao.LinkDao
import com.msa.seeyoulater.data.local.entity.Link
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.IOException
import java.net.URL

class LinkRepositoryImpl(
    private val linkDao: LinkDao,
    private val externalScope: CoroutineScope // For long running background tasks like preview fetching
) : LinkRepository {

    override fun getAllLinks(): Flow<List<Link>> = linkDao.getAllLinks()
    override fun getStarredLinks(): Flow<List<Link>> = linkDao.getStarredLinks()
    override fun getAllLinksSortedByLastOpened(): Flow<List<Link>> = linkDao.getAllLinksSortedByLastOpened()
    override fun getStarredLinksSortedByLastOpened(): Flow<List<Link>> = linkDao.getStarredLinksSortedByLastOpened()
    override suspend fun getLinkById(id: Long): Link? = linkDao.getLinkById(id)

    override suspend fun saveLink(url: String, title: String?): Long {
        val link = Link(url = url, title = title?.takeIf { it.isNotBlank() })
        val newId = linkDao.insertLink(link)
        // Trigger preview fetching in the background without blocking the save operation
        if (newId > 0) {
            externalScope.launch {
                fetchAndUpdateLinkPreview(newId)
            }
        }
        return newId
    }

    override suspend fun updateLink(link: Link) {
        linkDao.updateLink(link)
    }

    override suspend fun toggleStarStatus(linkId: Long) {
        val link = linkDao.getLinkById(linkId)
        link?.let {
            it.isStarred = !it.isStarred
            linkDao.updateLink(it)
        }
    }

    override suspend fun markLinkAsOpened(linkId: Long) {
        val link = linkDao.getLinkById(linkId)
        link?.let {
            if (!it.isOpened || it.lastOpenedTimestamp == null) { // Update only if not already marked or first time
                it.isOpened = true
                it.lastOpenedTimestamp = System.currentTimeMillis()
                linkDao.updateLink(it)
            } else { // Optionally update timestamp on every open
                it.lastOpenedTimestamp = System.currentTimeMillis()
                linkDao.updateLink(it)
            }
        }
    }

    override suspend fun deleteLink(linkId: Long) {
        linkDao.deleteLinkById(linkId)
    }

     override suspend fun deleteAllLinks() {
        linkDao.deleteAllLinks()
    }

    override suspend fun fetchAndUpdateLinkPreview(linkId: Long) {
        withContext(Dispatchers.IO) { // Perform network and DB operations on IO thread
            val link = linkDao.getLinkById(linkId)
            if (link == null || !URLUtil.isValidUrl(link.url)) {
                Log.w("LinkPreview", "Invalid URL or link not found for ID: $linkId")
                return@withContext
            }

            // Only fetch if essential metadata is missing
            if (link.title.isNullOrBlank() || link.previewImageUrl.isNullOrBlank() || link.faviconUrl.isNullOrBlank()) {
                 Log.d("LinkPreview", "Fetching metadata for ${link.url}")
                try {
                    // Simple User-Agent helps some sites return better content
                    val connection = Jsoup.connect(link.url)
                                          .userAgent("Mozilla/5.0 (Linux; Android 10; SM-G975F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Mobile Safari/537.36")
                                          .timeout(10000) // 10 seconds timeout
                                          .followRedirects(true)
                                          .get()

                    val fetchedTitle = connection.title()?.takeIf { it.isNotBlank() } ?: connection.select("meta[property=og:title]").attr("content").takeIf { it.isNotBlank() }
                    val fetchedDescription = connection.select("meta[name=description]").attr("content").takeIf { it.isNotBlank() } ?: connection.select("meta[property=og:description]").attr("content").takeIf { it.isNotBlank() }
                    var fetchedFavicon = connection.select("link[rel=shortcut icon]").attr("href").takeIf { it.isNotBlank() }
                                          ?: connection.select("link[rel=icon]").attr("href").takeIf { it.isNotBlank() }
                    var fetchedPreviewImage = connection.select("meta[property=og:image]").attr("content").takeIf { it.isNotBlank() }

                     // Make relative URLs absolute
                    fetchedFavicon = fetchedFavicon?.let { makeAbsoluteUrl(link.url, it) }
                    fetchedPreviewImage = fetchedPreviewImage?.let { makeAbsoluteUrl(link.url, it) }

                    // Update link only if new data was found
                    var updated = false
                    if (link.title.isNullOrBlank() && !fetchedTitle.isNullOrBlank()) {
                        link.title = fetchedTitle
                        updated = true
                         Log.d("LinkPreview", "Found Title: $fetchedTitle")
                    }
                    if (link.description.isNullOrBlank() && !fetchedDescription.isNullOrBlank()) {
                        link.description = fetchedDescription
                        updated = true
                         Log.d("LinkPreview", "Found Desc: $fetchedDescription")
                    }
                     if (link.faviconUrl.isNullOrBlank() && !fetchedFavicon.isNullOrBlank()) {
                        link.faviconUrl = fetchedFavicon
                        updated = true
                         Log.d("LinkPreview", "Found Favicon: $fetchedFavicon")
                    }
                     if (link.previewImageUrl.isNullOrBlank() && !fetchedPreviewImage.isNullOrBlank()) {
                        link.previewImageUrl = fetchedPreviewImage
                        updated = true
                         Log.d("LinkPreview", "Found Preview Image: $fetchedPreviewImage")
                    }

                    if (updated) {
                        linkDao.updateLink(link)
                        Log.i("LinkPreview", "Updated metadata for link ID: $linkId")
                    } else {
                         Log.d("LinkPreview", "No new metadata found for link ID: $linkId")
                    }

                } catch (e: IOException) {
                    Log.e("LinkPreview", "Error fetching metadata for ${link.url}: ${e.message}")
                    // Optionally update the link to indicate fetching failed
                } catch (e: Exception) {
                     Log.e("LinkPreview", "Unexpected error fetching metadata for ${link.url}: ${e.message}")
                }
            } else {
                 Log.d("LinkPreview", "Skipping metadata fetch for link ID: $linkId, already populated.")
            }
        }
    }

     // Helper to resolve relative URLs to absolute URLs
    private fun makeAbsoluteUrl(baseUrl: String, relativeUrl: String): String? {
        return try {
            val base = URL(baseUrl)
            val resolved = URL(base, relativeUrl)
            resolved.toString()
        } catch (e: Exception) {
            Log.w("LinkPreview", "Could not resolve URL: $relativeUrl against base: $baseUrl")
            if (URLUtil.isValidUrl(relativeUrl)) relativeUrl else null // Fallback if it's already absolute
        }
    }
}
