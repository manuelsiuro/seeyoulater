package com.msa.seeyoulater.data.export

import com.msa.seeyoulater.data.local.entity.Link
import com.msa.seeyoulater.data.local.entity.Collection
import com.msa.seeyoulater.data.local.entity.Tag
import java.text.SimpleDateFormat
import java.util.*

object HtmlExporter {

    fun generateHtml(
        links: List<Link>,
        collections: List<Collection>,
        tags: List<Tag>,
        linkCollections: Map<Long, List<Collection>>,
        linkTags: Map<Long, List<Tag>>
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val exportDate = dateFormat.format(Date())

        return buildString {
            appendLine("<!DOCTYPE NETSCAPE-Bookmark-file-1>")
            appendLine("<!-- This is an automatically generated file.")
            appendLine("     It will be read and overwritten.")
            appendLine("     DO NOT EDIT! -->")
            appendLine("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">")
            appendLine("<TITLE>See You Later - Bookmarks</TITLE>")
            appendLine("<H1>See You Later - Bookmarks</H1>")
            appendLine("<p>Exported on: $exportDate</p>")
            appendLine("<DL><p>")

            // Export by Collections
            if (collections.isNotEmpty()) {
                appendLine("    <DT><H3>Collections</H3>")
                appendLine("    <DL><p>")
                collections.forEach { collection ->
                    appendLine("        <DT><H3>${escapeHtml(collection.name)}</H3>")
                    if (!collection.description.isNullOrBlank()) {
                        appendLine("        <DD>${escapeHtml(collection.description)}")
                    }
                    appendLine("        <DL><p>")

                    // Find links in this collection
                    val collectionLinks = links.filter { link ->
                        linkCollections[link.id]?.any { it.id == collection.id } == true
                    }

                    collectionLinks.forEach { link ->
                        appendLinkHtml(link, linkTags[link.id] ?: emptyList())
                    }

                    appendLine("        </DL><p>")
                }
                appendLine("    </DL><p>")
            }

            // Export by Tags
            if (tags.isNotEmpty()) {
                appendLine("    <DT><H3>Tags</H3>")
                appendLine("    <DL><p>")
                tags.forEach { tag ->
                    appendLine("        <DT><H3>${escapeHtml(tag.name)}</H3>")
                    appendLine("        <DL><p>")

                    // Find links with this tag
                    val tagLinks = links.filter { link ->
                        linkTags[link.id]?.any { it.id == tag.id } == true
                    }

                    tagLinks.forEach { link ->
                        appendLinkHtml(link, listOf(tag))
                    }

                    appendLine("        </DL><p>")
                }
                appendLine("    </DL><p>")
            }

            // Export All Links (uncategorized)
            val categorizedLinkIds = linkCollections.keys.union(linkTags.keys)
            val uncategorizedLinks = links.filter { !categorizedLinkIds.contains(it.id) }

            if (uncategorizedLinks.isNotEmpty()) {
                appendLine("    <DT><H3>Uncategorized</H3>")
                appendLine("    <DL><p>")
                uncategorizedLinks.forEach { link ->
                    appendLinkHtml(link, emptyList())
                }
                appendLine("    </DL><p>")
            }

            appendLine("</DL><p>")
        }
    }

    private fun StringBuilder.appendLinkHtml(link: Link, tags: List<Tag>) {
        val addedDate = link.addedTimestamp / 1000 // Convert to seconds
        val tagsList = tags.joinToString(", ") { it.name }
        val description = link.description ?: ""

        append("            <DT><A HREF=\"${escapeHtml(link.url)}\"")
        append(" ADD_DATE=\"$addedDate\"")
        if (link.isStarred) {
            append(" STARRED=\"1\"")
        }
        if (tagsList.isNotEmpty()) {
            append(" TAGS=\"${escapeHtml(tagsList)}\"")
        }
        append(">")
        append(escapeHtml(link.title ?: link.url))
        appendLine("</A>")
        if (description.isNotBlank()) {
            appendLine("            <DD>${escapeHtml(description)}")
        }
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
