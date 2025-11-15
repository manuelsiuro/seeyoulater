package com.msa.seeyoulater.data.services

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist

/**
 * Service for extracting main article content from web pages
 * Uses readability-style heuristics to identify the main content
 */
object ContentExtractor {

    private const val TAG = "ContentExtractor"
    private const val AVERAGE_WORDS_PER_MINUTE = 200 // Average reading speed

    /**
     * Extracted content data
     */
    data class ExtractedContent(
        val title: String?,
        val content: String, // Clean HTML content
        val textContent: String, // Plain text for analysis
        val estimatedReadingTime: Int // In minutes
    )

    /**
     * Extract article content from a URL
     */
    suspend fun extractFromUrl(url: String): ExtractedContent? = withContext(Dispatchers.IO) {
        try {
            val document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Linux; Android 10; SM-G975F) AppleWebKit/537.36")
                .timeout(15000)
                .followRedirects(true)
                .get()

            extractFromDocument(document)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting content from $url", e)
            null
        }
    }

    /**
     * Extract content from a Jsoup document
     */
    private fun extractFromDocument(document: Document): ExtractedContent {
        // Try to get title
        val title = document.title().takeIf { it.isNotBlank() }
            ?: document.select("meta[property=og:title]").attr("content").takeIf { it.isNotBlank() }
            ?: document.select("h1").first()?.text()

        // Remove unwanted elements
        removeUnwantedElements(document)

        // Find main content
        val mainContent = findMainContent(document)

        // Clean and simplify HTML
        val cleanedContent = cleanContent(mainContent)

        // Get plain text for analysis
        val textContent = Jsoup.parse(cleanedContent).text()

        // Calculate reading time
        val wordCount = textContent.split("\\s+".toRegex()).size
        val readingTime = (wordCount / AVERAGE_WORDS_PER_MINUTE).coerceAtLeast(1)

        return ExtractedContent(
            title = title,
            content = cleanedContent,
            textContent = textContent,
            estimatedReadingTime = readingTime
        )
    }

    /**
     * Remove unwanted elements like ads, navigation, etc.
     */
    private fun removeUnwantedElements(document: Document) {
        val selectorsToRemove = listOf(
            "script", "style", "noscript", "iframe", "embed",
            "nav", "header", "footer", "aside",
            ".ad", ".ads", ".advertisement", ".sidebar",
            ".social-share", ".comments", ".related",
            "[role=navigation]", "[role=complementary]", "[role=banner]"
        )

        selectorsToRemove.forEach { selector ->
            document.select(selector).remove()
        }
    }

    /**
     * Find the main content element using readability heuristics
     */
    private fun findMainContent(document: Document): Element {
        // Try common article selectors first
        val commonSelectors = listOf(
            "article",
            "[role=main]",
            ".post-content",
            ".article-content",
            ".entry-content",
            ".main-content",
            "#content",
            ".content"
        )

        for (selector in commonSelectors) {
            val element = document.select(selector).firstOrNull()
            if (element != null && hasSignificantContent(element)) {
                return element
            }
        }

        // Fall back to scoring algorithm
        return scoreAndSelectBest(document) ?: document.body()
    }

    /**
     * Check if an element has significant content
     */
    private fun hasSignificantContent(element: Element): Boolean {
        val text = element.text()
        val paragraphs = element.select("p")
        return text.length > 200 && paragraphs.size >= 2
    }

    /**
     * Score elements based on content density
     */
    private fun scoreAndSelectBest(document: Document): Element? {
        val candidates = document.select("div, section, article")
            .filter { it.select("p").size >= 2 }

        return candidates.maxByOrNull { element ->
            var score = 0

            // Score based on paragraph count
            score += element.select("p").size * 10

            // Score based on text length
            score += element.text().length / 100

            // Bonus for article-like class names
            val className = element.className().lowercase()
            if (className.contains("content") || className.contains("article") ||
                className.contains("post") || className.contains("entry")
            ) {
                score += 50
            }

            // Penalty for sidebar/navigation-like class names
            if (className.contains("sidebar") || className.contains("nav") ||
                className.contains("menu") || className.contains("footer")
            ) {
                score -= 50
            }

            score
        }
    }

    /**
     * Clean and simplify HTML content for reader view
     */
    private fun cleanContent(element: Element): String {
        // Create a safe HTML with only content elements
        val safelist = Safelist.relaxed()
            .addTags("figure", "figcaption", "mark", "time", "code", "pre")
            .addAttributes("img", "alt", "title")
            .addAttributes("a", "title")
            .addAttributes("pre", "class")
            .addAttributes("code", "class")

        return Jsoup.clean(element.html(), safelist)
    }
}
