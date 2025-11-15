package com.msa.seeyoulater.data.health

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

enum class LinkHealthStatus {
    HEALTHY,      // Link is accessible (200-299)
    REDIRECT,     // Link redirects (300-399)
    CLIENT_ERROR, // Client error (400-499)
    SERVER_ERROR, // Server error (500-599)
    UNREACHABLE,  // Cannot connect to server
    UNKNOWN       // Not yet checked
}

data class LinkHealthResult(
    val status: LinkHealthStatus,
    val statusCode: Int?,
    val errorMessage: String?
)

object LinkHealthChecker {
    private const val TAG = "LinkHealthChecker"
    private const val TIMEOUT_MS = 10000 // 10 seconds

    /**
     * Check if a URL is accessible
     * Uses HEAD request to minimize bandwidth
     */
    suspend fun checkLink(url: String): LinkHealthResult = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "HEAD"
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                instanceFollowRedirects = false
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android)")
            }

            val statusCode = connection.responseCode
            connection.disconnect()

            val status = when (statusCode) {
                in 200..299 -> LinkHealthStatus.HEALTHY
                in 300..399 -> LinkHealthStatus.REDIRECT
                in 400..499 -> LinkHealthStatus.CLIENT_ERROR
                in 500..599 -> LinkHealthStatus.SERVER_ERROR
                else -> LinkHealthStatus.UNKNOWN
            }

            Log.d(TAG, "Checked $url: $statusCode ($status)")
            LinkHealthResult(status, statusCode, null)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to check $url: ${e.message}")
            LinkHealthResult(
                status = LinkHealthStatus.UNREACHABLE,
                statusCode = null,
                errorMessage = e.message
            )
        }
    }

    /**
     * Get human-readable description for health status
     */
    fun getStatusDescription(status: LinkHealthStatus): String {
        return when (status) {
            LinkHealthStatus.HEALTHY -> "Link is accessible"
            LinkHealthStatus.REDIRECT -> "Link redirects to another URL"
            LinkHealthStatus.CLIENT_ERROR -> "Link not found or forbidden"
            LinkHealthStatus.SERVER_ERROR -> "Server error"
            LinkHealthStatus.UNREACHABLE -> "Cannot reach server"
            LinkHealthStatus.UNKNOWN -> "Not checked"
        }
    }
}
