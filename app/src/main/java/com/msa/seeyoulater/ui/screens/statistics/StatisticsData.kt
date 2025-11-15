package com.msa.seeyoulater.ui.screens.statistics

/**
 * Statistics data for the user's bookmark collection
 */
data class StatisticsData(
    // Link Statistics
    val totalLinks: Int = 0,
    val starredLinks: Int = 0,
    val openedLinks: Int = 0,
    val unreadLinks: Int = 0,
    val linksWithSavedContent: Int = 0,
    val linksWithNotes: Int = 0,

    // Tag Statistics
    val totalTags: Int = 0,

    // Collection Statistics
    val totalCollections: Int = 0
) {
    val starredPercentage: Float
        get() = if (totalLinks > 0) (starredLinks.toFloat() / totalLinks * 100) else 0f

    val openedPercentage: Float
        get() = if (totalLinks > 0) (openedLinks.toFloat() / totalLinks * 100) else 0f

    val savedContentPercentage: Float
        get() = if (totalLinks > 0) (linksWithSavedContent.toFloat() / totalLinks * 100) else 0f

    val notesPercentage: Float
        get() = if (totalLinks > 0) (linksWithNotes.toFloat() / totalLinks * 100) else 0f
}
