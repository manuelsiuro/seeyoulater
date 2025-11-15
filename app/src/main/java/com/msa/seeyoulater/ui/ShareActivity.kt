package com.msa.seeyoulater.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.msa.seeyoulater.LinkManagerApp
import com.msa.seeyoulater.R
import com.msa.seeyoulater.data.repository.LinkRepository
import kotlinx.coroutines.launch

class ShareActivity : ComponentActivity() {

    private lateinit var repository: LinkRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get repository instance (ideally via DI)
         repository = (application as LinkManagerApp).repository


        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            handleSharedText(intent)
        } else {
            // Handle other cases or finish if the intent is not as expected
            Log.w("ShareActivity", "Received unexpected intent action or type.")
            finish()
        }
    }

    private fun handleSharedText(intent: Intent) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT) // Often contains title/context

        if (sharedText != null && URLUtil.isValidUrl(sharedText)) {
            saveLink(sharedText, subject)
        } else if (sharedText != null) {
            // Try to extract URL from shared text if it's not just a URL
             val potentialUrl = extractUrl(sharedText)
             if (potentialUrl != null && URLUtil.isValidUrl(potentialUrl)) {
                 saveLink(potentialUrl, subject ?: extractTitleGuess(sharedText)) // Use subject or try guessing title
            } else {
                Log.w("ShareActivity", "Shared text is not a valid URL: $sharedText")
                 Toast.makeText(this, getString(R.string.invalid_url_received), Toast.LENGTH_LONG).show()
                 finish()
            }
        } else {
            Log.w("ShareActivity", "No shared text received.")
            Toast.makeText(this, getString(R.string.invalid_url_received), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

     // Basic URL extraction (can be improved)
    private fun extractUrl(text: String): String? {
        // Simple regex to find URLs (might need refinement)
        val urlPattern = "(https?://\\S+)".toRegex()
        return urlPattern.find(text)?.value
    }

     // Very basic title guessing (can be improved)
    private fun extractTitleGuess(text: String): String? {
        // Take the first line as a potential title, trim whitespace
        return text.lines().firstOrNull()?.trim()?.takeIf { it.isNotEmpty() && !URLUtil.isValidUrl(it) }
    }


    private fun saveLink(url: String, title: String?) {
        lifecycleScope.launch { // Use lifecycleScope for coroutine tied to Activity lifecycle
            try {
                val newId = repository.saveLink(url, title)
                 if (newId > 0) {
                    Log.i("ShareActivity", "Link saved successfully with ID: $newId")
                    Toast.makeText(applicationContext, getString(R.string.link_saved_success), Toast.LENGTH_SHORT).show()
                     // Optional: Trigger a background update or sync if needed
                     // repository.fetchAndUpdateLinkPreview(newId) // Already triggered within repo save
                } else {
                     Log.e("ShareActivity", "Failed to save link (repository returned non-positive ID). URL: $url")
                    Toast.makeText(applicationContext, getString(R.string.link_saved_error), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("ShareActivity", "Error saving link: ${e.message}", e)
                Toast.makeText(applicationContext, getString(R.string.link_saved_error), Toast.LENGTH_LONG).show()
            } finally {
                finish() // Close the activity regardless of success or failure
            }
        }
    }
}
