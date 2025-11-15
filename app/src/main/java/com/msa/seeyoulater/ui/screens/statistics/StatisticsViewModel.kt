package com.msa.seeyoulater.ui.screens.statistics

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.seeyoulater.data.repository.LinkRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StatisticsState(
    val statistics: StatisticsData = StatisticsData(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class StatisticsViewModel(
    private val repository: LinkRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StatisticsState())
    val state: StateFlow<StatisticsState> = _state.asStateFlow()

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val totalLinks = repository.getTotalLinksCount()
                val starredLinks = repository.getStarredLinksCount()
                val openedLinks = repository.getOpenedLinksCount()
                val linksWithSavedContent = repository.getLinksWithSavedContentCount()
                val linksWithNotes = repository.getLinksWithNotesCount()
                val totalTags = repository.getTotalTagsCount()
                val totalCollections = repository.getTotalCollectionsCount()

                val statistics = StatisticsData(
                    totalLinks = totalLinks,
                    starredLinks = starredLinks,
                    openedLinks = openedLinks,
                    unreadLinks = totalLinks - openedLinks,
                    linksWithSavedContent = linksWithSavedContent,
                    linksWithNotes = linksWithNotes,
                    totalTags = totalTags,
                    totalCollections = totalCollections
                )

                _state.update {
                    it.copy(
                        statistics = statistics,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("StatisticsViewModel", "Error loading statistics", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load statistics: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
